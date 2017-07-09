package org._2333.cyls.cyls

import sun.dc.path.*

class TreeNode(val name: String, parent: TreeNode?, val runner: (String) -> Unit) {

    val children = HashMap<String, TreeNode>()

    init {
        parent?.children?.put(name, this)
    }

    fun run(message: String) {
        runner(message)
    }

    fun findPath(path: List<String>, index: Int = 0): TreeNode {
        return if (index == path.size) this
        else children[path[index]]?.findPath(path, index + 1) ?: throw PathException()
    }
}

