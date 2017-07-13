package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.*
import org.smileLee.smilescript.expression.controlExpression.*
import org.smileLee.smilescript.stack.*
import java.lang.Thread.*

@JSONType(ignores = arrayOf("hot", "hasGreeted"))
class CylsGroup(
        var name: String = "",
        var isPaused: Boolean = false
) {
    companion object {
        val MAX_MESSAGE_COUNT = 50
    }

    @JSONField(serialize = false) var group: Group? = null
    @JSONField(serialize = false) var groupInfo: GroupInfo? = null
    @JSONField(serialize = false) var _groupUsersFromId: HashMap<Long, GroupUser> = HashMap()
    @JSONField(serialize = false) val groupUsersFromId = InitSafeMap(_groupUsersFromId) { key ->
        groupInfo?.users?.forEach {
            if (it.uin == key) {
                _groupUsersFromId.put(key, it)
                return@InitSafeMap it
            }
        }
        null!!
    }

    val messageCount get() = _messageCount
    val hasGreeted get() = _greetingCount != 0
    val hot get() = messageCount > MAX_MESSAGE_COUNT

    @JSONField(serialize = false) private var _messageCount = 0
    @JSONField(serialize = false) private var _greetingCount = 0

    fun addMessage() {
        ++_messageCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_messageCount }).start()
    }

    fun addGreeting() {
        ++_greetingCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_greetingCount }).start()
    }

    fun set(group: Group) {
        name = group.name!!
        this.group = group
    }

    @JSONField(serialize = false) val stack = Stack()
    fun calculate(s: String) = Block.parse(s).invoke(stack).toString()
}
