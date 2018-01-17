package com.pku.law.term.xzf

import com.pku.law.term.preprocess.Node
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

//达梦数据库基本参数及配置
const val driver = "dm.jdbc.driver.DmDriver"
const val url = "jdbc:dm://192.168.200.64:5678"
const val username="SYSDBA"
const val password="123123123"
//从加入用户词典之后的分词结果中查询信息
const val lawNameTypeSql="select ID, TITLE, LAWTYPE from FLFGK.ZLLAW_XZ where LAWTYPE=44 || LAWTYPE=45 order by LAWTYPE"
//从未加入用户词典的分词结果中查询信息
const val noDictSql="select ID, SEGMENTATION from TF_IDF.SEG_LQ_BEFORE order by ID"
//加入词典后的分词结果存入的目录
const val dictDir = "dict"
//未加入词典的分词结果存入的目录
const val noDictDir = "noDict"
//数据库连接
var conn: Connection? = null

//加入用户词典后，对分词产生实际效果的用户词及出现次数
val mapOccur: MutableMap<String, Int> = mutableMapOf()
//所有用户词典中用户词的集合
val setAll: MutableSet<String> = mutableSetOf()

fun main(args: Array<String>) {
    createConnection()
    itemWrite()
}

//将数据库查询结果写入到文件当中。type用于标识，加入用户词典后的结果和未加入用户词典的结果在不同的文件目录之下。
fun lawNameTypeAddToFile(rs: ResultSet, type: String) {
    val forbit = listOf("意见", "命令", "决定", "目录", "批复", "复函", "通知",
            "草案", "暂行规定", "国务院令第", "废止", "修改", "决议")
    val permit = listOf("条例", "办法", "规定", "细则", "法", "法（")
    val dir = "D:/xzf/$type"
    if(!File("$dir/44").exists()) File("$dir/44").mkdir()
    if(!File("$dir/45").exists()) File("$dir/45").mkdir()
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
