package gitinternals

const val SPACE_BYTE = 32.toByte()
const val NULL_STRING = "\u0000"
const val NULL_BYTE = 0.toByte()

data class Wrapper<T> (var value: T)

fun promptForString(prompt: String, default: String = ""): String {
    val promptWithDefault = if (default.isNotBlank()) "$prompt[$default] " else prompt
    println(promptWithDefault)
    var input = readln()
    if (input.isBlank()) input = default

    return input
}

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
