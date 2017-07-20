package org.smileLee.cyls.cyls

import org.intellij.lang.annotations.*

fun createTree(name: String, runner: (String, Cyls) -> Unit, init: TreeNode.() -> Unit): TreeNode {
    val treeNode = TreeNode(name, runner)
    treeNode.init()
    return treeNode
}

fun createTree(init: TreeNode.() -> Unit): TreeNode {
    val treeNode = TreeNode("") { _, _ -> }
    treeNode.init()
    return treeNode
}

fun TreeNode.childNode(name: String, runner: (String, Cyls) -> Unit, init: TreeNode.() -> Unit) {
    children.put(name, createTree(name, runner, init))
}

fun TreeNode.childNode(name: String, runner: (String, Cyls) -> Unit) {
    children.put(name, TreeNode(name, runner))
}

fun createVerifier(init: MatchingVerifier.() -> Unit): MatchingVerifier {
    val verifier = MatchingVerifier(ArrayList())
    verifier.init()
    return verifier
}

fun MatchingVerifier.equal(string: String, runner: (String, Cyls) -> Unit) {
    nodes.add(EqualNode(string, runner))
}

fun MatchingVerifier.contain(subString: String, runner: (String, Cyls) -> Unit) {
    nodes.add(ContainNode(subString, runner))
}

fun MatchingVerifier.containRegex(@Language("RegExp") regex: String, runner: (String, Cyls) -> Unit) {
    nodes.add(ContainRegexNode(regex, runner))
}

fun MatchingVerifier.regex(@Language("RegExp") regex: String, runner: (String, Cyls) -> Unit) {
    nodes.add(RegexNode(regex, runner))
}

fun MatchingVerifier.default(runner: (String, Cyls) -> Unit) {
    nodes.add(DefaultRunnerNode(runner))
}

fun MatchingVerifier.equalPath(string: String, init: EqualPathNode.() -> Unit) {
    val node = EqualPathNode(string, ArrayList())
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.containPath(subString: String, init: ContainPathNode.() -> Unit) {
    val node = ContainPathNode(subString, ArrayList())
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.regexPath(@Language("RegExp") regex: String, init: RegexPathNode.() -> Unit) {
    val node = RegexPathNode(regex, ArrayList())
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.containRegexPath(@Language("RegExp") regex: String, init: ContainRegexPathNode.() -> Unit) {
    val node = ContainRegexPathNode(regex, ArrayList())
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.anyOf(init: AnyRunnerNode.() -> Unit, runner: (String, Cyls) -> Unit) {
    val node = AnyRunnerNode(ArrayList(), runner)
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.allOf(init: AllRunnerNode.() -> Unit, runner: (String, Cyls) -> Unit) {
    val node = AllRunnerNode(ArrayList(), runner)
    node.init()
    nodes.add(node)
}

fun MatchingVerifier.special(matches: (String, Cyls) -> Boolean, runner: (String, Cyls) -> Unit) {
    nodes.add(object : RunnerNode {
        override val runner = runner
        override fun matches(string: String, cyls: Cyls) = matches(string, cyls)
    })
}

fun MatchingVerifier.special(verifyAndRun: (String, Cyls) -> Boolean) {
    nodes.add(object : VerifyNode {
        override fun matches(string: String, cyls: Cyls) = false
        override fun verifyAndRun(string: String, cyls: Cyls) = verifyAndRun(string, cyls)
    })
}

fun PathNode.equal(string: String, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    children.add(EqualNode(string, runner))
}

fun PathNode.contain(subString: String, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    children.add(ContainNode(subString, runner))
}

fun PathNode.containRegex(@Language("RegExp") regex: String, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    children.add(ContainRegexNode(regex, runner))
}

fun PathNode.regex(@Language("RegExp") regex: String, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    children.add(RegexNode(regex, runner))
}

fun PathNode.default(runner: (String, Cyls) -> Unit = { _, _ -> }) {
    children.add(DefaultRunnerNode(runner))
}

fun PathNode.equalPath(string: String, init: EqualPathNode.() -> Unit) {
    val node = EqualPathNode(string, ArrayList())
    node.init()
    children.add(node)
}

fun PathNode.containPath(subString: String, init: ContainPathNode.() -> Unit) {
    val node = ContainPathNode(subString, ArrayList())
    node.init()
    children.add(node)
}

fun PathNode.regexPath(@Language("RegExp") regex: String, init: RegexPathNode.() -> Unit) {
    val node = RegexPathNode(regex, ArrayList())
    node.init()
    children.add(node)
}

fun PathNode.containRegexPath(@Language("RegExp") regex: String, init: ContainRegexPathNode.() -> Unit) {
    val node = ContainRegexPathNode(regex, ArrayList())
    node.init()
    children.add(node)
}

fun PathNode.anyOf(init: AnyRunnerNode.() -> Unit, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    val node = AnyRunnerNode(ArrayList(), runner)
    node.init()
    children.add(node)
}

fun PathNode.allOf(init: AllRunnerNode.() -> Unit, runner: (String, Cyls) -> Unit = { _, _ -> }) {
    val node = AllRunnerNode(ArrayList(), runner)
    node.init()
    children.add(node)
}