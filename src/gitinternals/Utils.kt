package gitinternals

const val SPACE_BYTE = 32.toByte()
const val NULL_STRING = "\u0000"
const val NULL_BYTE = 0.toByte()

data class Wrapper<T> (var value: T)

fun promptForString(prompt: String): String {
    println(prompt)
    return readln()
}

//fun toHex(input: String): String {
//    val sb = StringBuilder()
//
//    for (chunk in input.chars()) {
//        sb.append(String.format("%02x", chunk.toInt()))
//    }
//
//    return sb.toString()
//}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun stringUpToStopByte(body: ByteArray, index: Wrapper<Int>, stopByte: Byte): String {
    val sb = StringBuilder()

    while (index.value < body.size && body[index.value] != stopByte) {
        sb.append(body[index.value].toInt().toChar())
        index.value++
    }

    index.value++

    return sb.toString()
}

fun stringUpToSpace(body: ByteArray, index: Wrapper<Int>): String = stringUpToStopByte(body, index, SPACE_BYTE)

fun stringUpToNull(body: ByteArray, index: Wrapper<Int>): String = stringUpToStopByte(body, index, NULL_BYTE)

fun main() {
    val body = "tree 4e249073dd59af17b3d09d65fb34256b5232fcca\n" +
            "parent 2c2eb0082f757ebff6716a2feb4a7abce004b712\n" +
            "parent 0000000000000000000000000000000000000000\n" +
            "author Knute Snortum <knute@snortum.net> 1670887039 -0800\n" +
            "committer Knute Snortum <knute@snortum.net> 1670956397 -0800\n" +
            "\n" +
            "Minor cleanup\n" +
            "foo bar\n"
    val m = Regex("^tree ([a-f0-9]{40})\$", RegexOption.MULTILINE).find(body)
    // val sha = m!!.groups[1]?.value
    val sha = m!!.destructured.component1()
    println("tree: $sha")

    val m1 = Regex("^parent ([a-f0-9]{40})\$", RegexOption.MULTILINE).findAll(body)
    // val sha1 = m1.asIterable().map { it.destructured.component1() }.joinToString(" | " )
    val sha1 = m1.asIterable().joinToString(" | ") { it.destructured.component1() }
    if (sha1.isNotBlank()) println("parent: $sha1")

    val record = "((?:\\w+ )+)(<[^>]+>) ([\\d]{10}) ([-+]\\d{4})\$"
    // val m2 = Regex("^author ((?:\\w+ )+)(<[^>]+>) ([\\d]{10}) ([-+]\\d{4})\$", RegexOption.MULTILINE).find(body)
    val m2 = Regex("^author $record", RegexOption.MULTILINE).find(body)
    val (_, name, email, timestamp, timeZone) = m2!!.groupValues  // .destructured.toList()
    println("name: ${name.trim()}, email: $email, timestamp: $timestamp, timeZone: $timeZone")

    // val m3 = Regex("\\n\\n(.*)\\n", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)).find(body)
    val m3 = Regex("\\n\\n(.*)\\n", RegexOption.DOT_MATCHES_ALL).find(body)
    val comments = m3!!.destructured.component1()
    println("comments:")
    println(comments)
}