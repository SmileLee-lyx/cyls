package com.scienjus.smartqq.model

/**
 * 好友.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/18.
 */
class Friend {

    var userId: Long = 0

    var markname = ""

    var nickname: String? = null

    var isVip: Boolean = false

    var vipLevel: Int = 0

    override fun toString(): String {
        return "Friend{userId=$userId, markname='$markname', nickname='$nickname', vip=$isVip, vipLevel=$vipLevel}"
    }
}
