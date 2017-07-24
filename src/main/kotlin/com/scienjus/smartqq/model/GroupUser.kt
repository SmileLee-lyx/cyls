package com.scienjus.smartqq.model

import com.google.gson.annotations.*

/**
 * 群成员.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class GroupUser(
        var nick: String? = "",
        var province: String? = "",
        var gender: String? = "",
        @SerializedName("uin")
        var userId: Long = 0L,
        var country: String? = "",
        var city: String? = "",
        var card: String? = null,
        var clientType: Int = 0,
        var status: Int = 0,
        var isVip: Boolean = false,
        var vipLevel: Int = 0
)
