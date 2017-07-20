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

class Cyls {
    val loggerInfoName = "cylsData/loggerInfo.property"
    val loggerInfo = File(loggerInfoName)
    lateinit var loggerFile: File

    fun log(str: String) {
        println(str)
        val fos = FileOutputStream(loggerFile, true)
        val dos = DataOutputStream(fos)
        dos.write("$str\n".toByteArray())
        dos.close()
    }

    val MAX_RETRY = 3
    inline fun <T> retry(action: () -> T): T {
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
    val currentGroup get() = data._cylsGroupFromId[currentGroupMessage.groupId]!!
    val currentUser get() = data._cylsFriendFromId[currentGroupMessage.userId]!!
    val currentFriend get() = data._cylsFriendFromId[currentFriendMessage.userId]!!

    fun reply(message: String) {
        log("[${Util.timeName}] [${currentGroup.name}] > $message")
        client.sendMessageToGroup(currentGroupId, message)
    }

    fun replyToFriend(message: String) {
        log("[${Util.timeName}] [${currentFriend.markName}] > $message")
        client.sendMessageToFriend(currentFriendMessage.userId, message)
    }

    /**
     * SmartQQ客户端
     */
    lateinit var client: SmartQQClient
    val callback = object : MessageCallback {
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
                        reply("请确保输入了正确的指令哦|•ω•`)")
                    } else {
                        if (!currentUser.isIgnored) {
                            if (currentUser.isRepeated) {
                                Util.runByChance(currentUser.repeatFrequency) {
                                    reply(message.content)
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
                    currentUser
                    if (message.content.startsWith("cyls.")) try {
                        val order = Util.readOrder(message.content.substring(5))
                        currentGroup.status.commandTree.findPath(order.path).run(order.message, this@Cyls)
                    } catch (e: PathException) {
                        reply("请确保输入了正确的指令哦|•ω•`)")
                    } else {
                        if (!currentGroup.isPaused && !currentUser.isIgnored && !currentGroup.hot
                                && getGroupUserNick(message.groupId, message.userId) != "系统消息") {
                            currentGroup.addMessage()
                            if (currentUser.isRepeated) {
                                Util.runByChance(currentUser.repeatFrequency) {
                                    reply(message.content)
                                }
                            } else if (currentGroup.isRepeated) {
                                Util.runByChance(currentGroup.repeatFrequency) {
                                    reply(message.content)
                                }
                            } else {
                                currentGroup.status.replyVerifier.findAndRun(message.content, this@Cyls)
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
        val file = savedFile
        val fin = FileInputStream(file)
        val length = fin.available()
        val bytes = ByteArray(length)
        fin.read(bytes)
        val json = String(bytes)
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
                data.cylsGroupFromId[group.id].set(group)
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

     * @param id 被查询的群id
     * *
     * @return 该群详情
     */
    fun getGroupInfoFromID(id: Long): GroupInfo {
        val cylsGroup = data.cylsGroupFromId[id]
        return if (cylsGroup.groupInfo != null) cylsGroup.groupInfo!! else {
            val groupInfo = client.getGroupInfo(cylsGroup.group?.code ?: throw RuntimeException())
            cylsGroup.groupInfo = groupInfo
            groupInfo
        }
    }

    /**
     * 获取群消息所在群名称

     * @param id 被查询的群消息
     * *
     * @return 该消息所在群名称
     */
    fun getGroupName(id: Long) = getGroup(id).name

    /**
     * 获取群消息所在群

     * @param id 被查询的群消息
     * *
     * @return 该消息所在群
     */
    fun getGroup(id: Long) = data.cylsGroupFromId[id]

    /**
     * 获取私聊消息发送者昵称

     * @param id 被查询的私聊消息
     * *
     * @return 该消息发送者
     */
    fun getFriendNick(id: Long): String {
        val user = data.cylsFriendFromId[id].friend
        return user?.markname ?: user?.nickname ?: null!!
    }

    fun getGroupUserNick(gid: Long, uid: Long): String {
        getGroupInfoFromID(gid)
        val user = data.cylsGroupFromId[gid].groupUsersFromId[uid]
        return user.card ?: user.nick
    }

    private val weatherKey = "3511aebb46e04a59b77da9b1c648c398"               //天气查询密钥
    private val weatherUrl = ApiURL("https://free-api.heweather.com/v5/forecast?city={1}&key={2}", "")

    /**
     * @param cityName 查询的城市名
     * @param d        0=今天 1=明天 2=后天
     */
    fun getWeather(cityName: String, d: Int) {
        val actualCityName = cityName.replace("[ 　\t\n]".toRegex(), "")
        if (actualCityName == "") {
            reply("请输入城市名称进行查询哦|•ω•`)")
        } else {
            val days = arrayOf("今天", "明天", "后天")
            reply("云裂天气查询服务|•ω•`)\n下面查询$actualCityName${days[d]}的天气:")
            var msg = ""
            val web = weatherUrl.buildUrl(actualCityName, weatherKey)
            try {
                val result = WebUtil.request(web)
                val weather = JSON.parseObject(result)
                val weatherData = weather.getJSONArray("HeWeather5").getJSONObject(0)
                val basic = weatherData.getJSONObject("basic")
                if (basic == null) {
                    reply("啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)")
                } else {
                    val forecast = weatherData.getJSONArray("daily_forecast")
                    val day = forecast.getJSONObject(d)
                    val cond = day.getJSONObject("cond")
                    if (cond.getString("txt_d") == cond.getString("txt_n")) {
                        msg += "全天${cond.getString("txt_d")},\n"
                    } else {
                        msg += "白天${cond.getString("txt_d")}，夜晚${cond.getString("txt_n")}，\n"
                    }
                    val tmp = day.getJSONObject("tmp")
                    msg += "最高温${tmp.getString("max")}℃，最低温${tmp.getString("min")}℃，\n"
                    val wind = day.getJSONObject("wind")
                    if (wind.getString("sc") == "微风") msg += "微${wind.getString("dir")}|•ω•`)"
                    else msg += "${wind.getString("dir")}${wind.getString("sc")}级|•ω•`)"
                    reply(msg)
                }
            } catch (e: Exception) {
                reply("啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)")
            }
        }
    }

    val init: Unit by lazy {
        client = SmartQQClient(callback, qrCodeFile)
        client.start()
        load()
        val loggerProperties = Properties()
        loggerProperties.load(FileInputStream(loggerInfo))
        val currentIndex = (loggerProperties.getProperty("index", "0").toIntOrNull() ?: 0) + 1
        loggerProperties.setProperty("index", currentIndex.toString())
        loggerProperties.store(FileOutputStream(loggerInfo), null)
        loggerFile = File("cylsData/chattingLog_$currentIndex.txt")
    }
}