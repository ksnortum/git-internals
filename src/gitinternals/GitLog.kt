package gitinternals

import java.io.File

class GitLog(private val pathToGit: String) {
    private val slash = File.separator

    fun printLog(branch: String) {
        var commitHash = getHashForThisBranch(branch)
        if (commitHash == ERROR_STRING) return

        while (true) {
            var gitObject = GitObject(pathToGit, commitHash)
            printCommit(commitHash, "", gitObject)
            val parentMatch = Regex(GitCatFile.PARENT_REGEX, RegexOption.MULTILINE).findAll(gitObject.body)
            val parentList = parentMatch.asIterable().toList()
            when (parentList.size) {
                0 -> break
                1 -> commitHash = parentList[0].value.drop(7)
                2 -> {
                    // Print second parent
                    commitHash = parentList[1].value.drop(7)
                    gitObject = GitObject(pathToGit, commitHash)
                    printCommit(commitHash, " (merged)", gitObject)

                    // Setup first parent to be printed
                    commitHash = parentList[0].value.drop(7)
                }
            }
        }
    }

    private fun printCommit(commitHash: String, mergedMessage: String, gitObject: GitObject) {
        println("\nCommit: $commitHash$mergedMessage")
        val committerMatch = Regex(GitCatFile.COMMITTER_REGEX, RegexOption.MULTILINE).find(gitObject.body)
        println(GitCatFile.formatNameEmailTimestamp(committerMatch, isOriginal = false))
        val messageMatch = Regex(GitCatFile.MESSAGE_REGEX, RegexOption.DOT_MATCHES_ALL).find(gitObject.body)
        println(messageMatch!!.destructured.component1())
    }

    private fun getHashForThisBranch(branch: String): String {
        val fileName = "$pathToGit${slash}refs${slash}heads$slash$branch"
        val file = File(fileName)

        if (!file.exists()) {
            println("Could not find branch ($fileName)")
            return ERROR_STRING
        }

        return file.readText(Charsets.UTF_8).trim()
    }
}