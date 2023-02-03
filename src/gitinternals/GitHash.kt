package gitinternals

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class GitHash(private val pathToGit: String) {
    private val slash = File.separator

    fun getHash(prompt: String): String {
        val hash = promptForString(prompt)
        if (hash == BACK_STRING) return BACK_STRING
        val subdirectory = hash.take(2)
        val hashFileName = hash.drop(2)
        val files = mutableListOf<String>()

        try {
            Files.newDirectoryStream(
                Path.of(pathToGit, "objects", subdirectory),
                "${hashFileName}*"
            ).use { dirStream -> dirStream.forEach { files.add(it.toString()) } }
        } catch (e: IOException) {
            println("Could not find $hash (may need more characters)")
            return ERROR_STRING
        }

        if (files.size == 0) {
            println("Could not find $hash (may need more characters)")
            return ERROR_STRING
        } else if (files.size > 1) {
            println("You must enter more characters of the hash")
            return ERROR_STRING
        }

        return subdirectory + files[0].substringAfterLast(slash)
    }
}
