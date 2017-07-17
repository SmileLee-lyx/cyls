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

    var userId = 0L

    var markname: String? = null

    var nickname = ""

    var isVip = false

    var vipLevel = 0

    override fun toString(): String {
        return "Friend{userId=$userId, markname='$markname', nickname='$nickname', vip=$isVip, vipLevel=$vipLevel}"
    }
}
