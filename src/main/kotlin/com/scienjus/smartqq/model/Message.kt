package com.scienjus.smartqq.model

import com.github.salomonbrys.kotson.getObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * 消息.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
data class Message(
        var time: Long = 0L,
        var content: String? = "",
        var userId: Long? = 0L,
        var font: Font? = null
) {
    constructor(json: JsonObject) : this() {
        val cont = json.get("content").asJsonArray
        this.font = cont.get(0).asJsonArray.getObject(1)
        val size = cont.size()
        val contentBuilder = StringBuilder()
        for (i in 1 until size) {
            contentBuilder.append(cont.get(i).run {
                if (this@run is JsonPrimitive && this@run.isString) {
                    this@run.string
                } else {
                    this@run.toString()
                }
            })
        }
        this.content = contentBuilder.toString()
        this.time = json.get("time").asLong
        this.userId = json.get("from_uin").asLong
    }
}
