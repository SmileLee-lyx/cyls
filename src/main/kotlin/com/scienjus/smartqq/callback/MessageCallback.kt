package com.scienjus.smartqq.callback

import com.scienjus.smartqq.model.DiscussMessage
import com.scienjus.smartqq.model.GroupMessage
import com.scienjus.smartqq.model.Message

/**
 * 收到消息的回调

 * @author ScienJus
 * *
 * @date 2015/12/18.
 */
interface MessageCallback {

    /**
     * 收到私聊消息后的回调

     * @param message
     */
    fun onMessage(message: Message) = Unit

    /**
     * 收到群消息后的回调

     * @param message
     */
    fun onGroupMessage(message: GroupMessage) = Unit

    /**
     * 收到讨论组消息后的回调

     * @param message
     */
    fun onDiscussMessage(message: DiscussMessage) = Unit
}
