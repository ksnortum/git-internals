package gitinternals

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GitCatFile(private val gitFile: GitFile) {
    fun formatBody(): String {
        return when (gitFile.gitType) {
            GitType.BLOB -> gitFile.body
            GitType.COMMIT -> formatCommit(gitFile.body)
            GitType.TREE -> formatTree(gitFile.bodyBytes)
        }
    }

    private fun formatCommit(body: String): String {
        val sb = StringBuilder()
        val lines = body.split("\n").toMutableList()
        var lineIndex = 0

        var parts = lines[lineIndex].split(" ", limit = 2)
        sb.append("tree: ${parts[1]}")
        lineIndex++

        parts = lines[lineIndex].split(" ", limit = 2)
        if (parts[0] == "parent") {
            sb.append("\nparents: ${parts[1]}")
            lineIndex++
        }

        // May be second parent if this is a merge commit
        parts = lines[lineIndex].split(" ", limit = 2)
        if (parts[0] == "parent") {
            sb.append(" | ${parts[1]}")
            lineIndex++
        }

        parts = lines[lineIndex].split(" ", limit = 2)
        sb.append("\nauthor: " + formatEmailTimestamp(parts))
        lineIndex++

        parts = lines[lineIndex].split(" ", limit = 2)
        sb.append("\ncommitter: " + formatEmailTimestamp(parts))
        lineIndex++

        if (lines[lineIndex].isBlank()) {
            sb.append("\ncommit message:")
            lineIndex++

            while (lineIndex < lines.size && lines[lineIndex].isNotBlank()) {
                sb.append("\n${lines[lineIndex]}")
                lineIndex++
            }
        }

        return sb.toString()
    }

    private fun formatEmailTimestamp(parts: List<String>): String {
        val items = parts[1].split("\\s+".toRegex())
        val timeZone = items.last()
        val timestamp = items[items.size - 2]
        val email = items[items.size - 3]
        val sb = StringBuilder()

        // Name could have many parts
        for (i in 0 until items.size - 3) {
            sb.append("${items[i]} ")
        }

        sb.append("${email.trim('<', '>')} ")
        sb.append(if (parts[0] == "author") "original timestamp: " else "commit timestamp: ")
        sb.append(formatTimestamp(timestamp, timeZone))

        return sb.toString()
    }

    private fun formatTimestamp(timestamp: String, timeZone: String): String {
        val instant = Instant.ofEpochSecond(timestamp.toLong())
        val date = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        return formatter.format(date)
    }

    private fun formatTree(body: ByteArray): String {
        val sb = StringBuilder()
        var bodyIndex = 0

        while (bodyIndex < body.size) {
            val (permission, index) = stringUpToSpace(body, bodyIndex)
            bodyIndex = index
            val (fileName, index1) = stringUpToNull(body, bodyIndex)
            bodyIndex = index1
            val hexSha = body.copyOfRange(bodyIndex, bodyIndex + 20).toHex()
            bodyIndex += 20
            sb.append("$permission $hexSha $fileName\n")
        }

        return sb.toString()
    }
}
