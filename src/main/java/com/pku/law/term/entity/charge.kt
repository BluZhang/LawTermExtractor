package com.pku.law.term.entity

import net.sf.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset

/**
 * Created by serc1730 on 2017/12/29.
 */

//罪名字符串集合
val chargeMap = mutableMapOf<String, Int>()
fun main(args: Array<String>) {
    chargeSetInit()
    countDictUsed()
}

fun chargeSetInit() {
    val sb = StringBuilder()
    val file = File("D:/term/charge.txt")
    file.forEachLine {
        if(it.endsWith("罪")) {
            sb.append(it)
            chargeMap.put(sb.toString().replace("\n","").replace("\r\n",""),0)
            sb.delete(0, sb.length)
        }
        else if (it.isNotBlank()){
            sb.append(it)
        }
    }
}

fun countDictUsed(): Unit  {
    val dir = File("D:/term/dict")
    val fileList = dir.listFiles()
    //存储用户词典使得分词效果发生了改变的词条数量
    var count = 0
    for(i in 1..fileList.size) {
        val file1 = File("D:/term/dict/$i.txt")
//        println(file1.absolutePath)
        val jsonObj1 = JSONObject.fromObject(file1.readText())
        val jsonArr1 = jsonObj1.getJSONArray("arr")
        for(j in 0..(jsonArr1.size - 1)) {
            val jsonObj12 = jsonArr1.getJSONObject(j)
            var id1 = 1
            jsonObj12.keys().forEach { id1 = it.toString().toInt() }
            val jsonArr12 = jsonObj12.getJSONArray("$id1")
            var term = jsonArr12.filterIndexed { index, any ->  index % 2 == 0}.joinToString("","","")
            term = term.replace("犯罪", "omg").replace("无罪","omg").replace("罪犯","omg").replace("罪证","omg").replace("有罪","omg").replace("两款罪","").replace("三款罪","").replace("畏罪","")
                    .replace("之罪","").replace("论罪","").replace("轻罪","").replace("重罪","").replace("罪行","").replace("定罪","")
            if(term.contains("罪")) {
                var bool = false
                chargeMap.keys.forEach { it1 ->  if(term.contains(it1)) { bool = true; chargeMap.put(it1, chargeMap.get(it1)!! + 1) } }
            }
        }
    }
    println(chargeMap.size)
    val outFile = File("D:/term/chargeUsed.txt")
    outFile.delete()
    outFile.createNewFile()
    val writer = BufferedWriter(FileWriter(outFile))
    chargeMap.asSequence().filter { it.value > 0 }.forEach { writer.write("${it.key}\t\t${it.value}\r\n")}
    writer.close()
}
