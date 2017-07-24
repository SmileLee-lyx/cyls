package com.scienjus.smartqq.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 讨论组资料.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class DiscussInfo(
        @SerializedName("did")
        var discussId: Long = 0,
        @SerializedName("discu_name")
        var name: String? = null,
        var users: MutableList<DiscussUser>? = ArrayList()
) {
    fun addUser(user: DiscussUser) {
        this.users?.add(user)
    }
}
