package org.smileLee.cyls.cyls

class MatchingVerifier(
        val nodes: ArrayList<RegexNode>
) {
    class RegexNode(
            val regex: String,
            val runner: (String) -> Unit
    )

    fun findAndRun(string: String) {
        nodes.forEach {
            if (string.matches(it.regex.toRegex())) {
                it.runner(string)
                return
            }
        }
    }
}
