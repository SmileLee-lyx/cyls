package com.scienjus.smartqq.model

import java.util.*

/**
 * 分组.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 15/12/19.
 */
class Category {

    var index: Int = 0

    var sort: Int = 0

    var name: String? = null

    private var friends: MutableList<Friend> = ArrayList()

    fun addFriend(friend: Friend) {
        this.friends.add(friend)
    }

    override fun toString(): String {
        return "Category{index=$index, sort=$sort, name='$name', friends=$friends}"
    }

    fun getFriends(): List<Friend> {
        return friends
    }

    fun setFriends(friends: MutableList<Friend>) {
        this.friends = friends
    }

    companion object {

        fun defaultCategory(): Category {
            val category = Category()
            category.index = 0
            category.sort = 0
            category.name = "我的好友"
            return category
        }
    }
}
