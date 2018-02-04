package com.pku.law.term.dependency

import net.sf.json.JSONArray
import net.sf.json.JSONObject

fun main(args: Array<String>) {
    val wordSplits = mutableListOf<String>()
    val posSplits = mutableListOf<String>()
    Str.wordSplits.split("^$").map { val splits = it.split("^^"); wordSplits.add(splits[0]); posSplits.add(splits[1])}
    val parentMap = mutableMapOf<Int, Pair<Int, String>>()
    Str.dependency.split("^$").forEach { val splits = it.split("^^"); parentMap.put(splits[0].toInt(), Pair(splits[1].toInt(), splits[2]))}
//    println(parentMap)
    val data = JSONArray()
    val root = JSONObject()
    root.put("id",0)
    root.put("word","ROOT")
    root.put("tag","ROOT")
    root.put("level",0)
    data.add(root)
    wordSplits.forEachIndexed { index, s ->
        val obj = JSONObject()
        obj.put("id", index +1)
        obj.put("word", s)
        obj.put("tag", posSplits[index])
        val pair = parentMap.get(index + 1)
        obj.put("parent", pair?.first)
        obj.put("dependency", pair?.second)
        obj.put("level", 1)
        data.add(obj)
    }
    println("data=$data")
}