package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.JSONType
import com.scienjus.smartqq.model.Friend
import org.smileLee.cyls.qqbot.MatchingVerifier
import org.smileLee.cyls.qqbot.QQBotFriend
import org.smileLee.cyls.qqbot.TreeNode
import org.smileLee.cyls.qqbot.anyOf
import org.smileLee.cyls.qqbot.contain
import org.smileLee.cyls.qqbot.createTree
import org.smileLee.cyls.qqbot.createVerifier
import org.smileLee.cyls.qqbot.equal

@JSONType(ignores = arrayOf(
        "friend",
        "admin",
        "owner",
        "ignored"
))
class CylsFriend(
        override var markName: String = "",
        var adminLevel: AdminLevel = AdminLevel.NORMAL,
        var ignoreLevel: IgnoreLevel = IgnoreLevel.RECOGNIZED,
        var isRepeated: Boolean = false,
        var repeatFrequency: Double = 0.0,
        var isMoha: Boolean = false,
        override var friend: Friend? = null,
        override var status: ChattingStatus = CylsFriend.ChattingStatus.COMMON
) : QQBotFriend<Cyls>() {
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

    override fun equals(other: Any?) = other is CylsFriend && markName == other.markName
    override fun hashCode() = markName.hashCode()

    companion object {
        val commonCommand = createTree<Cyls> {
        }
        val commonVerifier = createVerifier<Cyls> {
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
            override val commandTree: TreeNode<Cyls>,
            override val replyVerifier: MatchingVerifier<Cyls>
    ) : QQBotChattingStatus<Cyls> {
        COMMON(commonCommand, commonVerifier);
    }
}