package org.smileLee.cyls.qqbot

import com.scienjus.smartqq.model.Friend

abstract class QQBotFriend<T : QQBot<T>> {
    abstract var markName: String
    abstract val status: QQBotChattingStatus<T>
    abstract var friend: Friend?

    fun set(friend: Friend) {
        markName = friend.markname ?: ""
        this.friend = friend
    }

    interface QQBotChattingStatus<T : QQBot<T>> {
        val commandTree: TreeNode<T>
        val replyVerifier: MatchingVerifier<T>
    }
}