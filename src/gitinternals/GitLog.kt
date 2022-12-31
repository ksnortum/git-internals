package gitinternals

import java.io.File

class GitLog(private val pathToGit: String) {
    private val slash: String = File.separator

    fun printLog(branch: String) {
        var commitHash = getHashForThisBranch(branch)

        while (true) {
            var gitFile = GitFile(pathToGit, commitHash)
            printCommit(commitHash, "", gitFile)
            val parentMatch = Regex(GitCatFile.PARENT_REGEX, RegexOption.MULTILINE).findAll(gitFile.body)
            val parentList = parentMatch.asIterable().toList()
            when (parentList.size) {
                0 -> break
                1 -> commitHash = parentList[0].value.drop(7)
                2 -> {
                    // Print second parent
                    commitHash = parentList[1].value.drop(7)
                    gitFile = GitFile(pathToGit, commitHash)
                    printCommit(commitHash, " (merged)", gitFile)

                    // Setup first parent to be printed
                    commitHash = parentList[0].value.drop(7)
                }
            }
        }
    }

    private fun printCommit(commitHash: String, mergedMessage: String, gitFile: GitFile) {
        println("Commit: $commitHash$mergedMessage")
        val committerMatch = Regex(GitCatFile.COMMITTER_REGEX, RegexOption.MULTILINE).find(gitFile.body)
        println(GitCatFile.formatNameEmailTimestamp(committerMatch, isOriginal = false))
        val messageMatch = Regex(GitCatFile.MESSAGE_REGEX, RegexOption.DOT_MATCHES_ALL).find(gitFile.body)
        println(messageMatch!!.destructured.component1())
        println()
    }

    private fun getHashForThisBranch(branch: String): String {
        val fileName = "$pathToGit${slash}refs${slash}heads$slash$branch"
        return File(fileName).readText(Charsets.UTF_8).trim()
    }
}