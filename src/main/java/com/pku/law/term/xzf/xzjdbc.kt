package com.pku.law.term.xzf

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*

//达梦数据库基本参数及配置
const val driver = "dm.jdbc.driver.DmDriver"
const val url = "jdbc:dm://192.168.200.64:5678"
const val username="SYSDBA"
const val password="123123123"
//从加入用户词典之后的分词结果中查询信息
const val lawNameTypeSql="select ID, TITLE, TYPEID from FLFGK.ZLLAW_XZ where TYPEID=44 or TYPEID=45 order by TYPEID"

const val lawItemSql="select ID, LAWID, CONTENT from FLFGK.ZLLAWCOLUMN_XZ where TYPEID=44 or TYPEID=45 order by TYPEID"
//加入词典后的分词结果存入的目录
const val dictDir = "lawNameId"
//数据库连接
var conn: Connection? = null
val lawIdMap = mutableMapOf<Int, String>()

fun main(args: Array<String>) {
    createConnection()
//    itemWrite()
    lawNameTypeInit()
    lawItemWrite()
//    lawNameChange()
}

fun lawNameTypeInit() {
//    File("D:/xzf/44.txt").forEachLine { if(it.isNotBlank()) { val splits = it.split("\t"); lawIdMap.put(splits[0].toInt(), splits[1]) } }
    File("D:/xzf/45.txt").forEachLine { if(it.isNotBlank()) { val splits = it.split("\t"); lawIdMap.put(splits[0].toInt(), splits[1]) } }
}

//将数据库查询结果写入到文件当中。type用于标识，加入用户词典后的结果和未加入用户词典的结果在不同的文件目录之下。
fun lawNameTypeAddToFile(rs: ResultSet, type: String) {
    val forbit = listOf("意见", "命令", "决定", "目录", "批复", "复函", "通知",
            "草案", "暂行规定", "国务院令第", "废止", "修改", "决议", "补充规定")
    val permit = listOf("条例", "办法", "规定", "细则", "法", "法（")
    val dir = "D:/xzf/$type"
    if(!File("$dir/44").exists()) File("$dir/44").mkdirs()
    if(!File("$dir/45").exists()) File("$dir/45").mkdirs()
    val writer44 = BufferedWriter(FileWriter("D:/xzf/44.txt"))
    val writer45 = BufferedWriter(FileWriter("D:/xzf/45.txt"))
    val map = mutableMapOf<Int, BufferedWriter>()
    map.put(44, writer44)
    map.put(45, writer45)
    var bool1 = false
    var bool2 = true
    while (rs.next()) {
        bool1 = false
        bool2 = true
        val typeId = rs.getInt("TYPEID")
        val id = rs.getInt("ID")
        val title = rs.getString("TITLE")
        //只有包含permit中的字符串并且不包含forbit中的字符串，才将该法律名称加入到文件中
        permit.forEach { if(title.contains(it)) bool1 = true }
        forbit.forEach { if(title.contains(it)) bool2 = false }
        if(bool1 && bool2) {
            map.get(typeId)!!.write("$id\t$title\r\n")
        }
    }
    map.forEach { k, v -> v.close() }
}

//创建数据库连接
fun createConnection(): Unit {
    Class.forName(driver)
    conn = DriverManager.getConnection(url, username, password)
}

fun lawNameChange() {
    val set44 = mutableSetOf<String>()
    File("D:/xzf/44.txt").forEachLine { if(!it.contains("修正")) { set44.add(it) } }
    File("D:/xzf/44.txt").delete()
    File("D:/xzf/44.txt").writeText(set44.joinToString("\r\n","",""))
    val set45 = mutableSetOf<String>()
    File("D:/xzf/45.txt").forEachLine { if(!it.contains("修正")) { set45.add(it) } }
    File("D:/xzf/45.txt").delete()
    File("D:/xzf/45.txt").writeText(set45.joinToString("\r\n","",""))
}

//从数据库中读取法律法规id、名称及对应分类
fun itemWrite() {
    //预编译dictSql语句
    val pstmt1 = conn!!.prepareStatement(lawNameTypeSql)
    val rs1 = pstmt1.executeQuery()
    if(!File("D:/xzf").exists()) File("D:/xzf").mkdir()
    if(!File("D:/xzf/lawId/44").exists()) File("D:/xzf/44").mkdir()
    if(!File("D:/xzf/lawId/45").exists()) File("D:/xzf/45").mkdir()
    //将加入用户词典之后的分词结果，写入文件当中
    lawNameTypeAddToFile(rs1, dictDir)
    rs1.close()
    pstmt1.close()
    //关闭数据库连接
    conn!!.close()
}
//从数据库中读取法律法规id、名称及对应分类
fun lawItemWrite() {
    //预编译dictSql语句
    val pstmt1 = conn!!.prepareStatement(lawItemSql)
    val rs1 = pstmt1.executeQuery()
    if(!File("D:/xzf").exists()) File("D:/xzf").mkdir()
//    if(!File("D:/xzf/44").exists()) File("D:/xzf/44").mkdir()
    if(!File("D:/xzf/45").exists()) File("D:/xzf/45").mkdir()
    //将加入用户词典之后的分词结果，写入文件当中
    lawItemAddToFile(rs1, "45")
    rs1.close()
    pstmt1.close()
    //关闭数据库连接
    conn!!.close()
}

fun lawItemAddToFile(rs: ResultSet, type: String) {
    val writerMap = mutableMapOf<String, BufferedWriter>()
    while (rs.next()) {
        val id = rs.getInt("ID")
        val lawId = rs.getInt("LAWID")
        val content = rs.getString("CONTENT")
        if(lawIdMap.keys.contains(lawId)) {
            if(!writerMap.keys.contains(lawId.toString())) { writerMap.put(lawId.toString(), BufferedWriter(FileWriter(File("D:/xzf/$type/${lawIdMap.get(lawId)}.txt")))) }
            writerMap.get(lawId.toString())!!.write("$id^^${String(Base64.getEncoder().encode(content.toByteArray(Charset.forName("UTF-8"))), Charset.forName("UTF-8"))}\r\n")
        }
    }
    writerMap.values.forEach { it.close() }
}
