package com.scienjus.smartqq.model

/**
 * 字体.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
class Font {

    var style: IntArray? = null

    var color: String? = null

    var name: String? = null

    var size: Int = 0

    companion object {

        val DEFAULT_FONT = defaultFont()

        private fun defaultFont(): Font {
            val font = Font()
            font.color = "000000"
            font.style = intArrayOf(0, 0, 0)
            font.name = "宋体"
            font.size = 10
            return font
        }
    }
}
