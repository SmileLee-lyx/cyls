package org.smileLee.cyls.cyls

import sun.dc.path.*

class TreeNode {
    interface RunnerForJava {
        fun run(str: String)
    }

    val name: String
    val runner: (String) -> Unit

    val children = HashMap<String, TreeNode>()

    //used in kotlin
    constructor(name: String, parent: TreeNode, runner: (String) -> Unit) {
        this.name = name
        this.runner = runner
        parent.children.put(name, this)
    }

    constructor(name: String, runner: (String) -> Unit) {
        this.name = name
        this.runner = runner
    }

    //used in java
    constructor(name: String, parent: TreeNode, runner: RunnerForJava) {
        this.name = name
        this.runner = { runner.run(it) }
        parent.children.put(name, this)
    }

    constructor(name: String, runner: RunnerForJava) {
        this.name = name
        this.runner = { runner.run(it) }
    }

    fun run(message: String) {
        runner(message)
    }

    fun findPath(path: List<String>, index: Int = 0): TreeNode {
        return if (index == path.size) this
        else children[path[index]]?.findPath(path, index + 1) ?: throw PathException()
    }
}

