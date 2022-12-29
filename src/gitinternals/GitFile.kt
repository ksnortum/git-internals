package gitinternals

import java.io.File
import java.io.FileInputStream
import java.util.zip.InflaterInputStream
import kotlin.properties.Delegates

class GitFile(private val pathToGit: String, private val hash: String) {
    lateinit var type: Type
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
        val wholeFile = bytes.decodeToString() // Doesn't decode some binary chars correctly
        val header = wholeFile.substringBefore(NULL_STRING)
        body = wholeFile.substringAfter(NULL_STRING)
        val parts = header.split(" ")
        type = Type.valueOf(parts[0].uppercase())
        length = parts[1].toInt()
        val splitPoint = bytes.indexOfFirst { x -> x == NULL_BYTE }
        bodyBytes = bytes.copyOfRange(splitPoint + 1, bytes.size)
    }
}