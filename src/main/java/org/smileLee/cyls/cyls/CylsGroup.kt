package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.alibaba.fastjson.serializer.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.*
import java.lang.Thread.*

@JSONType(ignores = arrayOf("hot"))
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
    @JSONField(serialize = false) val groupUsersFromId = object : SafeMap<Long, GroupUser> {
        override fun put(key: Long, value: GroupUser) = _groupUsersFromId.put(key, value)
        override fun putAll(from: Map<Long, GroupUser>) = _groupUsersFromId.putAll(from)
        override fun iterator() = _groupUsersFromId.iterator()
        override fun get(key: Long): GroupUser {
            val user = _groupUsersFromId[key]
            if (user != null) return user else {
                groupInfo?.users?.forEach {
                    if (it.uin == key) {
                        _groupUsersFromId.put(key, it)
                        return it
                    }
                }
                null!!
            }
        }
    }

    val messageCount get() = _messageCount
    val hot get() = messageCount > MAX_MESSAGE_COUNT

    @JSONField(serialize = false) private var _messageCount = 0

    fun addMessage() {
        ++_messageCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_messageCount }).start()
    }

    fun set(group: Group) {
        name = group.name!!
        this.group = group
    }
}
