package com.pku.law.term.map

import net.sf.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created by serc1730 on 2017/12/25.
 */
fun main(args: Array<String>) {
    val wordSet = mutableSetOf<String>()
    File("D:/term/candidates.txt").forEachLine { wordSet.add(it) }
    File("D:/term/dict").listFiles().forEach {
        val jsonArr1 = JSONObject.fromObject(it.readText()).getJSONArray("arr")
        for(index1 in 0..jsonArr1.size - 1) {
            val jsonObj1 = jsonArr1.getJSONObject(index1)
            var id = jsonObj1.keys().next().toString()
            val jsonArr2 = jsonObj1.getJSONArray(id)
            val str = jsonArr2.filterIndexed { index, any ->  index % 2 == 0 }.joinToString("","","")
//            wordSet.filter { str.contains(it) }.forEach { it1 -> wordMap.getOrDefault(it1, BufferedWriter(FileWriter(File("D:/term/candidate/$it1.txt")))).write("$jsonArr2\r\n$str\r\n\r\n") }
            wordSet.filter { str.contains(it) }.forEach { it1 ->
                if(!jsonArr2.filterIndexed { index, any ->  index % 2 == 0}.contains(it)) {
                    File("D:/term/candidate/$it1.txt").appendText("$jsonArr2\r\n$str\r\n\r\n")
                }
            }
        }
    }
}
