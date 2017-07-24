package com.scienjus.smartqq.model

import com.google.gson.annotations.*

/**
 * 讨论组.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/23.
 */
data class Discuss(
        @SerializedName("did")
        var discussId: Long = 0,
        var name: String? = null
)
