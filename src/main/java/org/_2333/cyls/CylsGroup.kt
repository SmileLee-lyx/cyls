package org._2333.cyls

import java.lang.Thread.*

class CylsGroup {
    var isPaused = false
    val messageCount
        get() = _messageCount
    private var _messageCount = 0
    val MAX_MESSAGE_COUNT = 50

    fun addMessage() {
        ++_messageCount
        Thread(Runnable {
            sleep(5 * 60 * 1000)
            --_messageCount
        })
    }

    val hot get() = messageCount > MAX_MESSAGE_COUNT
}
