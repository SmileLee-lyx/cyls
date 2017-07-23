package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 讨论组成员.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class DiscussUser {

    @JSONField(name = "uin")
    var userId: Long = 0

    var nick: String? = null

    var clientType: Int = 0

    var status: String? = null

    override fun toString(): String {
        return "DiscussUser{uin=$userId, nick='$nick', clientType='$clientType', status='$status'}"
    }
}
