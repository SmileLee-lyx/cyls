package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.alibaba.fastjson.serializer.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.cyls.CylsFriend.*

@JSONType(ignores = arrayOf("friend"))
class CylsFriend(
        var markName: String = "",
        var adminLevel: AdminLevel = AdminLevel.NORMAL,
        var ignoreLevel: IgnoreLevel = IgnoreLevel.RECOGNIZED,
        @JSONField(serialize = false) var friend: Friend? = null
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

    val isAdmin @JSONField(serialize = false) get() = adminLevel == AdminLevel.ADMIN || adminLevel == AdminLevel.OWNER
    val isOwner @JSONField(serialize = false) get() = adminLevel == AdminLevel.OWNER
    val isIgnored @JSONField(serialize = false) get() = ignoreLevel == IgnoreLevel.IGNORED

    fun set(friend: Friend) {
        markName = friend.markname
        this.friend = friend
    }
}