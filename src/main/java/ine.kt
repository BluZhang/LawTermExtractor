import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * Created by serc1730 on 2017/12/28.
 */
object ine {
    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {
        val reader = BufferedReader(FileReader(File("")))
        var line = ""
        line = reader.readLine()
        while (line != null) {
            println(line)
            try {
                line = reader.readLine()
            }
            catch (e: Exception) {

            }
        }
        reader.close()
    }
}
