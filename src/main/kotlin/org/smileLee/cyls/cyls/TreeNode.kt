package org.smileLee.cyls.cyls

import sun.dc.path.*

class TreeNode {
    interface RunnerForJava {
        fun run(str: String, cyls: Cyls)
    }

    val name: String
    val runner: (String, Cyls) -> Unit

    val children = HashMap<String, TreeNode>()

    //used in kotlin
    constructor(name: String, parent: TreeNode, runner: (String, Cyls) -> Unit) {
        this.name = name
        this.runner = runner
        parent.children.put(name, this)
    }

    constructor(name: String, runner: (String, Cyls) -> Unit) {
        this.name = name
        this.runner = runner
    }

    //used in java
    constructor(name: String, parent: TreeNode?, runner: RunnerForJava) {
        this.name = name
        this.runner = { str, cyls -> runner.run(str, cyls) }
        parent?.children?.put(name, this)
    }

    constructor(name: String, runner: RunnerForJava) {
        this.name = name
        this.runner = { str, cyls -> runner.run(str, cyls) }
    }

    fun findPath(path: List<String>, index: Int = 0): TreeNode = if (index == path.size) this
    else children[path[index]]?.findPath(path, index + 1) ?: throw PathException()

    fun run(message: String, cyls: Cyls) = runner(message, cyls)
}

