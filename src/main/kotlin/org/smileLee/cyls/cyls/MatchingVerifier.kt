package org.smileLee.cyls.cyls

class MatchingVerifier(
        val nodes: ArrayList<VerifyNode>
) {
    fun findAndRun(string: String, cyls: Cyls) = nodes.any {
        it.verifyAndRun(string, cyls)
    }

    operator fun VerifyNode.unaryPlus() {
        nodes.add(this@unaryPlus)
    }
}

interface VerifyNode {
    fun matches(string: String, cyls: Cyls): Boolean
    fun verifyAndRun(string: String, cyls: Cyls): Boolean
}

interface PathNode : VerifyNode {
    val children: ArrayList<VerifyNode>
}

interface RunnerNode : VerifyNode {
    val runner: (String, Cyls) -> Unit
    override fun verifyAndRun(string: String, cyls: Cyls) = if (matches(string, cyls)) {
        runner(string, cyls)
        true
    } else false
}

open class AnyRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String, cyls: Cyls) = children.any { it.matches(string, cyls) }
}

open class AllRunnerNode(
        override val children: ArrayList<VerifyNode>,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode, PathNode {
    override fun matches(string: String, cyls: Cyls) = children.all { it.matches(string, cyls) }
}

open class EqualNode(
        val string: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string == this.string
}

open class ContainNode(
        val subString: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.contains(subString)
}

open class RegexNode(
        val regex: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.matches(regex.toRegex())
}

open class ContainRegexNode(
        val regex: String,
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = string.contains(regex.toRegex())
}

open class DefaultRunnerNode(
        override val runner: (String, Cyls) -> Unit
) : RunnerNode {
    override fun matches(string: String, cyls: Cyls) = true
}