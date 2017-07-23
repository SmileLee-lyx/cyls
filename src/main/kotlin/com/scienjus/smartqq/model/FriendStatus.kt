package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 好友状态.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class FriendStatus {

    @JSONField(name = "uin")
    var userId: Long = 0

    var status: String? = null

    @JSONField(name = "client_type")
    var clientType: Int = 0
}
