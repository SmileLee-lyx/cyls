package com.scienjus.smartqq.model

import com.google.gson.annotations.*

/**
 * 用户.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class UserInfo(
        var birthday: Birthday? = null,
        var phone: String? = null,
        var occupation: String? = null,
        var college: String? = null,
        @SerializedName("uin")
        var userId: String? = null,
        var blood: Int = 0,
        var homepage: String? = null, //签名
        var lnick: String? = null,
        @SerializedName("vip_info")
        var vipInfo: Int = 0,
        var city: String? = null,
        var country: String? = null,
        var province: String? = null,
        var personal: String? = null,
        var shengxiao: Int = 0,
        var nick: String? = null,
        var email: String? = null,
        var account: String? = null,
        var gender: String? = null,
        var mobile: String? = null
)
