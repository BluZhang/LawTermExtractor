package com.pku.law.term.ciku

import com.pku.law.term.Config
import java.io.*

/**
 * 用于从搜狗的细胞词库中抽取法律术语。
 * 网上已经有抽取搜狗细胞词库中术语的python代码，本部分是通过在java中直接调用系统的python命令，
 * 实现从细胞词库中抽取术语。
 * 搜狗细胞词库官网中，有法律相关的术语抽取，可以直接拿来使用。
 * 请自行去搜狗输入法官网上下载
 * Created by serc1730 on 2017/12/27.
 */
fun main(args: Array<String>) {
    //输出文件。用于保存所有从搜狗细胞词库中抽取得到的术语
    val file = File(Config.sogou_ciku_output)
    //为了避免和之前的文件内容冲突，先将原来文件删除，然后重新创建
    file.delete()
    file.createNewFile()
    var writer = BufferedWriter(FileWriter(file))
    //用于保存从细胞词库中抽取到的术语。由于考虑到，不同的细胞词库中可能有术语发生重合，此处用于无重复的存放术语
    val itemSet = mutableSetOf<String>()
    //sogou_ciku_dir表示搜狗细胞词库文件存放的目录，分别遍历每个目录，抽取其中的术语并存放到itemSet当中
    File(Config.sogou_ciku_dir).listFiles().forEach { file ->
        //调用系统python脚本进行处理。
        val reader = BufferedReader(InputStreamReader(Runtime.getRuntime().exec("python ${Config.importer_py_path} $file").inputStream))
        reader.forEachLine { line -> itemSet.add(line) }
        reader.close()
    }
    //将itemSet中的每个术语写入到文件当中
    itemSet.forEach { writer.write("$it\r\n") }
    writer.close()
}
