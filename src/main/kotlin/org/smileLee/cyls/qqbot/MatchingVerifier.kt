package org.smileLee.cyls.qqbot

class MatchingVerifier<T : QQBot<T>>(
        val nodes: ArrayList<VerifyNode<T>>
) {
    fun findAndRun(string: String, cyls: T) = nodes.any {
        it.verifyAndRun(string, cyls)
    }

    operator fun VerifyNode<T>.unaryPlus() {
        nodes.add(this@unaryPlus)
    }
}

interface VerifyNode<T : QQBot<T>> {
    fun matches(string: String, cyls: T): Boolean
    fun verifyAndRun(string: String, cyls: T): Boolean
}

interface PathNode<T : QQBot<T>> : VerifyNode<T> {
    val children: ArrayList<VerifyNode<T>>
}

interface RunnerNode<T : QQBot<T>> : VerifyNode<T> {
    val runner: (String, T) -> Unit
    override fun verifyAndRun(string: String, cyls: T) = if (matches(string, cyls)) {
        runner(string, cyls)
        true
    } else false
}

open class AnyRunnerNode<T : QQBot<T>>(
        override val children: ArrayList<VerifyNode<T>>,
        override val runner: (String, T) -> Unit
) : RunnerNode<T>, PathNode<T> {
    override fun matches(string: String, cyls: T) = children.any { it.matches(string, cyls) }
}

open class AllRunnerNode<T : QQBot<T>>(
        override val children: ArrayList<VerifyNode<T>>,
        override val runner: (String, T) -> Unit
) : RunnerNode<T>, PathNode<T> {
    override fun matches(string: String, cyls: T) = children.all { it.matches(string, cyls) }
}

open class EqualNode<T : QQBot<T>>(
        val string: String,
        override val runner: (String, T) -> Unit
) : RunnerNode<T> {
    override fun matches(string: String, cyls: T) = string == this.string
}

open class ContainNode<T : QQBot<T>>(
        val subString: String,
        override val runner: (String, T) -> Unit
) : RunnerNode<T> {
    override fun matches(string: String, cyls: T) = string.contains(subString)
}

open class RegexNode<T : QQBot<T>>(
        val regex: String,
        override val runner: (String, T) -> Unit
) : RunnerNode<T> {
    override fun matches(string: String, cyls: T) = string.matches(regex.toRegex())
}

open class ContainRegexNode<T : QQBot<T>>(
        val regex: String,
        override val runner: (String, T) -> Unit
) : RunnerNode<T> {
    override fun matches(string: String, cyls: T) = string.contains(regex.toRegex())
}

open class DefaultRunnerNode<T : QQBot<T>>(
        override val runner: (String, T) -> Unit
) : RunnerNode<T> {
    override fun matches(string: String, cyls: T) = true
}