package com.scienjus.smartqq.model

/**
 * 好友.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/18.
 */
data class Friend(
        var userId: Long = 0L,
        var markname: String? = null,
        var nickname: String? = "",
        var isVip: Boolean = false,
        var vipLevel: Int = 0
)
