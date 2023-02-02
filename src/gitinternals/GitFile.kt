package gitinternals

import java.io.File
import java.io.FileInputStream
import java.util.zip.InflaterInputStream

class GitFile(private val pathToGit: String, private val hash: String) {
    private val slash = File.separator

    lateinit var gitType: GitType
        private set

    var length = 0
        private set

    lateinit var body: String
        private set

    lateinit var bodyBytes: ByteArray
        private set

    init {
        val fis = openGitFile()
        val iis = InflaterInputStream(fis)
        parseFileStats(iis)
        fis.close()
        iis.close()
    }

    private fun openGitFile(): FileInputStream {
        val subdirectory = hash.take(2)
        val hashFileName = hash.drop(2)
        val fileName = "$pathToGit${slash}objects$slash$subdirectory$slash$hashFileName"
        return FileInputStream(fileName)
    }

    private fun parseFileStats(iis: InflaterInputStream) {
        val bytes = iis.readAllBytes()
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