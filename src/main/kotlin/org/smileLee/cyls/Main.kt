package org.smileLee.cyls

import org.ansj.splitWord.analysis.ToAnalysis
import org.smileLee.cyls.cyls.Cyls

/**
 * @author 2333
 */
object Main {
    private val loggerInfoName = "cylsData/cylsInfo.properties"

    private val cyls = Cyls(loggerInfoName)

    @JvmStatic
    fun main(args: Array<String>) {
        ToAnalysis.parse("233").toString() //初始化分词库，无实际作用
        cyls.init()
    }
}