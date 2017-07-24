package com.scienjus.smartqq.model

import com.google.gson.annotations.*

/**
 * 好友状态.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class FriendStatus(
        @SerializedName("uin")
        var userId: Long = 0,
        var status: String? = null,
        var client_type: Int = 0
)
