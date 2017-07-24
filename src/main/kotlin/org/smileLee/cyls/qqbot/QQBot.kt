package org.smileLee.cyls.qqbot

import com.scienjus.smartqq.callback.MessageCallback
import com.scienjus.smartqq.client.SmartQQClient
import com.scienjus.smartqq.model.GroupMessage
import com.scienjus.smartqq.model.Message
import org.smileLee.cyls.util.NonNullMap
import org.smileLee.cyls.util.Util
import org.smileLee.cyls.util.Util.timeName
import sun.dc.path.PathException
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

abstract class QQBot<T : QQBot<T>>(
        infoName: String
) {
    abstract fun getThis(): T

    abstract val qqBotName: String

    protected var working = true

    protected val loggerInfo = File(infoName)
    protected lateinit var ownerName: String
    abstract val loggerFile: File

    abstract val data: QQBotData<T>

    abstract var currentGroupMessage: GroupMessage
    abstract var currentFriendMessage: Message
    abstract val currentGroupId: Long
    abstract val currentGroupUserId: Long
    abstract val currentFriendId: Long
    abstract val currentGroup: QQBotGroup<T>
    abstract val currentGroupUser: QQBotFriend<T>
    abstract val currentFriend: QQBotFriend<T>

    abstract val qrCodeFile: File

    fun log(str: String) {
        println(str)
        val dos = DataOutputStream(FileOutputStream(loggerFile, true))
        dos.write("$str\n".toByteArray())
        dos.close()
    }

    open fun save() {
    }

    open fun load() {
        Util.doWithLog("建立好友列表索引") {
            initFriend(client, data.qqBotFriendList, data.qqBotFriendFromId)
        }
        Util.doWithLog("建立群列表索引") {
            initGroup(client, data.qqBotGroupList, data.qqBotGroupFromId)
        }
    }

    /**
     * 获取群id对应群详情

     * @param groupId 被查询的群id
     * *
     * @return 该群详情
     */
    fun getGroupInfoFromID(groupId: Long) = data.qqBotGroupFromId[groupId].run outer@ {
        groupInfo ?: run {
            client.getGroupInfo(this@outer.group!!.code).apply {
                this@outer.groupInfo = this@apply
            }
        }
    }

    /**
     * 获取群id对应群
     */
    fun getGroup(groupId: Long) = data.qqBotGroupFromId[groupId].apply {
        if (groupInfo == null) {
            groupInfo = client.getGroupInfo(group!!.code)
        }
    }

    /**
     * 获取群id对应群名称
     *
     * @param groupId 被查询的群id
     *
     * @return 该群名称
     */
    fun getGroupName(groupId: Long) = getGroup(groupId).name

    fun getFriend(userId: Long) = data.qqBotFriendFromId[userId]

    /**
     * 获取好友id对应的好友昵称
     *
     * @param userId 被查询的好友id
     *
     * @return 该消息发送者
     */
    fun getFriendNick(userId: Long) = getFriendNick(getFriend(userId))

    /**
     * 获取私聊消息发送者昵称
     *
     * @param groupId 被查询的群id
     * @param userId 被查询的群成员id
     *
     * @return 该消息发送者的群名片
     */
    fun getGroupUserNick(groupId: Long, userId: Long): String {
        getGroupInfoFromID(groupId)
        return getGroupUserNick(data.qqBotGroupFromId[groupId], userId)
    }

    protected abstract fun setupInfo()

    var initialized = false

    fun init() {
        if (!initialized) {
            client = SmartQQClient(callback, qrCodeFile)
            client.start()
            setupInfo()
            load()
            initialized = true
        }
    }

    abstract fun illegalOrderReaction(replier: Replier)
    abstract fun testIfReplyToFriend(content: String): Boolean
    abstract fun testIfReplyToGroup(content: String): Boolean

    /**
     * SmartQQ客户端
     */
    protected lateinit var client: SmartQQClient
    val callback = object : MessageCallback {
        override fun onMessage(message: Message) {
            if (working) {
                try {
                    log("[${Util.timeName}] [私聊] ${getFriendNick(message.userId ?: 0)}：${message.content}")
                    currentFriendMessage = message
                    currentGroup
                    currentFriend
                    val content = message.content ?: ""
                    if (content.startsWith("$qqBotName.")) try {
                        val order = Util.readOrder(content.substring(qqBotName.length + 1))
                        currentFriend.status.commandTree.findPath(order.path).run(order.message, getThis())
                    } catch (e: PathException) {
                        illegalOrderReaction(currentFriendReplier)
                    } else {
                        if (testIfReplyToFriend(content)) {
                            currentFriend.status.replyVerifier.findAndRun(content, getThis())
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }

        override fun onGroupMessage(message: GroupMessage) {
            if (working) {
                try {
                    log("[${Util.timeName}] [${getGroupName(message.groupId)}] " +
                            "${getGroupUserNick(message.groupId, message.userId)}：${message.content}")
                    currentGroupMessage = message
                    currentGroup
                    currentGroupUser
                    val content = message.content ?: ""
                    if (content.startsWith("$qqBotName.")) try {
                        val order = Util.readOrder(content.substring(qqBotName.length + 1))
                        currentGroup.status.commandTree.findPath(order.path).run(order.message, getThis())
                    } catch (e: PathException) {
                        illegalOrderReaction(currentGroupReplier)
                    } else {
                        if (testIfReplyToGroup(content)) {
                            currentGroup.status.replyVerifier.findAndRun(content, getThis())
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    val currentGroupReplier = object : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [${currentGroup.name}] > $message")
            client.sendMessageToGroup(currentGroupId, message)
        }
    }

    val currentFriendReplier = object : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [私聊] [${currentFriend.markName}] > $message")
            client.sendMessageToFriend(currentFriendId, message)
        }
    }

    inner class replierToGroup(private val group: QQBotGroup<T>) : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [${group.name}] > $message")
            client.sendMessageToGroup(group.group!!.groupId, message)
        }

        constructor(groupId: Long) : this(getGroup(groupId))
    }

    inner class replierToFriend(private val friend: QQBotFriend<T>) : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [私聊] [${friend.markName}] > $message")
            client.sendMessageToFriend(friend.friend!!.userId, message)
        }

        constructor(userId: Long) : this(getFriend(userId))
    }

    companion object {
        val MAX_RETRY = 3
        inline fun <T> retry(action: () -> T): T {
            for (retry in 0..MAX_RETRY) {
                try {
                    return action()
                } catch (e: Exception) {
                    if (retry != MAX_RETRY) {
                        println("[$timeName] 第${retry + 1}次尝试失败。正在重试...")
                    }
                }
            }
            println("[$timeName] 重试次数达到最大限制，程序无法继续进行。")
            System.exit(1)
            throw Error("Unreachable code")
        }

        fun <T : QQBot<T>> initGroup(
                client: SmartQQClient,
                cylsGroupList: ArrayList<out QQBotGroup<T>>,
                cylsGroupFromId: NonNullMap<Long, out QQBotGroup<T>>
        ) {
            val groupList = retry { client.groupList }
            retry {
                groupList.forEach { group ->
                    cylsGroupList.filter { cylsGroup -> cylsGroup.name == group.name }
                            .forEach { cylsGroup -> cylsGroup.set(group) }
                    cylsGroupFromId[group.groupId].set(group)
                }
            }
        }

        fun <T : QQBot<T>> initFriend(
                client: SmartQQClient,
                cylsFriendList: ArrayList<out QQBotFriend<T>>,
                cylsFriendFromId: NonNullMap<Long, out QQBotFriend<T>>
        ) {
            val friendList = retry { client.friendList }
            retry {
                friendList.forEach { friend ->
                    cylsFriendList.filter { cylsFriend -> cylsFriend.markName == friend.markname }
                            .forEach { cylsFriend -> cylsFriend.set(friend) }
                    cylsFriendFromId[friend.userId].set(friend)
                }
            }
        }

        fun <T : QQBot<T>> getFriendNick(friend: QQBotFriend<T>): String = friend.markName
        fun <T : QQBot<T>> getGroupUserNick(group: QQBotGroup<T>, userId: Long): String {
            val user = group.groupUsersFromId[userId]
            return user.card ?: user.nick ?: ""
        }
    }
}