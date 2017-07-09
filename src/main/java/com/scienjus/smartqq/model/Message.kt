package com.scienjus.smartqq.model

import com.alibaba.fastjson.*

/**
 * 消息.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
class Message(json: JSONObject) {

    var time: Long = 0

    var content: String? = null

    var userId: Long = 0

    var font: Font? = null

    init {
        val cont = json.getJSONArray("content")
        this.font = cont.getJSONArray(0).getObject(1, Font::class.java)

        val size = cont.size
        val contentBuilder = StringBuilder()
        for (i in 1..size - 1) {
            contentBuilder.append(cont.getString(i))
        }
        this.content = contentBuilder.toString()

        this.time = json.getLongValue("time")
        this.userId = json.getLongValue("from_uin")
    }
}
