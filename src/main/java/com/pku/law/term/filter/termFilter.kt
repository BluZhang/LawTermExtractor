package com.pku.law.term.filter

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created by serc1730 on 2018/1/5.
 */

fun main(args: Array<String>) {
    val set = mutableSetOf<String>()
    val file = File("D:/term/correct.txt")
    file.forEachLine { line ->
        if(line.isNotBlank()) set.add(line)
    }
    file.delete()
    val writer = BufferedWriter(FileWriter(file))
    set.forEach { writer.write("$it\r\n") }
    writer.close()
}
