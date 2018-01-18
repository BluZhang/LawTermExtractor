package com.pku.law.term.value

import com.pku.law.term.preprocess.createTransferMap
import com.pku.law.term.preprocess.prepareDict
import net.sf.json.JSONObject
import java.io.*

/**
 * Created by serc1730 on 2017/12/27.
 */
//用于记录术语集合中包含key术语的所有value术语。如：“人民代表”包含在“全国人民代表大会”当中
val parentMap = mutableMapOf<String, MutableSet<String>>()
//用于记录术语key出现的频率value
val frequencyMap = mutableMapOf<String, Int>()
//用于记录不包含在任何其他的候选术语中的术语
val noParentSet = mutableSetOf<String>()
//用于记录术语term的长度（字的数量）
val termLenMap = mutableMapOf<String, Int>()
//用于记录术语key的cValue值
val cValueMap = mutableMapOf<String, Double>()
//用于记录了术语key的ncValue值
val ncValueMap = mutableMapOf<String, Double>()
//用于记录上下文环境词汇。key为上下文词汇，value为上下文词汇所伴随的术语词汇。如：对于"全国人民代表大会"，若分词为“全国 人民代表 大会”，则对于术语“人民代表”，“全国”、“人民”是其上下文词汇。
val contextMap = mutableMapOf<String, MutableMap<String, Int>>()
//用于记录一个术语的上下文环境词汇，key为术语，value为术语中伴随出现的上下文词汇。
val termContextMap = mutableMapOf<String, MutableMap<String, Int>>()
//用于记录某个模式，如n_n下的所有候选术语

fun main(args: Array<String>) {
    //由于哈工大LTP和之前NLPIR的体系不兼容，因此，parentMap、frequencyMap、noParentSet、termLenMap这些需要自己写函数进行实现。
    calculateCValue()
    calculateNCValue()
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
