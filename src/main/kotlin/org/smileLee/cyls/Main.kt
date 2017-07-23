package org.smileLee.cyls

import org.ansj.splitWord.analysis.*
import org.smileLee.cyls.cyls.*

/**
 * @author 2333
 */
object Main {
    private val loggerInfoName = "cylsData/loggerInfo.properties"

    private val cyls = Cyls(loggerInfoName)

    @JvmStatic
    fun main(args: Array<String>) {
        ToAnalysis.parse("233").toString() //初始化分词库，无实际作用
        cyls.init
    }
}