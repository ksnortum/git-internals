package gitinternals

import java.io.File
import java.lang.IllegalArgumentException

const val HASH_LENGTH = 40
const val TREE_PLUS_SPACE = 5

class GitTree(private val pathToGit: String) {
    private val slash: String = File.separator

    fun printTreeInit(commitHash: String) {
        val gitObject = GitObject(pathToGit, commitHash)

        if (gitObject.body.take(TREE_PLUS_SPACE) == "tree ") {
            printTree(gitObject.body.substring(TREE_PLUS_SPACE, HASH_LENGTH + TREE_PLUS_SPACE))
        } else {
            throw IllegalArgumentException("Hash $commitHash is not a commit")
        }
    }

    private fun printTree(startHash: String, pathSoFar: String = "") {
        var gitObject = GitObject(pathToGit, startHash)
        if (gitObject.gitType != GitType.TREE) throw IllegalArgumentException("Hash must point to tree")
        val treeString = GitCatFile(gitObject).formatTree(gitObject.bodyBytes)
        println()

        for (treeElement in treeString.trim().split("\n")) {
            val (_, hash, fileName) = treeElement.split(" ")
            val fileToPrint = if (pathSoFar.isEmpty()) fileName else "$pathSoFar$slash$fileName"
            gitObject = GitObject(pathToGit, hash)

            when (gitObject.gitType) {
                GitType.TREE -> printTree(hash, fileToPrint)
                GitType.BLOB -> println(fileToPrint)
                GitType.COMMIT -> throw IllegalStateException("A commit file is illegal here")
            }
        }
    }
}