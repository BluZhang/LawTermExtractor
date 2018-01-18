package com.pku.law.term

import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by serc1730 on 2018/1/18.
 */

object Config {
//    private val input = FileInputStream(File("config.properties"))
    private val classLoader = this.javaClass.classLoader
    private val input = classLoader.getResourceAsStream("config.properties")
    private val properties = Properties()

    //ciku.kt文件中使用
    /*
     * sogou_ciku_dir 搜狗细胞词库存放的目录
     * sogou_ciku_output 抽取到的细胞词库所写入的目标文件
     * importer_py_path 外部调用的python脚本的存放路径
     */
    public var sogou_ciku_dir = ""
    public var sogou_ciku_output = ""
    public var importer_py_path = ""

    public var charge_path = ""

    public var excel_out_dir = ""
    public var excel_in_dir = ""

    public var user_dict_dir = ""

    public var pos_mode_dir = ""
    public var pos_mode_wordseq_out = ""
    public var pos_mode_seq_out = ""
    public var pos_mode_sorted_seq = ""
    public var pos_dot_file_path = ""


    init {
        properties.load(input)
        sogou_ciku_dir = properties.getProperty("sogou_ciku_dir")
        sogou_ciku_output = properties.getProperty("sogou_ciku_output")
        importer_py_path = properties.getProperty("importer_py_path")

        charge_path = properties.getProperty("charge_path")


        excel_out_dir = properties.getProperty("excel_out_dir")
        excel_in_dir = properties.getProperty("excel_in_dir")

        user_dict_dir = properties.getProperty("user_dict_dir")

        pos_mode_dir = properties.getProperty("pos_mode_dir")
        pos_mode_wordseq_out = properties.getProperty("pos_mode_wordseq_out")
        pos_mode_seq_out = properties.getProperty("pos_mode_seq_out")
        pos_mode_sorted_seq = properties.getProperty("pos_mode_sorted_seq")
        pos_dot_file_path = properties.getProperty("pos_dot_file_path")
    }

}

fun main(args: Array<String>) {
    println(Config.sogou_ciku_dir)
}

