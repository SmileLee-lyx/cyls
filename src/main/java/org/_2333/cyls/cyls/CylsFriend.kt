package org._2333.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org._2333.cyls.cyls.CylsFriend.*

class CylsFriend(
        @JSONField
        var markName: String = "",
        @JSONField
        var adminLevel: AdminLevel = AdminLevel.NORMAL,
        @JSONField
        var ignoreLevel: IgnoreLevel = IgnoreLevel.RECOGNIZED,
        @JSONField(serialize = false)
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
}