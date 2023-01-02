package gitinternals

fun main() {
    val pathToGit = promptForString("Enter .git directory location:")

    when (promptForString("Enter command:")) {
        "cat-file" -> catFile(pathToGit)
        "list-branches" -> listBranches(pathToGit)
        "log" -> printLog(pathToGit)
        "commit-tree" -> commitTree(pathToGit)
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

fun commitTree(pathToGit: String) {
    val hash = promptForString("Enter commit-hash:")
    GitTree(pathToGit).printTreeInit(hash)
}