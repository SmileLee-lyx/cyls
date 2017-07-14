package org.smileLee.cyls.cyls

import org.intellij.lang.annotations.*

fun createTree(name: String, runner: (String) -> Unit, init: TreeNode.() -> Unit): TreeNode {
    val treeNode = TreeNode(name, runner)
    treeNode.init()
    return treeNode
}

fun TreeNode.childNode(name: String, runner: (String) -> Unit, init: TreeNode.() -> Unit) {
    children.put(name, createTree(name, runner, init))
}

fun TreeNode.childNode(name: String, runner: (String) -> Unit) {
    children.put(name, TreeNode(name, runner))
}

fun createVerifier(init: MatchingVerifier.() -> Unit): MatchingVerifier {
    val verifier = MatchingVerifier(ArrayList())
    verifier.init()
    return verifier
}

fun MatchingVerifier.regex(@Language("RegExp") regex: String, runner: (String) -> Unit) {
    nodes.add(MatchingVerifier.RegexNode(regex, runner))
}