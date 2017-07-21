package org.smileLee.cyls

import org.ansj.splitWord.analysis.*
import org.smileLee.cyls.cyls.*
import java.io.*

/**
 * @author 2333
 */
object Main {

    private val cyls = Cyls()

    @JvmStatic
    fun main(args: Array<String>) {
        ToAnalysis.parse("233").toString() //初始化分词库，无实际作用

        cyls.init
        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 将在第一次请求时创建
    }
}
