package org.smileLee.cyls.qqbot

import sun.dc.path.PathException

class TreeNode<T : QQBot<T>> {
    interface RunnerForJava<T : QQBot<T>> {
        fun run(str: String, qqBot: T)
    }

    val name: String
    val runner: (String, T) -> Unit

    val children = HashMap<String, TreeNode<T>>()

    //used in kotlin
    constructor(name: String, parent: TreeNode<T>, runner: (String, T) -> Unit) {
        this.name = name
        this.runner = runner
        parent.children.put(name, this)
    }

    constructor(name: String, runner: (String, T) -> Unit) {
        this.name = name
        this.runner = runner
    }

    //used in java
    constructor(name: String, parent: TreeNode<T>?, runner: RunnerForJava<T>) {
        this.name = name
        this.runner = { str, qqBot -> runner.run(str, qqBot) }
        parent?.children?.put(name, this)
    }

    constructor(name: String, runner: RunnerForJava<T>) {
        this.name = name
        this.runner = { str, cyls -> runner.run(str, cyls) }
    }

    fun findPath(path: List<String>, index: Int = 0): TreeNode<T> = if (index == path.size) this
    else children[path[index]]?.findPath(path, index + 1) ?: throw PathException()

    fun run(message: String, qqBot: T) = runner(message, qqBot)
}

