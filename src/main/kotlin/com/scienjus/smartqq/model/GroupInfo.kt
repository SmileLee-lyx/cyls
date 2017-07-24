package com.scienjus.smartqq.model

import com.google.gson.annotations.*
import java.util.*

/**
 * 群资料.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
data class GroupInfo(
        @SerializedName("gid")
        var groupId: Long = 0L,
        var createtime: Long = 0L,
        var memo: String? = "",
        var name: String? = "",
        var owner: Long = 0L,
        var markname: String? = "",
        var users: MutableList<GroupUser>? = ArrayList()
) {
    fun addUser(user: GroupUser) {
        this.users?.add(user)
    }
}
