package com.scienjus.smartqq.model

import com.github.salomonbrys.kotson.*
import com.google.gson.*

/**
 * 讨论组消息.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
data class DiscussMessage(
        var discussId: Long = 0,
        var time: Long = 0,
        var content: String? = null,
        var userId: Long = 0,
        var font: Font? = null
) {
    constructor(json: JsonObject) : this() {
        val content = json.get("content").asJsonArray
        this.font = content.get(0).asJsonArray.getObject(1)
        this.content = content.get(1).asString
        this.time = json.get("time").asLong
        this.discussId = json.get("did").asLong
        this.userId = json.get("send_uin").asLong
    }

}
