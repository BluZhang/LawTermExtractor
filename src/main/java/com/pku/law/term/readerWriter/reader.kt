package com.pku.law.term.readerWriter

import java.io.File

/**
 * Created by serc1730 on 2017/12/24.
 */

fun main(args: Array<String>) {
    File("D:/term/candidates.txt").forEachLine {
        println(it)
    }
}