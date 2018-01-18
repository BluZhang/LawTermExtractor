package com.pku.law.term.preprocess

import com.pku.law.term.Config
import java.io.File


//加入用户词典后，对分词产生实际效果的用户词及出现次数
val mapOccur: MutableMap<String, Int> = mutableMapOf()
//所有用户词典中用户词的集合
val userDictAll: MutableSet<String> = mutableSetOf()

fun main(args: Array<String>) {
    modelPOSSeq()
    createTransferMap()
}


/* 识别术语可能存在的词性序列模式。
 * dir目录中，存储了所有在加入用户词典之后发生改变了的用户词，在加入该用户词典之前的分词结果。
 * 每个文件的文件名为：用户词.txt，文件中的内容存储了使用了该用户词的相应分词结果。如：
 * [中华,\n,人民,\n共和国,\n]
 * 该行存储的是整个法条，所以需要首先从法条当中找到相应的用户词，然后提取该用户词所对应的词性序列
 */
fun modeDetection() {
    val dir = File(Config.pos_mode_dir)
    val resDir = File(Config.pos_mode_wordseq_out)
    //删除输出目录
    resDir.deleteRecursively()
    resDir.mkdirs()
    dir.listFiles().forEach {
        //提取用户词的名称
        val termName = it.name.substringBefore(".")
        val set = mutableSetOf<String>()
        it.forEachLine { line ->
            //用于存储从法条开头至第index个词，共有多少个字符。用于方便定位用户词产生效果的地方。
            val len1List = mutableListOf<Int>()
            var len = 0
            //去除首尾的[和]字符，剩余的部分分别是以'，'分隔，词和对应词性相互交叉的结果。其中，偶数index表示词，奇数index表示词性
            val line1 = line.replace("[","").replace("]","")
            //用于保存整个未分词的法条。
            val sb = StringBuilder()
            line1.split(",").forEachIndexed { index, s ->
                if(index % 2 == 0) {
                    //截止s之前的字符串序列的长度之和
                    len1List.add(len)
                    //去掉所有"字符
                    val str = s.replace("\"", "")
                    len += str.length
                    sb.append(str)
                }
            }
            //查找该用户词所在的index位置
            val termIndex = sb.toString().indexOf(termName)
            //存储该用户词在加入用户词典分词之前的首Index
            var startIndex = 0
            //存储该用户词在加入用户词典分词之前的尾Index
            var endIndex = 0
            len1List.forEachIndexed { index, i ->
                if(i == termIndex) {
                    startIndex = index
                }
                if(i == termIndex + termName.length) {
                    endIndex = index - 1
                }
            }
            //将用户词的分词结果存入list。偶数index存的是词，奇数index存的是词性
            val list = mutableListOf<String>()
            line1.split(",").forEachIndexed { index, s ->
                if(index >= startIndex * 2 && index <= endIndex * 2 + 1) {
                    list.add(s.replace("\"", ""))
                }
            }
            //纯用户词组成的list。由list的偶数index所组成
            val list1 = list.filterIndexed { index, s ->  index % 2 == 0 }
            //将list变成一个字符串，第一个参数表示分隔符。表示list元素之前无分隔，第二个参数表示前缀，第三个参数表示后缀。都为""，则将分词后结果拼接，进而得到了分词前的法条
            val resStr = list1.joinToString("","","")
            if(resStr.equals(termName)) {
                set.add(list.toString())
            }
        }
        //将起作用的用户词的分词（词+词性）写入到文件当中
        set.forEach { File("${Config.pos_mode_wordseq_out}/$termName.txt").appendText("$it\n") }
    }
}

//将术语的词性序列模式总结并添加到文件当中
fun modelPOSSeq() {
    val dir = File(Config.pos_mode_wordseq_out)
    val map = mutableMapOf<String, Int>()
    val posDir = File(Config.pos_mode_seq_out)
    posDir.deleteRecursively()
    posDir.mkdir()
    dir.listFiles().forEach {
        it.forEachLine { line ->
            //删除首尾的[、]字符
            val line1 = line.substring(1, line.length - 1)
            val sb = StringBuilder("")
            line1.split(", ").forEachIndexed { index, s ->
                if(index % 2 == 1) {
                    sb.append("_$s")
                }
            }
            File("${Config.pos_mode_seq_out}/${sb.toString().substring(1)}.txt").appendText("$line\n")
            map.put(sb.toString(), map.getOrDefault(sb.toString(), 0) + 1)
        }
    }
    val file = File(Config.pos_mode_sorted_seq)
    file.delete()
    file.createNewFile()
    val set = map.entries.sortedByDescending { it.value }
    //文件中，key表示模式，value表示该模式对应的出现次数。也就是，发挥作用的用户词典中的用户词的数目
    set.sortedByDescending { it.value }.forEach {
        file.appendText("${it.key.substring(1)}\t${it.value}\n")
    }
}

//查找用户词典中被使用过的用户词。由于更改了数据来源，不再从数据库中读取，因此这部分需要重新实现。
fun countDictUsed() {
}

//预先将所有的用户词典单词加载到内存当中
fun prepareDict() {
    File(Config.user_dict_dir).readText().split("\n").forEach {
        userDictAll.add(it)
    }
}

var head: Node = Node("start", mutableMapOf<String, Node>())

//创建术语序列的状态转移图。将用户词出现的词性序列文件中的词性序列，转化成图结构，然后转化成Graphviz可以读取的dot文件
fun createTransferMap(): Node {
    File(Config.pos_mode_sorted_seq).forEachLine {
        val splits = it.split("\t")
        var node = head
        splits[0].split("_").forEach { it1 ->
            //词性标注必须为非空
            if(it1.isNotBlank()) {
                node.childrenMap.put(it1, node.childrenMap.getOrDefault(it1, Node(it1, mutableMapOf<String, Node>(),0,0,"${node.append}_$it1")))
                node = node.childrenMap.get(it1)!!
            }
        }
        node.endNum = splits[1].toInt()
    }
    backIterNode(head)
    println(head.totalSubNum)
    posSeqToGraphVizFile()
    return head
}

// 后向遍历术语的词性标注树，并更新树节点的totalSubNum属性。
fun backIterNode(node: Node) {
    node.childrenMap.forEach { key, value ->
        backIterNode(value)
    }
    node.totalSubNum = node.endNum + node.childrenMap.values.sumBy { it.totalSubNum }
}


//将术语列表的术语的词性序列生成graphviz的dot文件
fun posSeqToGraphVizFile() {
    val map = mutableMapOf<Node, String>()
    map.put(head, "n0")
    var index = 1
    fun preIterNode(node: Node) {
        node.childrenMap.values.forEach { map.put(it, "n${index++}") }
        node.childrenMap.values.forEach { preIterNode(it)}
    }
    preIterNode(head)
    val sb = StringBuilder("digraph G{\n")
    fun addToGraphVizStr(node: Node) {
        //添加该节点信息
        node.childrenMap.values.forEach { sb.append("${map.get(node)} -> ${map.get(it)} [label=\"${it.totalSubNum}\"]\n") }
        //遍历该节点的子节点，通过深度优先遍历加入节点信息
        node.childrenMap.values.forEach { addToGraphVizStr(it) }
    }
    //遍历首节点head的所有子节点，依次加入信息。
    head.childrenMap.values.forEach { addToGraphVizStr(it) }
    map.keys.filter { it.str != "start" }.forEach { sb.append("${map.get(it)} [label=\"${it.str}\"]\n") }
    sb.append("}\n")
    File(Config.pos_dot_file_path).writeText(sb.toString())
}

/*
 * str：表示该节点的词性。
 * childrenMap：用于保存该节点的词性和对应的Node。
 * endNum：表示以该节点结束的词性序列的数量。
 * totalSubNum：表示以该节点以及该节点的子节点等所有词性序列的数量总数。
 * append：表示从head到当前节点所代表的词性序列的字符串，词性之间以'_'分隔。
 */
class Node(val str: String, val childrenMap: MutableMap<String, Node>, var endNum: Int = 0, var totalSubNum: Int = 0, var append: String = "")
