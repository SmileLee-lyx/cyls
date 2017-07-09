package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 用户.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class UserInfo {

    var birthday: Birthday? = null

    var phone: String? = null

    var occupation: String? = null

    var college: String? = null

    var uin: String? = null

    var blood: Int = 0

    var lnick: String? = null   //签名

    var homepage: String? = null

    @JSONField(name = "vip_info")
    var vipInfo: Int = 0

    var city: String? = null

    var country: String? = null

    var province: String? = null

    var personal: String? = null

    var shengxiao: Int = 0

    var nick: String? = null

    var email: String? = null

    var account: String? = null

    var gender: String? = null

    var mobile: String? = null
}
