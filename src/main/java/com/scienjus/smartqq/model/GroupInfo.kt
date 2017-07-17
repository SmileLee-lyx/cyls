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

    var gid = 0L

    var createtime = 0L

    var memo = ""

    var name = ""

    var owner = 0L

    var markname = ""

    var users: MutableList<GroupUser> = ArrayList()

    fun addUser(user: GroupUser) {
        this.users.add(user)
    }
}
