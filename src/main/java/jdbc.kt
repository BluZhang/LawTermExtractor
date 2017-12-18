import net.sf.json.JSONArray
import net.sf.json.JSONObject
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList

//达梦数据库基本参数及配置
const val driver = "dm.jdbc.driver.DmDriver"
const val url = "jdbc:dm://192.168.200.64:5678"
const val username="SYSDBA"
const val password="123123123"
//从加入用户词典之后的分词结果中查询信息
const val dictSql="select ID, SEGMENTATION from TF_IDF.SEG_LQ order by ID"
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
//    createConnection()
//    itemWrite()
//    prepareDict()
//    countDictUsed()
//    modeDetection()
//    termOmmit()
//    modelPOSSeq()
    createTransferMap()
}

//识别术语可能存在的词性序列模式
fun modeDetection() {
    val dir = File("D:/term/dirDictChanged")
    val resDir = File("D:/term/dirDictSeq")
    resDir.deleteRecursively()
    resDir.mkdir()
    dir.listFiles().forEach {
        val termName = it.name.substringBefore(".")
        val set = mutableSetOf<String>()
//        File("D:/term/dirDictChanged/行政审判.txt").forEachLine { line ->
        it.forEachLine { line ->
            val len1List = mutableListOf<Int>()
            var len = 0;
            val line1 = line.replace("[","").replace("]","")
            val sb = StringBuilder()
            line1.split(",").forEachIndexed { index, s ->
                if(index % 2 == 0) {
                    //截止s之前的字符串序列的长度之和
                    len1List.add(len)
                    val str = s.replace("\"", "")
                    len += str.length
                    sb.append(str)
                }
            }
//            val termIndex = sb.toString().indexOf("行政审判")
            val termIndex = sb.toString().indexOf(termName)
            var startIndex = 0
            var endIndex = 0
            len1List.forEachIndexed { index, i ->
                if(i == termIndex) {
                    startIndex = index
                }
                if(i == termIndex + termName.length) {
                    endIndex = index - 1
                }
            }
            val list = mutableListOf<String>()
            line1.split(",").forEachIndexed { index, s ->
                if(index >= startIndex * 2 && index <= endIndex * 2 + 1) {
                    list.add(s.replace("\"", ""))
                }
            }
            val list1 = list.filterIndexed { index, s ->  index % 2 == 0 }
            val resStr = list1.joinToString("","","")
            if(resStr.equals(termName)) {
                set.add(list.toString())
            }
        }
        set.forEach { File("D:/term/dirDictSeq/$termName.txt").appendText("$it\n") }
    }
}

//将术语的词性序列模式总结并添加到文件当中
fun modelPOSSeq() {
    val dir = File("D:/term/dirDictSeq")
    val map = mutableMapOf<String, Int>()
    val posDir = File("D:/term/posDir")
    posDir.deleteRecursively()
    posDir.mkdir()
    dir.listFiles().forEach {
        it.forEachLine { line ->
            val line1 = line.replace("[","").replace("]","")
            val sb = StringBuilder("")
            line1.split(", ").forEachIndexed { index, s ->
                if(index % 2 == 1) {
                    sb.append("_$s")
                }
            }
            File("D:/term/posDir/${sb.toString().substring(1)}.txt").appendText("$line.s\n")
            map.put(sb.toString(), map.getOrDefault(sb.toString(), 0) + 1)
        }
    }
    val file = File("D:/term/modelSeqSorted.txt")
    file.delete()
    file.createNewFile()
    val set = map.entries.sortedByDescending { it.value }
    set.sortedBy { it.key }.forEach {
        file.appendText("${it.key.substring(1)}\t${it.value}\n")
    }
}


//由于出现了提取前后的术语数量不统一，这里找到不统一的术语集合
fun termOmmit() {
    val beforeDir = File("D:/term/dirDictChanged")
    val afterDir = File("D:/term/dirDictSeq")
    val beforeList = beforeDir.listFiles().map { it.name.substringBefore(".") }
    val afterList = afterDir.listFiles().map { it.name.substringBefore(".") }
    val beforeArrList = ArrayList<String>()
    beforeArrList.addAll(beforeList)
    val afterArrList = ArrayList<String>()
    afterArrList.addAll(afterList)
    beforeArrList.removeAll(afterArrList)
    beforeArrList.forEach { println(it) }

}

//查找用户词典中被使用过的用户词
fun countDictUsed() {
    val dir = File("D:/term/$dictDir")
    val fileList = dir.listFiles()
    //存储用户词典使得分词效果发生了改变的词条数量
    var count = 0
    val dirDictChanged = "D:/term/dirDictChanged"
    File(dirDictChanged).deleteRecursively()
    File(dirDictChanged).mkdir()
    for(i in 1..fileList.size) {
        val file1 = File("D:/term/$dictDir/$i.txt")
        val file2 = File("D:/term/$noDictDir/$i.txt")
        println(file1.absolutePath)
        val jsonObj1 = JSONObject.fromObject(file1.readText())
        val jsonObj2 = JSONObject.fromObject(file2.readText())
        val jsonArr1 = jsonObj1.getJSONArray("arr")
        val jsonArr2 = jsonObj2.getJSONArray("arr")
        for(j in 0..(jsonArr1.size - 1)) {
            val jsonObj12 = jsonArr1.getJSONObject(j)
            val jsonObj22 = jsonArr2.getJSONObject(j)
            var id1 = 1
            jsonObj12.keys().forEach { id1 = it.toString().toInt() }
            var id2 = 1
            jsonObj22.keys().forEach { id2 = it.toString().toInt() }
            assert(id1 == id2)
            val jsonArr12 = jsonObj12.getJSONArray("$id1")
            val jsonArr22 = jsonObj22.getJSONArray("$id2")
            if(!jsonArr12.toString().equals(jsonArr22.toString())) {
                for(str in setAll) {
                    jsonArr12.toArray().forEach {
                        if(it.toString().equals(str)) {
                            //寻找只在用户词典分词结果中出现的用户词。
                            var flag = false
                            jsonArr22.toArray().forEach { it2 ->
                                //如果用户词典中的词在加入分词结果前后都出现了，那么，设置flag为true
                                if(it2.toString().equals(str)) {
                                    flag = true
                                }
                            }
                            //如果用户词只在加入用户词典之后的粉刺结果中出现，则将其加入mapOccur当中`
                            if(!flag) {
                                mapOccur.put(str, mapOccur.getOrDefault(str, 0) + 1)
                                val file = File("${dirDictChanged}/${str}.txt")
                                if(!file.exists()) {
                                    file.createNewFile()
                                }
//                                file.appendText("$jsonArr12\n")
                                file.appendText("$jsonArr22\n")
                            }
                        }
                    }
                }
                count++
            }
        }
    }
    val dictList = mutableListOf<Pair<String, Int>>()
    //将出现过的用户词及频率加入到dictList当中
    mapOccur.forEach { key, value ->
        dictList.add(Pair(key, value))
    }
    val output = File("D:/term/dictUsedMap.txt")
    output.delete()
    output.createNewFile()
    //根据用户词在分词结果中出现的频率进行排序
    dictList.sortByDescending { it.second }
    //将出现过的用户词分词写入到文件当中
    dictList.forEach {
        output.appendText("${it.first}  ${it.second}\n")
    }
    println("user dict changed: $count")
    println("occur size: ${mapOccur.size}")
    println("all dict size: ${setAll.size}")
}

//预先将所有的用户词典单词加载到内存当中
fun prepareDict() {
    File("D:/lawDict1.txt").readText().split("\n").forEach {
        setAll.add(it)
    }
}

//将数据库查询结果写入到文件当中。type用于标识，加入用户词典后的结果和未加入用户词典的结果在不同的文件目录之下。
fun addToFile(rs: ResultSet, type: String) {
    val dir = File("D:/term/$type")
    if(!dir.exists()) dir.mkdir()
    var index = 1
    val list = mutableListOf<String>()
    while (rs.next()) {
        val jsonArr = JSONArray()
        for(i in 1..100) {
            if(!rs.next()) break
            val jsonArrElem = JSONArray()
            val str = rs.getString("SEGMENTATION").trim()
            val id = rs.getInt("ID")
            str.split(" ").forEach{ list.addAll(it.split("/")) }
            jsonArrElem.addAll(list)
            list.clear()
            val jsonObj2 = JSONObject()
            jsonObj2.put("$id", jsonArrElem)
            jsonArr.add(jsonObj2)
        }
        val jsonObj = JSONObject()
        jsonObj.put("arr", jsonArr)
        File("D:/term/$type/${index}.txt").writeText(jsonObj.toString())
        index++
    }

}

//从数据库中读取分词结果，并分别将加入用户词典前后的分词结果写入到文件当中
fun itemWrite() {
    //预编译dictSql语句
    val pstmt1 = conn!!.prepareStatement(dictSql)
    val rs1 = pstmt1.executeQuery()
    val dir = File("D:/term")
    if(!dir.exists()) dir.mkdir()
    //将加入用户词典之后的分词结果，写入文件当中
    addToFile(rs1, dictDir)
    rs1.close()
    pstmt1.close()
    //预编译noDictSql语句
    val pstmt2 = conn!!.prepareStatement(noDictSql)
    val rs2 = pstmt2.executeQuery()
    //将未加入用户词典的分词结果，写入文件当中
    addToFile(rs2, noDictDir)
    rs2.close()
    pstmt2.close()
    //关闭数据库连接
    conn!!.close()
}

var head: Node = Node("start", mutableMapOf<String, Node>())

//创建术语序列的状态转移图
fun createTransferMap() {
    File("D:/term/modelSeqSorted.txt").forEachLine {
        val splits = it.split("\t")
        var node = head
        splits[0].split("_").forEach { it1 ->
            node.childrenMap.put(it1, node.childrenMap.getOrDefault(it1, Node(it1, mutableMapOf<String, Node>())))
            node = node.childrenMap.get(it1)!!
        }
        node.endNum = splits[1].toInt()
    }
    backIterNode(head)
    println(head.totalSubNum)
}

fun backIterNode(node: Node) {
    node.childrenMap.forEach { key, value ->
        backIterNode(value)
    }
    node.totalSubNum = node.endNum + node.childrenMap.values.sumBy { it.totalSubNum }
}

//创建数据库连接
fun createConnection(): Unit {
    Class.forName(driver)
    conn = DriverManager.getConnection(url, username, password)
}

/*
 * str：表示该节点的词性
 * endNum：表示以该节点结束的词性序列的数量
 * totalSubNum：表示以该节点以及该节点的子节点等所有词性序列的数量总数
 */
class Node(val str: String, val childrenMap: MutableMap<String, Node>, var endNum: Int = 0, var totalSubNum: Int = 0)
