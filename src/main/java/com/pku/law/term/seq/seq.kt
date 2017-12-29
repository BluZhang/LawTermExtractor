package com.pku.law.term.seq

import java.io.File

/**
 * Created by serc1730 on 2017/12/26.
 */
fun main(args: Array<String>) {
    val root = File("D:/term/posCandidate")
    root.deleteRecursively()
    root.mkdir()
    File("D:/term/modelSeqSorted.txt").forEachLine {
        val dir = File("D:/term/posCandidate/${it.split("\t")[0]}")
        dir.mkdir()
        val file = File("D:/term/candidate")
        println(dir.name)
    }
}
