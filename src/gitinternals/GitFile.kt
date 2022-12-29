package gitinternals

import java.io.File
import java.io.FileInputStream
import java.util.zip.InflaterInputStream
import kotlin.properties.Delegates

class GitFile(private val pathToGit: String, private val hash: String) {
    lateinit var gitType: GitType
        private set

    var length by Delegates.notNull<Int>()
        private set

    lateinit var body: String
        private set

    lateinit var bodyBytes: ByteArray
        private set

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
        val bytes = iis.readAllBytes()
        // TODO test below
        val chars: CharArray = bytes.map { it.toInt().toChar() }.toCharArray()
        val headerString: String = chars.toString().substringBefore(NULL_STRING)
        val nullIndex: Int = chars.indexOfFirst { x -> x == '\u0000' }
        val bodyChar: CharArray = chars.copyOfRange(nullIndex + 1, chars.size)
        // TODO test above
        val wholeFile = bytes.decodeToString() // Doesn't decode some binary chars correctly
        val header = wholeFile.substringBefore(NULL_STRING)
        body = wholeFile.substringAfter(NULL_STRING)
        val parts = header.split(" ")
        gitType = GitType.valueOf(parts[0].uppercase())
        length = parts[1].toInt()
        val splitPoint = bytes.indexOfFirst { x -> x == NULL_BYTE }
        bodyBytes = bytes.copyOfRange(splitPoint + 1, bytes.size)
    }
}