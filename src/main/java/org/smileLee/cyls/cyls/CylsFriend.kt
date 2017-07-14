package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*

@JSONType(ignores = arrayOf(
        "friend",
        "admin",
        "owner",
        "ignored"
))
class CylsFriend(
        var markName: String = "",
        var adminLevel: AdminLevel = AdminLevel.NORMAL,
        var ignoreLevel: IgnoreLevel = IgnoreLevel.RECOGNIZED,
        var isRepeated: Boolean = false,
        var repeatFrequency: Double = 0.0,
        var isMoha: Boolean = false,
        var friend: Friend? = null
) {
    enum class AdminLevel {
        NORMAL,
        ADMIN,
        OWNER
    }

    enum class IgnoreLevel {
        RECOGNIZED,
        IGNORED
    }

    val isAdmin get() = adminLevel == AdminLevel.ADMIN || adminLevel == AdminLevel.OWNER
    val isOwner get() = adminLevel == AdminLevel.OWNER
    val isIgnored get() = ignoreLevel == IgnoreLevel.IGNORED

    fun set(friend: Friend) {
        markName = friend.markname
        this.friend = friend
    }
}