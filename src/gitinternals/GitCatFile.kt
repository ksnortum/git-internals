package gitinternals

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GitCatFile(private val gitFile: GitFile) {
    companion object {
        const val SHA_LENGTH = 20
        const val SHA_REGEX = "([a-f0-9]{40})\$"
        const val TREE_REGEX = "^tree $SHA_REGEX"
        const val PARENT_REGEX = "^parent $SHA_REGEX"
        const val RECORD_REGEX = "((?:\\w+ )+)(<[^>]+>) ([\\d]{10}) ([-+]\\d{4})\$"
        const val AUTHOR_REGEX = "^author $RECORD_REGEX"
        const val COMMITTER_REGEX = "^committer $RECORD_REGEX"
        const val MESSAGE_REGEX = "\\n\\n(.*)\\n"

        fun formatNameEmailTimestamp(matcher: MatchResult?, isOriginal: Boolean): String {
            val (_, name, email, timestamp, timeZone) = matcher!!.groupValues
            val sb = StringBuilder(name) // name has a space at the end
            sb.append("${email.trim('<', '>')} ")
            sb.append(if (isOriginal) "original timestamp: " else "commit timestamp: ")
            sb.append(formatTimestamp(timestamp, timeZone))

            return sb.toString()
        }

        private fun formatTimestamp(timestamp: String, timeZone: String): String {
            val instant = Instant.ofEpochSecond(timestamp.toLong())
            val date = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
            return formatter.format(date)
        }

    }

    fun formatBody(): String {
        return when (gitFile.gitType) {
            GitType.BLOB -> gitFile.body
            GitType.COMMIT -> formatCommit(gitFile.body)
            GitType.TREE -> formatTree(gitFile.bodyBytes)
        }
    }

    private fun formatCommit(body: String): String {
        val sb = StringBuilder()

        val treeMatch = Regex(TREE_REGEX, RegexOption.MULTILINE).find(body)
        sb.append("tree: ${treeMatch!!.destructured.component1()}")

        val parentMatch = Regex(PARENT_REGEX, RegexOption.MULTILINE).findAll(body)
        val parentSha = parentMatch.asIterable().joinToString(" | ") { it.destructured.component1() }
        if (parentSha.isNotBlank()) sb.append("\nparents: $parentSha")

        val authorMatch = Regex(AUTHOR_REGEX, RegexOption.MULTILINE).find(body)
        sb.append("\nauthor: " + formatNameEmailTimestamp(authorMatch, isOriginal = true))

        val committerMatch = Regex(COMMITTER_REGEX, RegexOption.MULTILINE).find(body)
        sb.append("\ncommitter: " + formatNameEmailTimestamp(committerMatch, isOriginal = false))

        sb.append("\ncommit message:")
        val messageMatch = Regex(MESSAGE_REGEX, RegexOption.DOT_MATCHES_ALL).find(body)
        sb.append("\n${messageMatch!!.destructured.component1()}\n")

        return sb.toString()
    }

    fun formatTree(body: ByteArray): String {
        val sb = StringBuilder()
        val bodyIndex = Wrapper(0)

        while (bodyIndex.value < body.size) {
            val permission = stringUpToSpace(body, bodyIndex)
            val fileName = stringUpToNull(body, bodyIndex)
            val hexSha = body.copyOfRange(bodyIndex.value, bodyIndex.value + SHA_LENGTH).toHex()
            bodyIndex.value += SHA_LENGTH
            sb.append("$permission $hexSha $fileName\n")
        }

        return sb.toString()
    }
}
