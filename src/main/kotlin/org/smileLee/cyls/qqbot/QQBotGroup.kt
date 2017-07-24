package org.smileLee.cyls.qqbot

import com.scienjus.smartqq.model.Group
import com.scienjus.smartqq.model.GroupInfo
import com.scienjus.smartqq.model.GroupUser
import org.smileLee.cyls.util.NonNullMap

abstract class QQBotGroup<T : QQBot<T>> {
    abstract var name: String
    abstract val status: QQBotChattingStatus<T>
    abstract var group: Group?
    abstract var groupInfo: GroupInfo?
    abstract val groupUsersFromId: NonNullMap<Long, GroupUser>

    fun set(group: Group) {
        name = group.name ?: ""
        this.group = group
    }

    interface QQBotChattingStatus<T : QQBot<T>> {
        val commandTree: TreeNode<T>
        val replyVerifier: MatchingVerifier<T>
    }
}