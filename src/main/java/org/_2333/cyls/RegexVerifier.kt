package org._2333.cyls

import org.intellij.lang.annotations.*

class RegexVerifier(
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