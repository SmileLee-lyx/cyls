package org.smileLee.cyls.cyls

class MatchingVerifier(
        val nodes: ArrayList<VerifyNode>
) {
    fun findAndRun(string: String, cyls: Cyls) = nodes.any {
        it.verifyAndRun(string, cyls)
    }
}

interface VerifyNode {
    fun matches(string: String, cyls: Cyls): Boolean
    fun verifyAndRun(string: String, cyls: Cyls): Boolean
}

interface PathNode : VerifyNode {
    val children: ArrayList<VerifyNode>
}

class EqualPathNode(
        val string: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String, cyls: Cyls) = if (string == this.string) {
        children.any { it.matches(string, cyls) }
    } else false

    override fun verifyAndRun(string: String, cyls: Cyls) = if (string == this.string) {
        children.any { it.verifyAndRun(string, cyls) }
    } else false
}

class ContainPathNode(
        val subString: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String, cyls: Cyls) = if (string.contains(subString)) {
        children.any { it.matches(string, cyls) }
    } else false

    override fun verifyAndRun(string: String, cyls: Cyls) = if (string.contains(subString)) {
        children.any { it.verifyAndRun(string, cyls) }
    } else false
}

class RegexPathNode(
        val regex: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String, cyls: Cyls) = if (string.matches(regex.toRegex())) {
        children.any { it.matches(string, cyls) }
    } else false

    override fun verifyAndRun(string: String, cyls: Cyls) = if (string.matches(regex.toRegex())) {
        children.any { it.verifyAndRun(string, cyls) }
    } else false
}

class ContainRegexPathNode(
        val regex: String,
        override val children: ArrayList<VerifyNode>
) : PathNode {
    override fun matches(string: String, cyls: Cyls) = if (string.contains(regex.toRegex())) {
        children.any { it.matches(string, cyls) }
    } else false

    override fun verifyAndRun(string: String, cyls: Cyls) = if (string.contains(regex.toRegex())) {
        children.any { it.verifyAndRun(string, cyls) }
    } else false
}

interface RunnerNode : VerifyNode {
    val runner: (String, Cyls) -> Unit
    override fun verifyAndRun(string: String, cyls: Cyls): Boolean {
        return if (matches(string, cyls)) {
            runner(string, cyls)
            true
        } else false
    }
}

class AnyRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String, cyls: Cyls) = children.any { it.matches(string, cyls) }
}

class AllRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String, cyls: Cyls) = children.all { it.matches(string, cyls) }
}

class EqualNode(
        val string: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string == this.string
}

class ContainNode(
        val subString: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.contains(subString)
}

class RegexNode(
        val regex: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.matches(regex.toRegex())
}

class ContainRegexNode(
        val regex: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.contains(regex.toRegex())
}

class DefaultRunnerNode(
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = true
}