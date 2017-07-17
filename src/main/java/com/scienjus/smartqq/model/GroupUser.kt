package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 群成员.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class GroupUser {
    var nick = ""
    var province = ""
    var gender = ""
    @JSONField(name = "uin")
    var uid = 0L
    var country = ""
    var city = ""
    var card: String? = null
    var clientType = 0
    var status = 0
    var isVip = false
    var vipLevel = 0
}
