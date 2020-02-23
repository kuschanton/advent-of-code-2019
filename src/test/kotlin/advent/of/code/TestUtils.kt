package advent.of.code

import java.io.File

class TestUtils {
    companion object {
        fun readLinesFrom(fileName: String) = File("src/test/resources/task_input/$fileName").readLines()
        fun readTextFrom(fileName: String) = File("src/test/resources/task_input/$fileName").readText()
    }
}