package gitinternals

fun main() {
    val pathToGit = promptForString("Enter .git directory location:")

    when (promptForString("Enter command:")) {
        "cat-file" -> catFile(pathToGit)
        "list-branches" -> listBranches(pathToGit)
        else -> println("Unknown command")
    }
}

fun catFile(pathToGit: String) {
    val hash = promptForString("Enter git object hash:")
    val gitFile = GitFile(pathToGit, hash)
    println("*${gitFile.type}*")
    println(GitCatFile(gitFile).formatBody())
}

fun listBranches(pathToGit: String) {
    GitBranches(pathToGit).printBranches()
}
