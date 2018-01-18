package com.pku.law.term.entity

import com.pku.law.term.Config
import net.sf.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset

/**
 * Created by serc1730 on 2017/12/29.
 */

//罪名字符串集合
public val chargeMap = mutableMapOf<String, Int>()
fun main(args: Array<String>) {
    chargeSetInit()
}

fun chargeSetInit() {
    val sb = StringBuilder()
    val file = File(Config.charge_path)
    //将charge.txt文件中的所有罪名加载进来
    //由于输入文件问题，某些罪名可能会被拆分成多行。因此，
    //如果改行的最后一个字符是“罪”，则将StringBuilder变为String存入罪名Map
    //否则，将改行缓存到StringBuilder当中，知道遇到以“罪”结尾的词，将其整个拼接为一条罪名
    file.forEachLine {
        if(it.endsWith("罪")) {
            sb.append(it)
            chargeMap.put(sb.toString(),0)
            sb.delete(0, sb.length)
        }
        else if (it.isNotBlank()){
            sb.append(it)
        }
    }
}

