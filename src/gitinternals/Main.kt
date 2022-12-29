package gitinternals

fun main() {
    val pathToGit = promptForString("Enter .git directory location:")

    when (promptForString("Enter command:")) {
        "cat-file" -> catFile(pathToGit)
        "list-branches" -> listBranches(pathToGit)
        "log" -> printLog(pathToGit)
        else -> println("Unknown command")
    }
}

fun catFile(pathToGit: String) {
    val hash = promptForString("Enter git object hash:")
    val gitFile = GitFile(pathToGit, hash)
    println("*${gitFile.gitType}*")
    println(GitCatFile(gitFile).formatBody())
}

fun listBranches(pathToGit: String) {
    GitBranches(pathToGit).printBranches()
}

fun printLog(pathToGit: String) {
    val branch = promptForString("Enter branch name:")
    GitLog(pathToGit).printLog(branch)
}
