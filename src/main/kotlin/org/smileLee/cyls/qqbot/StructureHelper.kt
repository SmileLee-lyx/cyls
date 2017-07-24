package org.smileLee.cyls.qqbot

import org.intellij.lang.annotations.Language

fun <T : QQBot<T>> createTree(name: String, runner: (String, T) -> Unit, init: TreeNode<T>.() -> Unit): TreeNode<T> {
    val treeNode = TreeNode(name, runner)
    treeNode.init()
    return treeNode
}

fun <T : QQBot<T>> createTree(init: TreeNode<T>.() -> Unit): TreeNode<T> {
    val treeNode = TreeNode<T>("") { _, _ -> }
    treeNode.init()
    return treeNode
}

fun <T : QQBot<T>> TreeNode<T>.childNode(name: String, runner: (String, T) -> Unit, init: TreeNode<T>.() -> Unit) {
    children.put(name, createTree(name, runner, init))
}

fun <T : QQBot<T>> TreeNode<T>.childNode(name: String, runner: (String, T) -> Unit) {
    children.put(name, TreeNode(name, runner))
}

fun <T : QQBot<T>> createVerifier(init: MatchingVerifier<T>.() -> Unit): MatchingVerifier<T> {
    val verifier = MatchingVerifier<T>(ArrayList())
    verifier.init()
    return verifier
}

fun <T : QQBot<T>> MatchingVerifier<T>.equal(string: String, runner: (String, T) -> Unit) {
    nodes.add(EqualNode(string, runner))
}

fun <T : QQBot<T>> MatchingVerifier<T>.contain(subString: String, runner: (String, T) -> Unit) {
    nodes.add(ContainNode(subString, runner))
}

fun <T : QQBot<T>> MatchingVerifier<T>.containRegex(@Language("RegExp") regex: String, runner: (String, T) -> Unit) {
    nodes.add(ContainRegexNode(regex, runner))
}

fun <T : QQBot<T>> MatchingVerifier<T>.regex(@Language("RegExp") regex: String, runner: (String, T) -> Unit) {
    nodes.add(RegexNode(regex, runner))
}

fun <T : QQBot<T>> MatchingVerifier<T>.default(runner: (String, T) -> Unit) {
    nodes.add(DefaultRunnerNode(runner))
}

fun <T : QQBot<T>> MatchingVerifier<T>.anyOf(init: AnyRunnerNode<T>.() -> Unit, runner: (String, T) -> Unit) {
    val node = AnyRunnerNode(ArrayList(), runner)
    node.init()
    nodes.add(node)
}

fun <T : QQBot<T>> MatchingVerifier<T>.allOf(init: AllRunnerNode<T>.() -> Unit, runner: (String, T) -> Unit) {
    val node = AllRunnerNode(ArrayList(), runner)
    node.init()
    nodes.add(node)
}

fun <T : QQBot<T>> MatchingVerifier<T>.special(matches: (String, T) -> Boolean, runner: (String, T) -> Unit) {
    nodes.add(object : RunnerNode<T> {
        override val runner = runner
        override fun matches(string: String, cyls: T) = matches(string, cyls)
    })
}

fun <T : QQBot<T>> MatchingVerifier<T>.special(verifyAndRun: (String, T) -> Boolean) {
    nodes.add(object : VerifyNode<T> {
        override fun matches(string: String, cyls: T) = false
        override fun verifyAndRun(string: String, cyls: T) = verifyAndRun(string, cyls)
    })
}

fun <T : QQBot<T>> PathNode<T>.equal(string: String, runner: (String, T) -> Unit = { _, _ -> }) {
    children.add(EqualNode(string, runner))
}

fun <T : QQBot<T>> PathNode<T>.contain(subString: String, runner: (String, T) -> Unit = { _, _ -> }) {
    children.add(ContainNode(subString, runner))
}

fun <T : QQBot<T>> PathNode<T>.containRegex(@Language("RegExp") regex: String, runner: (String, T) -> Unit = { _, _ -> }) {
    children.add(ContainRegexNode(regex, runner))
}

fun <T : QQBot<T>> PathNode<T>.regex(@Language("RegExp") regex: String, runner: (String, T) -> Unit = { _, _ -> }) {
    children.add(RegexNode(regex, runner))
}

fun <T : QQBot<T>> PathNode<T>.default(runner: (String, T) -> Unit = { _, _ -> }) {
    children.add(DefaultRunnerNode(runner))
}

fun <T : QQBot<T>> PathNode<T>.anyOf(init: AnyRunnerNode<T>.() -> Unit, runner: (String, T) -> Unit = { _, _ -> }) {
    val node = AnyRunnerNode(ArrayList(), runner)
    node.init()
    children.add(node)
}

fun <T : QQBot<T>> PathNode<T>.allOf(init: AllRunnerNode<T>.() -> Unit, runner: (String, T) -> Unit = { _, _ -> }) {
    val node = AllRunnerNode(ArrayList(), runner)
    node.init()
    children.add(node)
}

class ArrayListHelper<T> : ArrayList<T>() {
    operator fun T.unaryPlus() {
        add(this@unaryPlus)
    }
}

inline fun <reified T> arrayOf(elements: ArrayListHelper<T>.() -> Unit) =
        ArrayListHelper<T>().apply { elements() }.toTypedArray()