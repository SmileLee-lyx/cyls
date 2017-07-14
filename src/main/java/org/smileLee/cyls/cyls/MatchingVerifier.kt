package org.smileLee.cyls.cyls

class MatchingVerifier(
        val nodes: ArrayList<VerifyNode>
) {
    fun findAndRun(string: String) {
        nodes.any {
            it.verifyAndRun(string)
        }
    }
}

interface VerifyNode {
    fun matches(string: String): Boolean
    fun verifyAndRun(string: String): Boolean
}

interface PathNode : VerifyNode {
    val children: ArrayList<VerifyNode>
}

class EqualPathNode(
        val string: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String) = if (string == this.string) {
        children.any { it.matches(string) }
    } else false

    override fun verifyAndRun(string: String) = if (string == this.string) {
        children.any { it.verifyAndRun(string) }
    } else false
}

class ContainPathNode(
        val subString: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String) = if (string.contains(subString)) {
        children.any { it.matches(string) }
    } else false

    override fun verifyAndRun(string: String) = if (string.contains(subString)) {
        children.any { it.verifyAndRun(string) }
    } else false
}

class RegexPathNode(
        val regex: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String) = if (string.matches(regex.toRegex())) {
        children.any { it.matches(string) }
    } else false

    override fun verifyAndRun(string: String) = if (string.matches(regex.toRegex())) {
        children.any { it.verifyAndRun(string) }
    } else false
}

class ContainRegexPathNode(
        val regex: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String) = if (string.contains(regex.toRegex())) {
        children.any { it.matches(string) }
    } else false

    override fun verifyAndRun(string: String) = if (string.contains(regex.toRegex())) {
        children.any { it.verifyAndRun(string) }
    } else false
}

interface RunnerNode : VerifyNode {
    val runner: (String) -> Unit
    override fun verifyAndRun(string: String): Boolean {
        return if (matches(string)) {
            runner(string)
            true
        } else false
    }
}

class AnyRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String) = children.any { it.matches(string) }
}

class AllRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String) = children.all { it.matches(string) }
}

class EqualNode(
        val string: String,
        override val runner: (String) -> Unit
) : RunnerNode {
    override fun matches(string: String) = string == this.string
}

class ContainNode(
        val subString: String,
        override val runner: (String) -> Unit
) : RunnerNode {
    override fun matches(string: String) = string.contains(subString)
}

class RegexNode(
        val regex: String,
        override val runner: (String) -> Unit
) : RunnerNode {
    override fun matches(string: String) = string.matches(regex.toRegex())
}

class ContainRegexNode(
        val regex: String,
        override val runner: (String) -> Unit
) : RunnerNode {
    override fun matches(string: String) = string.contains(regex.toRegex())
}

class DefaultRunnerNode(
        override val runner: (String) -> Unit
) : RunnerNode {
    override fun matches(string: String) = true
}