package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.util.*
import org.smileLee.smilescript.expression.controlExpression.*
import org.smileLee.smilescript.stack.*
import java.lang.Thread.*

@JSONType(ignores = arrayOf(
        "hot",
        "hasGreeted",
        "group",
        "groupInfo",
        "_groupUsersFromId",
        "groupUsersFromId",
        "messageCount",
        "_messageCount",
        "_greetingCount"
))
class CylsGroup(
        var name: String = "",
        var isPaused: Boolean = false,
        var isRepeated: Boolean = false,
        var repeatFrequency: Double = 0.0
) {
    companion object {
        val MAX_MESSAGE_COUNT = 50
    }

    var group: Group? = null
    var groupInfo: GroupInfo? = null
    var _groupUsersFromId: HashMap<Long, GroupUser> = HashMap()
    val groupUsersFromId = InitSafeMap(_groupUsersFromId) { key ->
        groupInfo?.users?.forEach {
            if (it.uid == key) {
                _groupUsersFromId.put(key, it)
                return@InitSafeMap it
            }
        }
        null!!
    }

    val messageCount get() = _messageCount
    val hasGreeted get() = _greetingCount != 0
    val hot get() = messageCount > MAX_MESSAGE_COUNT

    private var _messageCount = 0
    private var _greetingCount = 0

    fun addMessage() {
        ++_messageCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_messageCount }).start()
    }

    fun addGreeting() {
        ++_greetingCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_greetingCount }).start()
    }

    fun set(group: Group) {
        name = group.name
        this.group = group
    }

    @JSONField(serialize = false) val stack = Stack()
    fun calculate(s: String) = Block.parse(s).invoke(stack).toString()
}
