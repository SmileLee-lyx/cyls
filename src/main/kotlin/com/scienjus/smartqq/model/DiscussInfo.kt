package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

import java.util.*

/**
 * 讨论组资料.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class DiscussInfo {

    @JSONField(name = "did")
    var id: Long = 0

    @JSONField(name = "discu_name")
    var name: String? = null

    private var users: MutableList<DiscussUser> = ArrayList()

    fun addUser(user: DiscussUser) {
        this.users.add(user)
    }

    fun getUsers(): List<DiscussUser> {
        return users
    }

    fun setUsers(users: MutableList<DiscussUser>) {
        this.users = users
    }
}
