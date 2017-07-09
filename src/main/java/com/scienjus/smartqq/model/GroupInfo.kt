package com.scienjus.smartqq.model

import java.util.*

/**
 * 群资料.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class GroupInfo {

    var gid: Long = 0

    var createtime: Long = 0

    var memo: String? = null

    var name: String? = null

    var owner: Long = 0

    var markname: String? = null

    var users: MutableList<GroupUser> = ArrayList()

    fun addUser(user: GroupUser) {
        this.users.add(user)
    }
}
