package com.pku.law.term.extract

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created by serc1730 on 2018/1/12.
 */

val set = mutableSetOf<String>()

fun main(args: Array<String>) {
    fileMerge()
//    modelWriter("n_n")
//    modelWriter("vn_n")
//    modelWriter("n_n_n")
//    modelWriter("n_vn")
//    modelWriter("n_vn_vn")
//    modelWriter("a_n")
//    modelWriter("n_vn_n")
    File("D:/term/cand/vn_n").forEachLine {
        if(it.isNotBlank() && set.contains(it.split(" ")[0])) {
            set.remove(it.split(" ")[0])
        }
    }
    File("D:/term/termExtract/vn_n").forEachLine {
        if(it.isNotBlank()) {
            set.add(it)
        }
    }
    File("D:/term/termExtracted.txt").delete()
    val writer = BufferedWriter(FileWriter(File("D:/term/termExtracted.txt")))
    File("D:/term/dirDictSeq").listFiles().forEach {
        if(it.name.isNotBlank()) {
            set.add(it.name.replace(".txt",""))
        }
    }
    set.forEach { writer.write("$it\r\n") }
    writer.close()
}

fun modelWriter(model: String) {
    val writer = BufferedWriter(FileWriter(File("D:/term/termExtract/$model")))
    File("D:/term/cand/$model").forEachLine {
        if(it.isNotBlank() && set.contains(it.split(" ")[0])) {
            writer.write("${it.split(" ")[0]}\r\n")
        }
    }
    writer.close()
}

fun fileMerge() {
    File("D:/term/term").listFiles().forEach { file ->
        file.forEachLine { line ->
            if(line.isNotBlank() && line.matches("[\u4e00-\u9fa5]+".toRegex())) {
                set.add(line)
            }
        }
    }
    val writer = BufferedWriter(FileWriter(File("D:/term/termExtracted.txt")))
    set.forEach { writer.write("$it\r\n") }
    writer.close()
}
