package gitinternals

import java.io.File
import java.lang.IllegalArgumentException

const val EXIT_STRING = "exit"
const val ERROR_STRING = "error"
const val BACK_STRING = "back"

class Runner {
    private var defaultPathToGit = ""

    fun run() {
        while (true) {
            var pathToGit: String
            var fileDoesNotExist: Boolean

            do {
                pathToGit = promptForString("Enter the .git directory location, or \"exit\": ", defaultPathToGit)
                if (pathToGit == EXIT_STRING) return
                defaultPathToGit = pathToGit
                fileDoesNotExist = !File(pathToGit).exists()

                if (fileDoesNotExist) {
                    println("File could not be found")
                    defaultPathToGit = ""
                }
            } while (fileDoesNotExist)

            while (true) {
                when (promptForString("\nEnter a command (number or name):\n" +
                        "1) cat-file\n" +
                        "2) list-branches\n" +
                        "3) log\n" +
                        "4) commit-tree\n" +
                        "0) back")) {
                    "cat-file", "1" -> catFile(pathToGit)
                    "list-branches", "2" -> listBranches(pathToGit)
                    "log", "3" -> printLog(pathToGit)
                    "commit-tree", "4" -> commitTree(pathToGit)
                    "back", "0" -> {
                        println()
                        break
                    }
                    else -> println("Unknown command")
                }
            }
        }
    }

    private fun catFile(pathToGit: String) {
        var hash: String
        do {
            hash = GitHash(pathToGit).getHash("Enter as few as 3 characters of the object hash, or \"back\":")
        } while (hash == ERROR_STRING)

        if (hash == BACK_STRING) return

        val gitObject = GitObject(pathToGit, hash)
        println("\n${gitObject.gitType}")
        print(GitCatFile(gitObject).formatBody())
    }

    private fun listBranches(pathToGit: String) {
        GitBranches(pathToGit).printBranches()
    }

    private fun printLog(pathToGit: String) {
        val branch = promptForString("Enter branch name:")
        GitLog(pathToGit).printLog(branch)
    }

    private fun commitTree(pathToGit: String) {
        var tryAgain = false
        do {
            var hash: String
            do {
                hash = GitHash(pathToGit).getHash("Enter as few as 3 characters of the commit hash, or \"back\":")
            } while (hash == ERROR_STRING)

            if (hash == BACK_STRING) return

            try {
                GitTree(pathToGit).printTreeInit(hash)
            } catch (e: IllegalArgumentException) {
                println(e.message)
                tryAgain = true
            }
        } while (tryAgain)
    }
}