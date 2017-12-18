import java.io.File

/**
 * Created by serc1730 on 2017/12/15.
 */
//用于去除原用户词典中大量存在的重复词
fun main(args: Array<String>) {
    val set = mutableSetOf<String>()
    File("D:/lawDict1.txt").forEachLine { set.add(it) }
    val file = File("D:/lawDict2.txt")
    file.delete()
    file.createNewFile()
    set.forEach {
        file.appendText("$it\n")
    }
    //打印实际加入的不重复的用户词的数量
    println(set.size)
}
