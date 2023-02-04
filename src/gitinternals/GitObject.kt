package gitinternals

import java.io.FileInputStream
import java.nio.file.Path
import java.util.zip.InflaterInputStream

class GitObject(private val pathToGit: String, private val hash: String) {
    lateinit var gitType: GitType
        private set

    var length = 0
        private set

    lateinit var body: String
        private set

    lateinit var bodyBytes: ByteArray
        private set

    init {
        val fis = openGitObject()
        val iis = InflaterInputStream(fis)
        parseFileStats(iis)
        fis.close()
        iis.close()
    }

    private fun openGitObject(): FileInputStream {
        val subdirectory = hash.take(2)
        val hashFileName = hash.drop(2)
        val file = Path.of(pathToGit, "objects", subdirectory, hashFileName).toFile()
        return FileInputStream(file)
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