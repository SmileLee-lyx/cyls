package com.scienjus.smartqq.callback

import com.scienjus.smartqq.model.*

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
    fun onMessage(message: Message) {}

    /**
     * 收到群消息后的回调

     * @param message
     */
    fun onGroupMessage(message: GroupMessage) {}

    /**
     * 收到讨论组消息后的回调

     * @param message
     */
    fun onDiscussMessage(message: DiscussMessage) {}
}
