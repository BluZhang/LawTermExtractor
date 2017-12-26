package com.pku.law.term.filter

import com.pku.law.term.preprocess.*
import net.sf.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created by serc1730 on 2017/12/19.
 */

fun main(args: Array<String>) {
    val head = createTransferMap()
    prepareDict()
    val canDir = File("D:/term/candidate")
    canDir.deleteRecursively()
    canDir.mkdir()
    val map = mutableMapOf<String, Pair<String, Int>>()
    val lastMap = mutableMapOf<String, String>()
    val modelMap = mutableMapOf<String, MutableSet<String>>()
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
            for(j in 0..(posList.size - 1)) {
                var node = head
                val sb = StringBuilder()
                for(k in j..(posList.size - 1)) {
                    if(setAll.contains(posList[k]) || !node.childrenMap.keys.contains(posList[k])) {
                        break
                    }
                    node = node.childrenMap.get(posList[k])!!
                    sb.append("_${posList[k]}")
                    //判断在当前位置是否已经到了状态转移图中可以作为结尾的词性序列位置
                    if(node.endNum != 0) {
                        val key = strList.subList(j, k +1).joinToString("","","").replace("\"","")
                        val pair = map.getOrDefault(key, Pair(strList.subList(j, k + 1).joinToString("--","","") + "->" + posList.subList(j, k + 1).joinToString("_","","").replace("\"",""), 0))
                        //如果不全都是汉字，则直接跳过
                        if(!key.matches("[\u4e00-\u9fa5]+".toRegex())) {
                            break
                        }
                        if(!jsonArr2.toString().equals(lastMap.get(key))) {
                            File("D:/term/candidate/$key.txt").appendText("$jsonArr2\r\n${jsonArr2.filterIndexed { index, any -> index % 2 == 0 }.joinToString("", "", "")}\r\n\r\n")
                            lastMap.put(key, jsonArr2.toString())
                        }
                        map.put(key, Pair(pair.first, pair.second + 1))
                        modelMap.putIfAbsent(sb.toString().substring(1), mutableSetOf())
                        modelMap.get(sb.toString().substring(1))!!.add(key)
                    }
                }
            }
        }
    }
    val resList = map.entries.sortedByDescending { it.value.second }.map { it.value }
    val resList1 = map.entries.sortedBy { it.value.second }.map { it.value }
    println(map.get("机动车驾驶证"))
    val outFile = File("D:/term/candidates.txt")
    outFile.delete()
    outFile.createNewFile()
    val writer = BufferedWriter(FileWriter(outFile))
    map.toList().sortedByDescending { it.second.second }.forEach {
        writer.write("${it.first} ${it.second.first} ${it.second.second}\n")
    }
    writer.close()
    modelMap.forEach { k1, v1 ->
        val writer1 = BufferedWriter(FileWriter(File("D:/term/cand/$k1")))
        v1.sortedByDescending { it2 -> map.get(it2)!!.second }.forEach { it1 -> writer1.write("$it1 ${map.get(it1)!!.second}\r\n") }
        writer1.close()
    }
}