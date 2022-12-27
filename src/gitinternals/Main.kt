package gitinternals

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val pathToGit = promptForString("Enter .git directory location:")
    val hash = promptForString("Enter git object hash:")
    val gitFile = GitFile(pathToGit, hash)
    println("*${gitFile.type}*")
    println(formatBody(gitFile))
}

fun promptForString(prompt: String): String {
    println(prompt)
    return readln()
}

fun formatBody(gitFile: GitFile): String {
    return when (gitFile.type) {
        Type.BLOB -> gitFile.body
        Type.COMMIT -> formatCommit(gitFile)
        Type.TREE -> gitFile.body
    }
}

fun formatCommit(gitFile: GitFile): String {
    val sb = StringBuilder()

    val lines = gitFile.body.split("\n").toMutableList()
    var parts = lineSplitter(lines[0])
    if (parts[0] == "tree") sb.append(formatTree(parts, lines))

    parts = lineSplitter(lines[0])
    if (parts[0] == "parent") sb.append(formatParents(parts, lines))

    parts = lineSplitter(lines[0])
    if (parts[0] == "author") sb.append("\nauthor: " + formatEmailTimestamp(parts, lines))

    parts = lineSplitter(lines[0])
    if (parts[0] == "committer") sb.append("\ncommitter: " + formatEmailTimestamp(parts, lines))

    if (lines[0].isBlank()) {
        sb.append("\ncommit message:")
        lines.removeAt(0)

        while (lines[0].isNotBlank()) {
            sb.append("\n${lines[0]}")
            lines.removeAt(0)
        }
    }

    return sb.toString()
}

fun lineSplitter(line: String): List<String> {
    return line.split(" ", limit = 2)
}

fun formatTree(parts: List<String>, lines: MutableList<String>): String {
    lines.removeAt(0)
    return "${parts[0]}: ${parts[1]}"
}

fun formatParents(parts: List<String>, lines: MutableList<String>): String {
    val sb = StringBuilder("\nparents: ${parts[1]}")
    lines.removeAt(0)
    val items = lineSplitter(lines[0])

    if (items.size > 1 && items[0] == "parent") {
        sb.append(" | ${items[1]}")
        lines.removeAt(0)
    }

    return sb.toString()
}

fun formatEmailTimestamp(parts: List<String>, lines: MutableList<String>): String {
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
    lines.removeAt(0)

    return sb.toString()
}

fun formatTimestamp(timestamp: String, timeZone: String): String {
    val instant = Instant.ofEpochSecond(timestamp.toLong())
    val date = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone))
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
    return formatter.format(date)
}