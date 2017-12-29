package com.pku.law.term.value

import com.pku.law.term.filter.*
import com.pku.law.term.preprocess.createTransferMap
import com.pku.law.term.preprocess.prepareDict
import com.pku.law.term.preprocess.setAll
import net.sf.json.JSONObject
import java.io.*
import java.util.*

/**
 * Created by serc1730 on 2017/12/27.
 */
val parentMap = mutableMapOf<String, MutableSet<String>>()
val frequencyMap = mutableMapOf<String, Int>()
val noParentSet = mutableSetOf<String>()
val termLenMap = mutableMapOf<String, Int>()
val cValueMap = mutableMapOf<String, Double>()
val ncValueMap = mutableMapOf<String, Double>()
val ncValueReviseMap = mutableMapOf<String, Double>()
val treeMap = mutableMapOf<String, MutableSet<String>>()
//用于记录上下文环境词汇。key为上下文词汇，value为上下文词汇所伴随的术语词汇。
val contextMap = mutableMapOf<String, MutableMap<String, Int>>()
//用于记录一个术语的上下文环境词汇，key为术语，value为术语中伴随出现的上下文词汇。
val termContextMap = mutableMapOf<String, MutableMap<String, Int>>()
//用于记录某个模式，如n_n下的所有候选术语
val appendMap = mutableMapOf<String, MutableSet<String>>()
val termToAppendMap = mutableMapOf<String, String>()
val specialSet = mutableSetOf<String>("市交通委员会","市财政税务局","经济贸易局","农林渔业局","计划生育办公室","国有资产管理办公室","城市管理办公室","社会保险管理局","住宅局")
val modelRatio = mutableMapOf<String, Double>()

fun main(args: Array<String>) {
//    calculateFreqAndParentStrMap()
//    initialize()
//    calculateCValue()
//    calculateNCValue()
    ncValueRevise()
}


fun ncValueRevise() {
    fun ncValueInit() {
        File("D:/term/ncValueFile.txt").forEachLine {
            if(it.isNotBlank()) {
                val splits = it.split("\t")
                ncValueMap.put(splits[0], splits[1].toDouble())
            }
        }
    }
    fun modelRatioInit() {
        val tmpMap = mutableMapOf<String, Int>()
        File("D:/term/modelSeqSorted.txt").forEachLine {
            if(it.isNotBlank()) {
                val splits = it.split("\t")
                tmpMap.put(splits[0], splits[1].toInt())
            }
        }
        val totalNum = tmpMap.values.sum()
        tmpMap.forEach { k, v -> modelRatio.put(k, v.toDouble() / totalNum) }
    }
    fun appendMapInit() {
        File("D:/term/appendDir").listFiles().forEach {
            val model = it.name.substring(0, it.name.indexOf('.'))
            appendMap.putIfAbsent(model, mutableSetOf())
            it.forEachLine { line ->
                if(line.isNotBlank()) {
                    appendMap.get(model)!!.add(line)
                }
            }
        }
        appendMap.forEach { k, v ->
            v.forEach { termToAppendMap.put(it, k) }
        }
    }
    ncValueInit()
    modelRatioInit()
    appendMapInit()
    val ncValueFile = File("D:/term/ncValueFile.txt")
    ncValueFile.forEachLine {
        if(it.isNotBlank()) {
            val splits = it.split("\t")
            ncValueMap.put(splits[0], splits[1].toDouble())
        }
    }
    ncValueMap.forEach { k, v ->
        ncValueReviseMap.put(k, v * modelRatio.get(termToAppendMap.get(k))!!)
    }
    val file = File("D:term/ncValueRevise.txt")
    file.delete()
    file.createNewFile()
    val writer = BufferedWriter(FileWriter(file))
    ncValueReviseMap.toList().sortedByDescending { it.second }.forEach { writer.write("${it.first}\t${it.second}\r\n") }
    writer.close()
}


fun initialize() {
    val freqFile = File("D:/term/freqFile.txt")
    val parentFile = File("D:/term/parentTerm.txt")
    val noParentFile = File("D:/term/noParentTerm.txt")
    val termLenFile = File("D:/term/termLenFile.txt")
    val contextFile = File("D:/term/context.txt")
    freqFile.forEachLine { val splits = it.split("\t"); frequencyMap.put(splits[0], splits[1].toInt()) }
    var bool = true
    var key = ""
    parentFile.forEachLine {
        if(bool) { key = it } else { parentMap.put(key, it.split("\t").toMutableSet()) }
        bool = !bool
    }

    noParentFile.forEachLine { noParentSet.add(it) }
    termLenFile.forEachLine { val splits = it.split("\t"); termLenMap.put(splits[0], splits[1].toInt()) }
    contextFile.forEachLine {
        if(it.isNotBlank()) {
            val splits = it.split("^^^")
            val key = splits[0]
            val set = splits[1].split("\t").toMutableSet()
            contextMap.put(key, mutableMapOf())
            set.forEach { it1 ->
                try {
                    val splits = it1.replace("(","").replace(")","").split(", ")
                    contextMap.get(key)!!.put(splits[0], splits[1].toInt())
                }
                catch(e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                    println(it1)
                }
            }

        }
    }
    println(contextMap.values.toList().flatMap { it.keys }.toSet().size)
    frequencyMap.keys.forEach { termContextMap.put(it, mutableMapOf()) }
    contextMap.forEach { context, termMap ->
        termMap.forEach { term, num ->
            try {
                termContextMap.get(term)!!.put(context, num)
            }
            catch (e: Exception) {
                println(term)
            }
        }
    }
//    val se = frequencyMap.keys.removeAll(contextMap.values.toList().flatMap { it.keys }.toSet())
//    println(frequencyMap.keys.toList())
//    println(termContextMap.size)
//    println(contextMap.values.toList().flatMap { it.keys }.toSet().size)
}

fun calculateCValue() {
    val log2 = Math.log(2.0)
    parentMap.forEach { k, v ->
        cValueMap.put(k, Math.log(termLenMap.get(k)!!.toDouble()) * (frequencyMap.get(k)!! - (v.sumBy { termLenMap.get(it)!! }) * 1.0 / v.size) / log2)
    }
    noParentSet.forEach { if(frequencyMap.containsKey(it)) { cValueMap.put(it, Math.log(termLenMap.get(it)!!.toDouble()) * frequencyMap.get(it)!!/ log2) } }
    var cValueFile = File("D:/term/cValueFile.txt")
    cValueFile.delete()
    cValueFile.createNewFile()
    val writer = BufferedWriter(FileWriter(cValueFile))
    cValueMap.toList().sortedByDescending { it.second }.forEach { writer.write("${it.first}\t${it.second}\r\n") }
    writer.close()
}

fun calculateNCValue() {
    val weightMap = mutableMapOf<String, Double>()
    val termNum = frequencyMap.keys.size
    contextMap.forEach { contextWord, termMap ->
        weightMap.put(contextWord, 1.0 * termMap.size / termNum)
    }
    fun fbwb(list: List<Pair<String, Int>>): Double  {
        var res = 0.0
        list.forEach { res += weightMap.get(it.first)!! * it.second }
        return res
    }
    termContextMap.forEach { term, contextMap ->
        val value = 0.8 * cValueMap.get(term)!! + fbwb(contextMap.toList())
        println("${cValueMap.get(term)}\t${value}")
        ncValueMap.put(term, value)
    }
    val ncValueFile = File("D:/term/ncValueFile.txt")
    ncValueFile.delete()
    ncValueFile.createNewFile()
    val writer = BufferedWriter(FileWriter(ncValueFile))
    ncValueMap.toList().sortedByDescending { it.second }.forEach { writer.write("${it.first}\t${it.second}\r\n") }
    writer.close()
}


fun calculateFreqAndParentStrMap() {
    val head = createTransferMap()
    prepareDict()
    val parentSet = mutableSetOf<String>()
    File("D:/term/dict").listFiles().forEach {
        println(it.canonicalPath)
        for(item in head.childrenMap.entries) {
            if(item.value.endNum != 0) {
                println(item.value)
            }
        }
        val jsonObj1 = JSONObject.fromObject(it.readText())
        val jsonArr1 = jsonObj1.getJSONArray("arr")
        for(i in 0..(jsonArr1.size - 1)) {
            val jsonObj2 = jsonArr1.getJSONObject(i)
            var id = 1
            jsonObj2.keys().forEach { id = it.toString().toInt() }
            val jsonArr2 = jsonObj2.getJSONArray("$id")
            val strList = jsonArr2.toArray().filterIndexed { index, any -> index % 2 == 0 }.map { it.toString() }
            val posList = jsonArr2.toArray().filterIndexed { index, any -> index % 2 == 1 }.map { it.toString() }
            //分词序列和词性标注序列长度应当相等
            assert(strList.size == posList.size)
            parentSet.clear()
            for(j in 0..(posList.size - 1)) {
                var node = head
                for(k in j..(posList.size - 1)) {
                    if(setAll.contains(posList[k]) || !node.childrenMap.keys.contains(posList[k])) {
                        break
                    }
                    node = node.childrenMap.get(posList[k])!!
                    //判断在当前位置是否已经到了状态转移图中可以作为结尾的词性序列位置
                    if(node.endNum != 0) {
                        val key = strList.subList(j, k +1).joinToString("","","").replace("\"","")
                        //如果不全都是汉字，则直接跳过
                        if(!key.matches("[\u4e00-\u9fa5]+".toRegex())) {
                            break
                        }
                        //当且仅当在这个法条中已经抽取的术语不等于key并且，已经抽取的术语中包含key的时候，将其加入到parentMap当中
                        parentSet.filter { parentStr -> parentStr != key && parentStr.contains(key) }.forEach { it1 ->
                            parentMap.putIfAbsent(key, mutableSetOf())
                            parentMap.get(key)!!.add(it1)
                        }
                        parentSet.add(key)
                        //词频统计
                        frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1)
                        termLenMap.putIfAbsent(key, node.append.substring(1).split("_").size)
                        var str = ""
                        if(specialSet.contains(key)) {
                            println()
                        }
                        if(j != 0 && strList[j - 1].isNotBlank() && strList[j - 1] != "\r\n" && strList[j - 1] != "\n") {
                            str = strList[j - 1].replace("\r\n","").replace("\n","").replace("\r","")
                            contextMap.putIfAbsent(str, mutableMapOf())
                            val n = contextMap.get(str)!!.getOrDefault(key, 0)
                            contextMap.get(str)!!.put(key, n + 1)
                        }
                        if(k != posList.size - 1 && strList[k + 1].isNotBlank() && strList[k + 1] != "\r\n" && strList[k + 1] != "\n") {
                            str = strList[k + 1].replace("\r\n","").replace("\n","").replace("\r","")
                            contextMap.putIfAbsent(str, mutableMapOf())
                            val n = contextMap.get(str)!!.getOrDefault(key, 0)
                            contextMap.get(str)!!.put(key, n + 1)
                        }
                        appendMap.putIfAbsent(node.append.substring(1), mutableSetOf())
                        appendMap.get(node.append.substring(1))!!.add(key)
                    }
                }
            }
        }
    }
    val freqFile = File("D:/term/freqFile.txt")
    freqFile.delete()
    freqFile.createNewFile()
    var writer = BufferedWriter(FileWriter(freqFile))
    frequencyMap.forEach { k, v ->  writer.write("$k\t$v\r\n")}
    writer.close()
    val parentFile = File("D:/term/parentTerm.txt")
    parentFile.delete()
    parentFile.createNewFile()
    writer = BufferedWriter(FileWriter(parentFile))
    parentMap.forEach { k, v -> writer.write("$k\r\n${v.joinToString("\t","","")}\r\n") }
    writer.close()
    var noParentFile = File("D:/term/noParentTerm.txt")
    noParentFile.delete()
    noParentFile.createNewFile()
    writer = BufferedWriter(FileWriter(noParentFile))
    val keys = frequencyMap.keys
    println("keys' length = ${keys.size}")
    keys.removeAll(parentMap.keys)
    println("keys' length = ${keys.size}")
    keys.forEach { writer.write("$it\r\n") }
    writer.close()
    var termLenFile = File("D:/term/termLenFile.txt")
    termLenFile.delete()
    termLenFile.createNewFile()
    writer = BufferedWriter(FileWriter(termLenFile))
    termLenMap.forEach { k, v -> writer.write("$k\t$v\r\n") }
    writer.close()
    var contextFile = File("D:/term/context.txt")
    contextFile.delete()
    contextFile.createNewFile()
    writer = BufferedWriter(FileWriter(contextFile))
    println(contextMap.values.toList().flatMap { it.keys }.toSet().size)
    contextMap.forEach { k, v -> writer.write("$k^^^${v.toList().joinToString("\t","","").replace("\r\n","").replace("\n","")}\r\n")}
    writer.close()
    val dir = File("D:/term/appendDir")
    dir.deleteRecursively()
    dir.mkdir()
    appendMap.forEach { k, v ->
        val file = File("D:/term/appendDir/$k.txt")
        writer = BufferedWriter(FileWriter(file))
        v.forEach { writer.write("$it\r\n") }
        writer.close()
    }
    println("end")
}
