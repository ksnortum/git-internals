package gitinternals

import java.io.File

const val EXIT_STRING = "exit"
const val ERROR_STRING = "error"

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
                when (promptForString("\nEnter command\n" +
                        "1) cat-file\n" +
                        "2) list-branches\n" +
                        "3) log\n" +
                        "4) commit-tree\n" +
                        "0) back")) {
                    "cat-file", "1" -> catFile(pathToGit)
                    "list-branches", "2" -> listBranches(pathToGit)
                    "log", "3" -> printLog(pathToGit)
                    "commit-tree", "4" -> commitTree(pathToGit)
                    "back", "0" -> break
                    else -> println("Unknown command")
                }
            }
        }
    }

    private fun catFile(pathToGit: String) {
        val hash = promptForString("Enter git object hash:")
        val gitFile = GitFile(pathToGit, hash)
        println("\n${gitFile.gitType}")
        print(GitCatFile(gitFile).formatBody())
    }

    private fun listBranches(pathToGit: String) {
        GitBranches(pathToGit).printBranches()
    }

    private fun printLog(pathToGit: String) {
        val branch = promptForString("Enter branch name:")
        GitLog(pathToGit).printLog(branch)
    }

    private fun commitTree(pathToGit: String) {
        val hash = promptForString("Enter commit-hash:")
        GitTree(pathToGit).printTreeInit(hash)
    }
}