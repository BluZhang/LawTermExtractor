package com.pku.law.term.ciku

import java.io.*

/**
 * Created by serc1730 on 2017/12/27.
 */
fun main(args: Array<String>) {
    val file = File("D:/term/ciku.txt")
    file.delete()
    file.createNewFile()
    var writer = BufferedWriter(FileWriter(file))
    File("D:/term/ciku").listFiles().forEach { file ->
        val reader = BufferedReader(InputStreamReader(Runtime.getRuntime().exec("python D:/term/importer.py $file").inputStream))
        reader.forEachLine { line -> writer.write("$line\r\n") }
        reader.close()
    }
    writer.close()
    val set = mutableSetOf<String>()
    file.forEachLine { set.add(it) }
    file.delete()
    File("D:/lawDict2.txt").forEachLine { set.add(it) }
    writer = BufferedWriter(FileWriter(file))
    set.forEach { writer.write("$it") }
    println(set.size)
    writer.close()
}
