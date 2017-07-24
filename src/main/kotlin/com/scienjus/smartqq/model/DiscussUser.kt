package com.scienjus.smartqq.model

import com.google.gson.annotations.SerializedName

/**
 * 讨论组成员.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class DiscussUser(
        @SerializedName("uin")
        var userId: Long = 0,
        var nick: String? = null,
        var clientType: Int = 0,
        var status: String? = null
)