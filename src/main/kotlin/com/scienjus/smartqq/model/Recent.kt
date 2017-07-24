package com.scienjus.smartqq.model

import com.google.gson.annotations.SerializedName

/**
 * 最近会话.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class Recent(
        @SerializedName("uin")
        var id: Long = 0,
        //0:好友、1:群、2:讨论组
        var type: Int = 0
)
