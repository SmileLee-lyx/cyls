package org._2333.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import java.lang.Thread.*

class CylsGroup(
        @JSONField
        var name: String = "",
        @JSONField
        var isPaused: Boolean = false,
        @JSONField(serialize = false)
        var group: Group? = null,
        @JSONField(serialize = false)
        var groupInfo: GroupInfo? = null
) {

    private var _messageCount = 0

    val MAX_MESSAGE_COUNT = 50

    fun addMessage() {
        ++_messageCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_messageCount }).start()
    }

    val messageCount get() = _messageCount
    val hot get() = messageCount > MAX_MESSAGE_COUNT
}
