package com.scienjus.smartqq.model

import com.alibaba.fastjson.*

/**
 * 讨论组消息.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
class DiscussMessage(json: JSONObject) {

    var discussId: Long = 0

    var time: Long = 0

    var content: String? = null

    var userId: Long = 0

    var font: Font? = null

    init {
        val content = json.getJSONArray("content")
        this.font = content.getJSONArray(0).getObject(1, Font::class.java)
        this.content = content.getString(1)
        this.time = json.getLongValue("time")
        this.discussId = json.getLongValue("did")
        this.userId = json.getLongValue("send_uin")
    }
}
