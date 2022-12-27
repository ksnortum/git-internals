package gitinternals

enum class Type(text: String) {
    COMMIT("commit"),
    BLOB("blob"),
    TREE("tree")
}