package gitinternals

import java.io.File
import java.util.zip.InflaterInputStream
import java.io.FileInputStream

fun main() {
    val pathToGit = promptForString("Enter .git directory location:")
    val hash = promptForString("Enter git object hash:")
    val iis = openGitFile(pathToGit, hash)
    printTypeAndLength(iis)
}

fun promptForString(prompt: String): String {
    println(prompt)
    return readln()
}

fun openGitFile(pathToGit: String, hash: String): InflaterInputStream {
    val subdirectory = hash.take(2)
    val hashFileName = hash.drop(2)
    val slash = File.separator
    val fis = FileInputStream("$pathToGit${slash}objects$slash$subdirectory$slash$hashFileName")
    return InflaterInputStream(fis)
}

fun printTypeAndLength(iis: InflaterInputStream) {
    val header = getFileHeader(iis)
    val (type, length) = header.split(" ")
    println("type:$type length:$length")
}

fun getFileHeader(iis: InflaterInputStream): String {
    var result = ""

    while (iis.available() == 1) {
        val thisChar = iis.read().toChar().toString()
        if (thisChar == "\u0000") break
        result += thisChar
    }

    return result
}