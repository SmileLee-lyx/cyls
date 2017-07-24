package com.scienjus.smartqq.model

/**
 * 字体.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
data class Font(
        var style: IntArray? = null,
        var color: String? = null,
        var name: String? = null,
        var size: Int = 0
) {
    companion object {
        val DEFAULT_FONT = Font(
                intArrayOf(0, 0, 0),
                "000000",
                "宋体",
                10
        )
    }
}
