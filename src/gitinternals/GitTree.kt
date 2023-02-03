package gitinternals

import java.io.File
import java.lang.IllegalArgumentException

const val HASH_LENGTH = 40
const val TREE_PLUS_SPACE = 5

class GitTree(private val pathToGit: String) {
    private val slash: String = File.separator

    fun printTreeInit(commitHash: String) {
        val gitFile = GitFile(pathToGit, commitHash)

        if (gitFile.body.take(TREE_PLUS_SPACE) == "tree ") {
            printTree(gitFile.body.substring(TREE_PLUS_SPACE, HASH_LENGTH + TREE_PLUS_SPACE))
        } else {
            throw IllegalArgumentException("Hash $commitHash is not a commit")
        }
    }

    private fun printTree(startHash: String, pathSoFar: String = "") {
        var gitFile = GitFile(pathToGit, startHash)
        if (gitFile.gitType != GitType.TREE) throw IllegalArgumentException("Hash must point to tree")
        val treeString = GitCatFile(gitFile).formatTree(gitFile.bodyBytes)
        println()

        for (treeElement in treeString.trim().split("\n")) {
            val (_, hash, fileName) = treeElement.split(" ")
            val fileToPrint = if (pathSoFar.isEmpty()) fileName else "$pathSoFar$slash$fileName"
            gitFile = GitFile(pathToGit, hash)

            when (gitFile.gitType) {
                GitType.TREE -> printTree(hash, fileToPrint)
                GitType.BLOB -> println(fileToPrint)
                GitType.COMMIT -> throw IllegalStateException("A commit file is illegal here")
            }
        }
    }
}