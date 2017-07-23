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
        var friend: Friend? = null,
        var status: ChattingStatus = CylsFriend.ChattingStatus.COMMON
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

    fun authorityGreaterThan(other: CylsFriend) = isOwner || (isAdmin && !other.isAdmin) || this == other

    fun set(friend: Friend) {
        markName = friend.markname ?: ""
        this.friend = friend
    }

    override fun equals(other: Any?) = other is CylsFriend && markName == other.markName

    companion object {
        val commonCommand = createTree {
        }
        val commonVerifier = createVerifier {
            anyOf({
                equal("早")
                contain("早安")
            }) { _, cyls ->
                PredefinedReplyInfo.GoodMorning()
                        .replyTo(cyls.currentFriendReplier)
            }
            contain("晚安", { _, cyls ->
                PredefinedReplyInfo.GoodNight()
                        .replyTo(cyls.currentFriendReplier)
            })
        }
    }

    enum class ChattingStatus(
            val commandTree: TreeNode,
            val replyVerifier: MatchingVerifier
    ) {
        COMMON(commonCommand, commonVerifier);
    }

}