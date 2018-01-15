package com.pku.law.term.excel

import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream

/**
 * Created by serc1730 on 2018/1/15.
 */

/**
 * 为了方便标注，将所有的分词结果处理成excel格式，方便拆分。
 */

fun main(args: Array<String>) {
    File("D:\\term\\法律文本分词及依存分析").listFiles().forEach { file ->
        println(file.name)
        var index = 0
        var lineNum = 0
        var id = ""
        var orig = ""
        var split = ""
        var workbook: HSSFWorkbook = HSSFWorkbook()
        var seq = 0
        var sheet = workbook.createSheet("${file.name}$seq")
        var pair: Pair<HSSFSheet, Int>
        var row: HSSFRow
        var preId = 0
        file.forEachLine { line ->
            if(line.startsWith("------") && (line.replace("------","").toIntOrNull() != null)) {
                id=line.replace("------","")
                index = 0
            }
            else {
                when(index % 3) {
                    0 -> { orig = line; index++ }
                    1 -> { split = line; index++ }
                    2 -> {
                        index++
                        var splitList: List<String> = split.split("^$")
                        if(lineNum <= 30000 && splitList.size < 255) {
                            row = sheet.createRow(lineNum++)
                            if(preId != id.toInt()) {
                                row.createCell(0).setCellValue(id)
                                row = sheet.createRow(lineNum++)
                                preId = id.toInt()
                            }
                            row.createCell(0).setCellValue(orig)
                            row = sheet.createRow(lineNum++)
                            var splitList: List<String> = split.split("^$")
                            splitList.forEachIndexed { ind, s -> row.createCell(ind).setCellValue(s.split("^^")[0]) }
                            lineNum++
                        }
                    }
                }
            }
        }
        val outFile = File("D:\\term\\excel\\${file.name.replace(".txt","")}.xls")
        var fileOutputStream = FileOutputStream(outFile)
        workbook.write(fileOutputStream)
        fileOutputStream.close()
    }
}

fun toExcelFile(sheet: HSSFSheet, strList: List<String>, index: Int) {
    var row = sheet.createRow(index)
    strList.forEachIndexed { index, s -> row.getCell(index).setCellValue(s) }
}

