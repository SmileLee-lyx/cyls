package org.smileLee.cyls.cyls

import org.intellij.lang.annotations.*

class MatchingVerifier(
        val regexNodes: ArrayList<RegexNode>
) {
    class RegexNode(
            @Language("RegExp") val regex: String,
            val runner: (String) -> Unit
    )

    fun findAndRun(string: String) {
        regexNodes.forEach {
            if (string.matches(it.regex.toRegex())) {
                it.runner(string)
                return
            }
        }
    }
}