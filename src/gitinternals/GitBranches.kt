package gitinternals

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class GitBranches(private val pathToGit: String) {
    private val slash = File.separator

    fun printBranches() {
        val head = getHead()
        println()

        for (branch in getBranches().sorted()) {
            print(if (branch == head) "* " else "  ")
            println(branch)
        }
    }

    private fun getBranches():List<String> {
        val path = Path.of("$pathToGit${slash}refs${slash}heads")
        return Files.newDirectoryStream(path, "*").map { it.fileName.toString() }
    }

    private fun getHead(): String {
        val headFileName = "$pathToGit${slash}HEAD"
        val contents = File(headFileName).readText(Charsets.UTF_8)
        return contents.substringAfterLast("/").trim()
    }
}