package gitinternals

const val SPACE_BYTE = 32.toByte()
const val NULL_STRING = "\u0000"
const val NULL_BYTE = 0.toByte()

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

fun stringUpToStopByte(body: ByteArray, indexIn: Int, stopByte: Byte): Pair<String, Int> {
    val sb = StringBuilder()
    var index = indexIn

    while (index < body.size && body[index] != stopByte) {
        sb.append(body[index].toInt().toChar())
        index++
    }

    index++

    return Pair(sb.toString(), index)
}

fun stringUpToSpace(body: ByteArray, index: Int): Pair<String, Int> = stringUpToStopByte(body, index, SPACE_BYTE)

fun stringUpToNull(body: ByteArray, index: Int): Pair<String, Int> = stringUpToStopByte(body, index, NULL_BYTE)

//data class Wrapper<T> (val value: T)
