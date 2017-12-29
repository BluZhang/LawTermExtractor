package com.pku.law.term.readerWriter

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset

/**
 * Created by serc1730 on 2017/12/24.
 */

fun main(args: Array<String>) {
    File("D:/term/Yes.txt").forEachLine(charset = Charset.forName("GB2312")) {
        if(it.isNotBlank()) {
            println(it)
        }
    }
}