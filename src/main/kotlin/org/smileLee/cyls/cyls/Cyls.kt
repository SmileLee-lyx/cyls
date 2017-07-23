package org.smileLee.cyls.cyls

import com.alibaba.fastjson.*
import com.scienjus.smartqq.callback.*
import com.scienjus.smartqq.client.*
import com.scienjus.smartqq.constant.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.util.*
import sun.dc.path.*
import java.io.*
import java.util.*

class Cyls(loggerInfoName: String) {
    private val loggerInfo = File(loggerInfoName)
    private lateinit var ownerName: String
    private lateinit var loggerFile: File

    fun log(str: String) {
        println(str)
        val fos = FileOutputStream(loggerFile, true)
        val dos = DataOutputStream(fos)
        dos.write("$str\n".toByteArray())
        dos.close()
    }

    private val MAX_RETRY = 3
    inline private fun <T> retry(action: () -> T): T {
        for (retry in 0..MAX_RETRY) {
            try {
                return action()
            } catch (e: Exception) {
                if (retry != MAX_RETRY) {
                    println("[${Util.timeName}] 第${retry + 1}次尝试失败。正在重试...")
                }
            }
        }
        println("[${Util.timeName}] 重试次数达到最大限制，程序无法继续进行。")
        System.exit(1)
        throw Error("Unreachable code")
    }

    private var working = true

    var data = Data()

    var currentGroupMessage: GroupMessage = GroupMessage()
    var currentFriendMessage: Message = Message()
    val currentGroupId get() = currentGroupMessage.groupId
    val currentGroupUserId get() = currentGroupMessage.userId
    val currentFriendId get() = currentFriendMessage.userId
    val currentGroup get() = data._cylsGroupFromId[currentGroupMessage.groupId]!!
    val currentGroupUser get() = data._cylsFriendFromId[currentGroupMessage.userId]!!
    val currentFriend get() = data._cylsFriendFromId[currentFriendMessage.userId]!!

    /**
     * SmartQQ客户端
     */
    lateinit var client: SmartQQClient
    private val callback = object : MessageCallback {
        override fun onMessage(message: Message) {
            if (working) {
                try {
                    log("[${Util.timeName}] [私聊] ${getFriendNick(message.userId)}：${message.content}")
                    currentFriendMessage = message
                    currentGroup
                    currentFriend
                    if (message.content.startsWith("cyls.")) try {
                        val order = Util.readOrder(message.content.substring(5))
                        currentFriend.status.commandTree.findPath(order.path).run(order.message, this@Cyls)
                    } catch (e: PathException) {
                        currentFriendReplier.reply("请确保输入了正确的指令哦|•ω•`)")
                    } else {
                        if (!currentGroupUser.isIgnored) {
                            if (currentGroupUser.isRepeated) {
                                Util.runByChance(currentGroupUser.repeatFrequency) {
                                    currentFriendReplier.reply(message.content)
                                }
                            } else {
                                currentFriend.status.replyVerifier.findAndRun(message.content, this@Cyls)
                            }
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
                    if (message.content.startsWith("cyls.")) try {
                        val order = Util.readOrder(message.content.substring(5))
                        currentGroup.status.commandTree.findPath(order.path).run(order.message, this@Cyls)
                    } catch (e: PathException) {
                        currentGroupReplier.reply("请确保输入了正确的指令哦|•ω•`)")
                    } else {
                        if (!currentGroup.isPaused && !currentGroupUser.isIgnored && !currentGroup.hot
                                && getGroupUserNick(message.groupId, message.userId) != "系统消息") {
                            currentGroup.addMessage()
                            when {
                                currentGroupUser.isRepeated -> Util.runByChance(currentGroupUser.repeatFrequency) {
                                    currentGroupReplier.reply(message.content)
                                }
                                currentGroup.isRepeated     -> Util.runByChance(currentGroup.repeatFrequency) {
                                    currentGroupReplier.reply(message.content)
                                }
                                else                        -> currentGroup.status.replyVerifier
                                        .findAndRun(message.content, this@Cyls)
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private val savedFileName = "cylsData/savedFile.txt"
    private val savedFile = File(savedFileName)
    private val qrCodeFileName = "cylsData/qrcode.png"
    private val qrCodeFile = File(qrCodeFileName)

    /**
     * 加载群信息等
     */
    fun load() {
        val json = savedFile.readText()
        data = JSON.parseObject(json, Data::class.java)
        working = false   //映射建立完毕前暂停接收消息以避免NullPointerException
        println()
        println("[${Util.timeName}] 开始建立索引，暂停接收消息")
        println("[${Util.timeName}] 尝试建立好友列表索引...")
        val friendList = retry { client.friendList }
        retry {
            friendList.forEach { friend ->
                data.cylsFriendList.filter { cylsFriend -> cylsFriend.markName == friend.markname }
                        .forEach { cylsFriend -> cylsFriend.set(friend) }
                data.cylsFriendFromId[friend.userId].set(friend)
            }
        }
        println("[${Util.timeName}] 建立好友列表索引成功。")
        println("[${Util.timeName}] 尝试建立群列表索引...")
        val groupList = retry { client.groupList }
        retry {
            groupList.forEach { group ->
                data.cylsGroupList.filter { cylsGroup -> cylsGroup.name == group.name }
                        .forEach { cylsGroup -> cylsGroup.set(group) }
                data.cylsGroupFromId[group.groupId].set(group)
            }
        }
        println("[${Util.timeName}] 建立群列表索引成功。")
        data.cylsFriendList.filter { it.markName == "smileLee" }
                .forEach { it.adminLevel = CylsFriend.AdminLevel.OWNER }
        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 和 讨论组id到讨论组详情映射 将在第一次请求时创建
        println("[${Util.timeName}] 索引建立完毕，开始接收消息\n")
        working = true                                     //映射建立完毕后恢复工作
    }

    /**
     * 储存群信息等
     */
    fun save() {
        val json = JSON.toJSON(data)
        val file = savedFile
        if (file.exists()) file.delete()
        val fout = FileOutputStream(file)
        fout.write(json.toString().toByteArray())
        fout.close()
    }

    /**
     * 获取群id对应群详情

     * @param groupId 被查询的群id
     * *
     * @return 该群详情
     */
    fun getGroupInfoFromID(groupId: Long): GroupInfo {
        val cylsGroup = data.cylsGroupFromId[groupId]
        return if (cylsGroup.groupInfo != null) cylsGroup.groupInfo!! else {
            client.getGroupInfo(cylsGroup.group!!.code).apply { cylsGroup.groupInfo = this@apply }
        }
    }

    /**
     * 获取群id对应群
     */
    fun getGroup(groupId: Long): CylsGroup {
        val cylsGroup = data.cylsGroupFromId[groupId]
        return if (cylsGroup.groupInfo != null) cylsGroup else {
            cylsGroup.apply { this@apply.groupInfo = client.getGroupInfo(cylsGroup.group!!.code) }
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

    fun getFriend(userId: Long) = data.cylsFriendFromId[userId]

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
        return getGroupUserNick(data.cylsGroupFromId[groupId], userId)
    }

    private lateinit var weatherKey: String               //天气查询密钥
    private val weatherUrl = ApiURL("https://free-api.heweather.com/v5/forecast?city={1}&key={2}", "")

    /**
     * @param cityName 查询的城市名
     * @param d        0=今天 1=明天 2=后天
     */
    fun getWeather(cityName: String, d: Int, replier: Replier) {
        val actualCityName = cityName.replace("[ 　\t\n]".toRegex(), "")
        if (actualCityName == "") {
            replier.reply("请输入城市名称进行查询哦|•ω•`)")
        } else {
            val days = arrayOf("今天", "明天", "后天")
            replier.reply("云裂天气查询服务|•ω•`)\n下面查询$actualCityName${days[d]}的天气:")
            var msg = ""
            val web = weatherUrl.buildUrl(actualCityName, weatherKey)
            try {
                val result = WebUtil.request(web)
                val weather = JSON.parseObject(result)
                val weatherData = weather.getJSONArray("HeWeather5").getJSONObject(0)
                val basic = weatherData.getJSONObject("basic")
                if (basic == null) {
                    replier.reply("啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)")
                } else {
                    val forecast = weatherData.getJSONArray("daily_forecast")
                    val day = forecast.getJSONObject(d)
                    val cond = day.getJSONObject("cond")
                    msg += if (cond.getString("txt_d") == cond.getString("txt_n")) {
                        "全天${cond.getString("txt_d")},\n"
                    } else {
                        "白天${cond.getString("txt_d")}，夜晚${cond.getString("txt_n")}，\n"
                    }
                    val tmp = day.getJSONObject("tmp")
                    msg += "最高温${tmp.getString("max")}℃，最低温${tmp.getString("min")}℃，\n"
                    val wind = day.getJSONObject("wind")
                    msg += if (wind.getString("sc") == "微风") "微${wind.getString("dir")}|•ω•`)"
                    else "${wind.getString("dir")}${wind.getString("sc")}级|•ω•`)"
                    replier.reply(msg)
                }
            } catch (e: Exception) {
                replier.reply("啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)")
            }
        }
    }

    private fun setupLogger() = Properties().apply {
        load(loggerInfo.inputStream())
        ownerName = getProperty("owner")
        weatherKey = getProperty("weatherKey")
        val currentIndex = (getProperty("index", "0").toIntOrNull() ?: 0) + 1
        setProperty("index", currentIndex.toString())
        store(loggerInfo.outputStream(), "")
        loggerFile = File("cylsData/chattingLog_$currentIndex.txt")
    }

    val init by lazy {
        client = SmartQQClient(callback, qrCodeFile)
        client.start()
        setupLogger()
        load()
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

    inner class replierToGroup(private val group: CylsGroup) : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [${group.name}] > $message")
            client.sendMessageToGroup(group.group!!.groupId, message)
        }

        constructor(groupId: Long) : this(getGroup(groupId))
    }

    inner class replierToFriend(private val friend: CylsFriend) : Replier {
        override fun reply(message: String) {
            log("[${Util.timeName}] [私聊] [${friend.markName}] > $message")
            client.sendMessageToFriend(friend.friend!!.userId, message)
        }

        constructor(userId: Long) : this(getFriend(userId))
    }

    companion object {
        fun getFriendNick(friend: CylsFriend): String = friend.markName
        fun getGroupUserNick(group: CylsGroup, userId: Long): String {
            val user = group.groupUsersFromId[userId]
            return user.card ?: user.nick
        }
    }
}
