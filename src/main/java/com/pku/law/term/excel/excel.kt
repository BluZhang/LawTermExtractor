package com.pku.law.term.excel

import com.pku.law.term.Config
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

    /*
     * 将类型为type的法律分词结果转化为方便标注的excel文件。
     * excel_in_dir：用于存放法律分词结果。在该目录下面，每个type类型的文本分别放置在type目录下。
     * excel_out_dir：用于将法律分词结果存放到该目录下的type目录下。
     */
    fun writeToExcel(type: String) {
        val lawLongSet = mutableSetOf<String>()
        //读取哈工大处理完的分词结果
        File("${Config.excel_in_dir}/$type").listFiles().forEach { file ->
            var index = 0
            var lineNum = 0
            var id = ""
            var orig = ""
            var split = ""
            var workbook: HSSFWorkbook = HSSFWorkbook()
            //写入到excel的sheet当中
            var sheet = workbook.createSheet("${file.name}")
            var row: HSSFRow
            var preId = 0
            //取index
            file.forEachLine { line ->
                if (line.startsWith("------") && (line.replace("------", "").toIntOrNull() != null)) {
                    id = line.replace("------", "")
                    index = 0
                } else {
                    //按照和昊星约定好的格式，进行读取
                    when (index % 3) {
                        0 -> {
                            orig = line; index++
                        }
                        1 -> {
                            split = line; index++
                        }
                        2 -> {
                            index++
                            var splitList: List<String> = split.split("^$")
                            //如果split的分词结果大于255，则不能存放excel当中
                            if (splitList.size < 255) {
                                row = sheet.createRow(lineNum++)
                                if (preId != id.toInt()) {
                                    row.createCell(0).setCellValue(id)
                                    row = sheet.createRow(lineNum++)
                                    preId = id.toInt()
                                }
                                row.createCell(0).setCellValue(orig)
                                row = sheet.createRow(lineNum++)
                                var row1 = sheet.createRow(lineNum++)
                                var splitList: List<String> = split.split("^$")
                                splitList.forEachIndexed { ind, s -> row.createCell(ind).setCellValue(s.split("^^")[0]); row1.createCell(ind).setCellValue("o") }
                            } else {  //将包含较长分词结果的法律放入lawLongSet当中
                                lawLongSet.add(file.name)
                            }
                        }
                    }
                }
            }
            //将分词结果写入到excel当中。
            val outDir = "${Config.excel_out_dir}/$type"
            //清空原输出目录，并重新建立该目录
            File(outDir).deleteRecursively()
            File(outDir).mkdirs()
            //每个输入的分词结果，分别写入到对应名称的excel文件当中
            val outFile = File("$outDir/${file.name.replace(".txt", "")}.xls")
            var fileOutputStream = FileOutputStream(outFile)
            workbook.write(fileOutputStream)
            fileOutputStream.close()
        }
        //打印法条中分词结果过长的法条所在的法律。由于这部分法律并不多，所以建议直接从数据源中删除
        lawLongSet.forEach { println(it) }
    }
    //将lawType为44、45的法律分词结果写入到Excel格式当中
    writeToExcel("44")
    writeToExcel("45")
}

