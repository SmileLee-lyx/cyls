package org.smileLee.cyls.qqbot

import org.smileLee.cyls.util.NonNullMap

abstract class QQBotData<T : QQBot<T>> {
    abstract val qqBotFriendList: ArrayList<out QQBotFriend<T>>
    abstract val qqBotGroupList: ArrayList<out QQBotGroup<T>>
    abstract val qqBotFriendFromId: NonNullMap<Long, out QQBotFriend<T>>
    abstract val qqBotGroupFromId: NonNullMap<Long, out QQBotGroup<T>>
}