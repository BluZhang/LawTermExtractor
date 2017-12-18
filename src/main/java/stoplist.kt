import java.io.File

/**
 * Created by serc1730 on 2017/12/15.
 */
//处理最开始的用户词典。统一用户词典的格式，如删除每行开始的多余的空白字符，以及在每个分词之后加入" n"词性标注
fun main(args: Array<String>) {
    //去除多余空白字符的文件
    val file = File("D:/lawDict1.txt")
    //加入名词词性后的用户词典文件
    val file2 = File("D:/lawDict2.txt")
    //删除原文件
    file.delete()
    //创建新文件
    file.createNewFile()
    file2.delete()
    file2.createNewFile()
    //读取源用户词典文件并进行处理
    File("D:/lawDict.txt").forEachLine {
        file.appendText("${it.trim()}\n")
        file2.appendText("${it.trim()} n\n")
    }
}
