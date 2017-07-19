package com.scienjus.smartqq.model

import com.alibaba.fastjson.*

/**
 * 群消息.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
class GroupMessage {

    var groupId = 0L

    var time = 0L

    var content = ""

    var userId = 0L

    var font: Font? = null

    constructor()

    constructor(json: JSONObject) {
        val cont = json.getJSONArray("content")
        this.font = cont.getJSONArray(0).getObject(1, Font::class.java)

        val size = cont.size
        val contentBuilder = StringBuilder()
        for (i in 1..size - 1) {
            contentBuilder.append(cont.getString(i))
        }
        this.content = contentBuilder.toString()

        this.time = json.getLongValue("time")
        this.groupId = json.getLongValue("group_code")
        this.userId = json.getLongValue("send_uin")
    }
}
