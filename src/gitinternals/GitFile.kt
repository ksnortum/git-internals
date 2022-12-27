package gitinternals

import java.io.File
import java.io.FileInputStream
import java.util.zip.InflaterInputStream
import kotlin.properties.Delegates

class GitFile(private val pathToGit: String, private val hash: String) {
    lateinit var type: Type
        private set

    var initialCommit by Delegates.notNull<Boolean>()
        private set

    var length by Delegates.notNull<Int>()
        private set

    lateinit var body: String
        private set

    private val regexForParent = "^parent.*".toRegex()

    init {
        parseFileStats(openGitFile())
    }

    private fun openGitFile(): InflaterInputStream {
        val subdirectory = hash.take(2)
        val hashFileName = hash.drop(2)
        val slash = File.separator
        val fileName = "$pathToGit${slash}objects$slash$subdirectory$slash$hashFileName"
        val fis = FileInputStream(fileName)
        return InflaterInputStream(fis)
    }

    private fun parseFileStats(iis: InflaterInputStream) {
        val wholeFile = iis.readAllBytes().decodeToString()
        val (header, body) = wholeFile.split("\u0000")
        this.body = body
        val parts = header.split(" ")
        type = Type.valueOf(parts[0].uppercase())
        length = parts[1].toInt()
        initialCommit = !body.matches(regexForParent)
    }
}