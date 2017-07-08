package org._2333.cyls

import com.alibaba.fastjson.*
import com.scienjus.smartqq.*
import com.scienjus.smartqq.callback.*
import com.scienjus.smartqq.client.*
import com.scienjus.smartqq.model.*
import org._2333.cyls.Main.Util.time
import org._2333.cyls.RegexVerifier.*
import org.ansj.splitWord.analysis.*
import sun.dc.path.*
import java.io.*
import java.lang.Thread.*
import java.text.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * @author 2333
 */
object Main {
    private object Util {
        /**
         * 获取本地系统时间

         * @return 本地系统时间
         */
        val time: String
            get() {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                return time.format(Date())
            }

        class Order(val path: ArrayList<String>, val message: String)

        /**
         * 将指令转为路径
         */
        fun readOrder(string: String): Order {
            var str = string
            val path = ArrayList<String>()
            while (true) {
                val dotIndex = str.indexOf('.')
                val blankIndex = str.indexOf(' ')
                if (blankIndex == -1 && dotIndex == -1) {
                    path.add(str)
                    return Order(path, "")
                } else if (blankIndex == -1 || (dotIndex < blankIndex && dotIndex != -1)) {
                    path.add(str.substring(0, dotIndex))
                    str = str.substring(dotIndex + 1)
                } else {
                    path.add(str.substring(0, blankIndex))
                    return Order(path, str.substring(blankIndex + 1))
                }
            }
        }

        /**
         * @param cityName 查询的城市名
         * @param d        0=今天 1=明天 2=后天
         */
        @Throws(InterruptedException::class)
        fun getWeather(cityName: String, d: Int): String {
            val actualCityName = cityName.replace("[ 　\t\n]".toRegex(), "")
            if (actualCityName == "") {
                return "请输入城市名称进行查询哦|•ω•`)"
            } else {
                val days = arrayOf("今天", "明天", "后天")
                var msg = "云裂天气查询服务|•ω•`)\n"
                msg = msg + "下面查询" + actualCityName + days[d] + "的天气:\n"
                val web = "https://free-api.heweather.com/v5/forecast?city=$actualCityName&key=$weatherKey"
                val result = WebUtil.request(web, null, "GET")
                if (result == null) {
                    return msg + "啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)"
                } else {
                    val weather = JSON.parseObject(result)
                    val something = weather.getJSONArray("HeWeather5")
                    val anotherThing = something.getJSONObject(0)
                    val basic = anotherThing.getJSONObject("basic")
                    if (basic == null) {
                        return msg + "啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)"
                    } else {
                        val forecast = anotherThing.getJSONArray("daily_forecast")
                        val day = forecast.getJSONObject(d)
                        val cond = day.getJSONObject("cond")
                        if (cond.getString("txt_d") == cond.getString("txt_n")) {
                            msg += "全天${cond.getString("txt_d")},"
                        } else {
                            msg += "白天${cond.getString("txt_d")}，夜晚${cond.getString("txt_n")}，"
                        }
                        val tmp = day.getJSONObject("tmp")
                        msg += "最高温与最低温为${tmp.getString("max")}℃和${tmp.getString("min")}℃，\n"
                        val wind = day.getJSONObject("wind")
                        msg += "${wind.getString("dir")}${wind.getString("sc")}级|•ω•`)"
                        return msg
                    }
                }
            }
        }
    }

    private val weatherKey = "3511aebb46e04a59b77da9b1c648c398"               //天气查询密钥

    private var friendList: List<Friend> = ArrayList()                        //好友列表
    private var groupList: List<Group> = ArrayList()                          //群列表
    private var discussList: List<Discuss> = ArrayList()                      //讨论组列表
    private val friendFromID = HashMap<Long, Friend>()                        //好友id到好友映射
    private val groupFromID = HashMap<Long, Group>()                          //群id到群映射
    private val groupInfoFromID = HashMap<Long, GroupInfo>()                  //群id到群详情映射
    private val groupUsersFromID = HashMap<Long, HashMap<Long, GroupUser>>()  //群id到群成员的映射
    private val cylsGroupFromID = HashMap<Long, CylsGroup>()                  //群id到机器人数据库的映射
    private val discussFromID = HashMap<Long, Discuss>()                      //讨论组id到讨论组映射
    private val discussInfoFromID = HashMap<Long, DiscussInfo>()              //讨论组id到讨论组详情映射

    private val admin = HashSet<Long>()
    private val ignored = HashSet<Long>()
    private var owner = 0L

    private var working = false

    private var currentMessage: GroupMessage = GroupMessage()
    private val currentGroupId get() = currentMessage.groupId
    private val currentUserId get() = currentMessage.userId

    fun reply(message: String) {
        client.sendMessageToGroup(currentMessage.groupId, message)
    }

    /**
     * SmartQQ客户端
     */
    private lateinit var client: SmartQQClient
    val callback = object : MessageCallback {

        override fun onMessage(msg: Message) {
            if (!working) {
                return
            }
            try {
                println("[$time] [私聊] ${getFriendNick(msg)}：${msg.content}")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        override fun onGroupMessage(msg: GroupMessage) {
            if (!working) {
                return
            }
            try {
                println("[$time] [${getGroupName(msg)}] " +
                        "${getGroupUserNick(msg)}：${msg.content}")
                currentMessage = msg
                if (msg.content.startsWith("cyls.")) try {
                    val order = Util.readOrder(msg.content.substring(5))
                    root.findPath(order.path).run(order.message)
                } catch (e: PathException) {
                    reply("请确保输入了正确的指令哦|•ω•`)")
                } else {
                    val cylsGroup = cylsGroupFromID[msg.groupId]
                    if (!(cylsGroup?.isPaused ?: throw RuntimeException())
                            && !ignored.contains(msg.userId) && !cylsGroup.hot
                            && getGroupUserNick(msg) != "系统消息") {
                        cylsGroup?.addMessage()
                        if (friendFromID[msg.userId]?.markname == "79") {
                            reply(msg.content)
                        } else {
                            regexVerifier.findAndRun(msg.content)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDiscussMessage(msg: DiscussMessage) {
            if (!working) {
                return
            }
            try {
                println("[$time] [${getDiscussName(msg)}] ${getDiscussUserNick(msg)}：${msg.content}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 加载群信息等
     */
    private fun setup() {
        val MAX_RETRY = 3 //最大允许重试次数
        working = false   //映射建立完毕前暂停接收消息以避免NullPointerException
        println("\n[$time] 开始建立索引，暂停接收消息")
        //获取好友列表
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                friendList = client.friendList
                println("[$time] 获取好友列表成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 获取好友列表失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 获取好友列表失败，准备第${retry + 1}次重试")
                }
            }
        }
        //获取群列表
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                groupList = client.groupList
                println("[$time] 获取群列表成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 获取群列表失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 获取群列表失败，准备第${retry + 1}次重试")
                }
            }
        }
        //获取讨论组列表
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                discussList = client.discussList
                println("[$time] 获取讨论组列表成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 获取讨论组列表失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 获取讨论组列表失败，准备第${retry + 1}次重试")
                }
            }
        }
        //建立好友id到好友映射
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                friendFromID.clear()
                friendList.forEach {
                    friendFromID.put(it.userId, it)
                    if (it.markname == "smileLee") owner = it.userId
                }
                println("[$time] 建立好友id到好友映射成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 建立好友id到好友映射失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 建立好友id到好友映射失败，准备第${retry + 1}次重试")
                }
            }
        }
        //建立群id到群映射
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                groupFromID.clear()
                groupInfoFromID.clear()
                cylsGroupFromID.clear()
                groupList.forEach {
                    groupFromID.put(it.id, it)
                    cylsGroupFromID.put(it.id, CylsGroup())
                }
                println("[$time] 建立群id到群映射成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 建立群id到群映射失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 建立群id到群映射失败，准备第${retry + 1}次重试")
                }
            }
        }
        //建立讨论组id到讨论组映射
        for (retry in 0..MAX_RETRY) {
            try {
                sleep(100)
                discussFromID.clear()
                discussInfoFromID.clear()
                discussList.forEach { discussFromID.put(it.id, it) }
                println("[$time] 建立讨论组id到讨论组映射成功")
                break
            } catch (e: Exception) {
                if (retry == MAX_RETRY) {
                    println("[$time] 建立讨论组id到讨论组映射失败")
                    println("[$time] 失败次数超出上限，程序无法继续运行")
                    System.exit(1)
                } else {
                    println("[$time] 建立讨论组id到讨论组映射失败，准备第${retry + 1}次重试")
                }
            }
        }
        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 和 讨论组id到讨论组详情映射 将在第一次请求时创建
        load()
        println("[$time] 读取储存的管理员信息成功")
        println("[$time] 索引建立完毕，开始接收消息\n")
        working = true                                     //映射建立完毕后恢复工作
    }

    /**
     * 读取群信息等
     */
    private fun load() {
        val file = File("savedFile.txt")
        if (!file.exists()) return
        val fin = FileInputStream(file)
        val din = DataInputStream(fin)
        admin.clear()
        val numOfAdmin = din.readInt()
        (1..numOfAdmin).forEach {
            val nameLength = din.readInt()
            val nameBytes = ByteArray(nameLength)
            din.readFully(nameBytes)
            val name = String(nameBytes)
            friendList.filter {
                it.markname == name
            }.forEach {
                admin.add(it.userId)
            }
        }
        din.close()
    }

    /**
     * 储存群信息等
     */
    private fun save() {
        val file = File("savedFile.txt")
        if (file.exists()) file.delete()
        val fout = FileOutputStream(file)
        val dout = DataOutputStream(fout)
        dout.writeInt(admin.size)
        admin.forEach {
            try {
                val name = friendFromID[it]?.markname ?: throw RuntimeException()
                dout.writeInt(name.length)
                dout.writeBytes(name)
            } catch (e: RuntimeException) {
                dout.writeInt(0)
            }
        }
        dout.close()
    }

    /**
     * 获取群id对应群详情

     * @param id 被查询的群id
     * *
     * @return 该群详情
     */
    private fun getGroupInfoFromID(id: Long): GroupInfo {
        return groupInfoFromID[id] ?: ({
            val groupInfo = client.getGroupInfo(groupFromID[id]?.code ?: throw RuntimeException())
            groupInfoFromID.put(id, groupInfo)
            val groupUsers = HashMap<Long, GroupUser>()
            groupInfo.users.forEach { groupUsers.put(it.uin, it) }
            groupUsersFromID.put(id, groupUsers)
            groupInfo
        }())
    }

    /**
     * 获取讨论组id对应讨论组详情

     * @param id 被查询的讨论组id
     * *
     * @return 该讨论组详情
     */
    private fun getDiscussInfoFromID(id: Long): DiscussInfo {
        return discussInfoFromID[id] ?: client.getDiscussInfo(id)
    }

    /**
     * 获取群消息所在群名称

     * @param msg 被查询的群消息
     * *
     * @return 该消息所在群名称
     */
    private fun getGroupName(msg: GroupMessage): String {
        return getGroup(msg).name
    }

    /**
     * 获取讨论组消息所在讨论组名称

     * @param msg 被查询的讨论组消息
     * *
     * @return 该消息所在讨论组名称
     */
    private fun getDiscussName(msg: DiscussMessage): String {
        return getDiscuss(msg).name
    }

    /**
     * 获取群消息所在群

     * @param msg 被查询的群消息
     * *
     * @return 该消息所在群
     */
    private fun getGroup(msg: GroupMessage): Group {
        return groupFromID[msg.groupId] ?: throw RuntimeException()
    }

    /**
     * 获取讨论组消息所在讨论组

     * @param msg 被查询的讨论组消息
     * *
     * @return 该消息所在讨论组
     */
    private fun getDiscuss(msg: DiscussMessage): Discuss {
        return discussFromID[msg.discussId] ?: throw RuntimeException()
    }

    /**
     * 获取私聊消息发送者昵称

     * @param msg 被查询的私聊消息
     * *
     * @return 该消息发送者
     */
    private fun getFriendNick(msg: Message): String {
        val user = friendFromID[msg.userId]
        return user?.markname ?: user?.nickname ?: throw RuntimeException()
    }

    /**
     * 获取群消息发送者昵称

     * @param msg 被查询的群消息
     * *
     * @return 该消息发送者昵称
     */
    private fun getGroupUserNick(msg: GroupMessage): String {
        getGroupInfoFromID(msg.groupId)
        return try {
            getGroupUserNick(groupUsersFromID[msg.groupId]?.get(msg.userId) ?: throw RuntimeException())
        } catch (e: RuntimeException) {
            return "系统消息" //若在群成员列表中查询不到，则为系统消息
        }
        //TODO: 也有可能是新加群的用户或匿名用户
    }

    /**
     * 获取群消息发送者昵称

     * @param user 被查询的群消息
     * *
     * @return 该消息发送者昵称
     */
    private fun getGroupUserNick(user: GroupUser): String {
        return user.card ?: user.nick
//        return if (user.card == null || user.card == "") {
//            user.nick //若发送者无群名片则返回其昵称
//        } else {
//            user.card //否则返回其群名片
//        }
    }

    private fun getGroupUserNick(gid: Long, uid: Long)
            = getGroupUserNick(groupUsersFromID[gid]?.get(uid) ?: throw RuntimeException())

    /**
     * 获取讨论组消息发送者昵称

     * @param msg 被查询的讨论组消息
     * *
     * @return 该消息发送者昵称
     */
    private fun getDiscussUserNick(msg: DiscussMessage): String {
        getDiscussInfoFromID(msg.discussId).users.forEach { user ->
            if (user.uin == msg.userId) {
                return user.nick //返回发送者昵称
            }
        }
        return "系统消息" //若在讨论组成员列表中查询不到，则为系统消息
        //TODO: 也有可能是新加讨论组的用户
    }

    val root = TreeNode("", null) {
    }
    val sudo = TreeNode("sudo", root) {
    }
    val sudoIgnore = TreeNode("ignore", sudo) {
        val uin = java.lang.Long.parseLong(it)
        if (currentMessage.userId == owner) {
            if (!ignored.contains(uin)) ignored.add(uin)
            reply("${getGroupUserNick(currentGroupId, uin)}已被屏蔽，然而这么做是不是 不太好…… |•ω•`)")
            save()
        } else if (admin.contains(currentMessage.userId) && !admin.contains(uin)) {
            if (!ignored.contains(uin)) ignored.add(uin)
            reply("${getGroupUserNick(currentGroupId, uin)}已被屏蔽，然而这么做是不是不太好…… |•ω•`)")
            save()
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
        }
    }
    val sudoRecognize = TreeNode("recognize", sudo) {
        val uin = java.lang.Long.parseLong(it)
        if (currentMessage.userId == owner) {
            if (ignored.contains(uin)) ignored.remove(uin)
            reply("${getGroupUserNick(currentGroupId, uin)}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
            save()
        } else if (admin.contains(currentMessage.userId) && !admin.contains(uin)) {
            if (ignored.contains(uin)) ignored.remove(uin)
            reply("${getGroupUserNick(currentGroupId, uin)}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
            save()
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
        }
    }
    val sudoAuthorize = TreeNode("authorize", sudo) {
        val uin = java.lang.Long.parseLong(it)
        if (currentMessage.userId == owner) {
            if (!admin.contains(uin)) {
                admin.add(uin)
                reply("${getGroupUserNick(currentGroupId, uin)}已被设置为管理员啦 |•ω•`)")
                save()
            } else {
                reply("${getGroupUserNick(currentGroupId, uin)}已经是管理员了， 再设置一次有什么好处么…… |•ω•`)")
            }
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧|･ω･｀)")
        }
    }
    val sudoUnauthorize = TreeNode("unauthorize", sudo) {
        val uin = java.lang.Long.parseLong(it)
        if (currentMessage.userId == owner) {
            if (currentMessage.userId == owner) {
                reply("${getGroupUserNick(currentGroupId, uin)}是云裂的主人哦，不能被取消管理员身份…… |•ω•`)")
            } else if (admin.contains(uin)) {
                admin.remove(uin)
                reply("${getGroupUserNick(currentGroupId, uin)}已被取消管理员身份……不过，真的要这样么 |•ω•`)")
                save()
            } else {
                reply("${getGroupUserNick(currentGroupId, uin)}并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)")
            }
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
        }
    }
    val sudoPause = TreeNode("pause", sudo) {
        if (admin.contains(currentMessage.userId)) {
            if (!(cylsGroupFromID[currentMessage.groupId]?.isPaused ?: false)) {
                println(2333)
                client.sendMessageToGroup(currentMessage.groupId, "通讯已中断（逃 |•ω•`)")
                cylsGroupFromID[currentMessage.groupId]?.isPaused = true
            } else {
                client.sendMessageToGroup(currentMessage.groupId, "已处于中断状态了啊……不能再中断一次了 |•ω•`)")
            }
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
        }
    }
    val sudoResume = TreeNode("resume", sudo) {
        if (admin.contains(currentMessage.userId)) {
            if (cylsGroupFromID[currentMessage.groupId]?.isPaused ?: true) {
                reply("通讯恢复啦 |•ω•`)")
                cylsGroupFromID[currentMessage.groupId]?.isPaused = false
            } else {
                reply("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
            }
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
        }
    }
    val sudoCheck = TreeNode("check", sudo) {
        reply("自检完毕\n一切正常哦|･ω･｀)")
    }
    val sudoTest = TreeNode("test", sudo) {
        if (currentMessage.userId == owner) {
            reply("你是云裂的主人哦|•ω•`)")
            reply("输入cyls.help.sudo查看……说好的主人呢，" +
                    "为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴")
        } else if (admin.contains(currentMessage.userId)) {
            reply("你是云裂的管理员呢|•ω•`)")
            reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
        } else {
            reply("你暂时只是个普通成员呢……|•ω•`)")
            reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
        }
    }
    var sudoSay = TreeNode("say", sudo) {
        if (admin.contains(currentMessage.userId)) {
            client.sendMessageToGroup(currentMessage.groupId, it)
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
        }
    }
    val sudoSave = TreeNode("save", sudo) {
        if (currentMessage.userId == owner) {
            save()
            reply("已保存完毕|•ω•`)")
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
        }
    }
    val sudoLoad = TreeNode("load", sudo) {
        if (currentMessage.userId == owner) {
            load()
            reply("已读取完毕|•ω•`)")
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
        }
    }
    val sudoSetup = TreeNode("setup", sudo) {
        if (currentMessage.userId == owner) {
            setup()
            reply("已更新信息完毕|•ω•`)")
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
        }
    }
    val sudoQuit = TreeNode("quit", sudo) {
        if (currentMessage.userId == owner) {
            reply("尝试关闭通讯中…… |•ω•`)")
            reply("通讯已关闭，大家再……")
            save()
            System.exit(0)
        } else {
            reply("你的权限不足哦")
            reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
        }
    }
    val util = TreeNode("util", root) {
    }
    val utilQuery = TreeNode("query", util) {
        reply("开始查找|•ω•`)")
        val groupUsers = groupUsersFromID[currentMessage.groupId] ?: throw RuntimeException()
        for ((uid, user) in groupUsers) {
            if (getGroupUserNick(user).contains(it)) {
                reply("$uid:${getGroupUserNick(user)}")
                Thread.sleep(100)
            }
        }
    }
    val utilWeather = TreeNode("weather", util) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.size >= 2) {
            reply(Util.getWeather(strs[0], strs[1].toInt()))
        } else reply("请输入城市名与天数|•ω•`)")
    }
    val utilWeatherDay0 = TreeNode("day0", utilWeather) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.isNotEmpty()) {
            reply(Util.getWeather(strs[0], 0))
        } else reply("请输入城市名|•ω•`)")
    }
    val utilWeatherDay1 = TreeNode("day1", utilWeather) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.isNotEmpty()) {
            reply(Util.getWeather(strs[0], 1))
        } else reply("请输入城市名|•ω•`)")
    }
    val utilWeatherDay2 = TreeNode("day2", utilWeather) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.isNotEmpty()) {
            reply(Util.getWeather(strs[0], 2))
        } else reply("请输入城市名|•ω•`)")
    }
    val utilWeatherToday = TreeNode("today", utilWeather) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.isNotEmpty()) {
            reply(Util.getWeather(strs[0], 0))
        } else reply("请输入城市名|•ω•`)")
    }
    val utilWeatherTomorrow = TreeNode("tomorrow", utilWeather) {
        var str = it.replace("  ", " ")
        if (str.startsWith(" ")) str = str.substring(1)
        val strs = str.split(" ")
        if (strs.isNotEmpty()) {
            reply(Util.getWeather(strs[0], 1))
        } else reply("请输入城市名|•ω•`)")
    }
    val help = TreeNode("help", root) {
    }
    val helpSudo = TreeNode("sudo", help) {
        if (currentMessage.userId == owner) {
            reply("""你可是云裂的主人呢，连这都不知道 |•ω•`)
可以让云裂屏蔽与解除屏蔽任何一名成员
cyls.sudo.ignore/recognize uid
可以将其他成员设置为云裂的管理员或取消管理员身份
cyls.sudo.authorize/unauthorize uid
可以进行通讯的中断与恢复
cyls.sudo.pause/resume
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
可以让云裂说特定的内容
cyls.sudo.say 要说的话
还可以终止连接
cyls.sudo.quit
看你的权限这么多，你还全忘了 |•ω•`)""")
        } else if (admin.contains(currentMessage.userId)) {
            reply("""你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)
可以让云裂屏蔽与解除屏蔽任何一名成员
cyls.sudo.ignore/recognize uid
可以进行通讯的中断与恢复
cyls.sudo.pause/resume
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
可以让云裂说特定的内容
cyls.sudo.say 要说的话""")
        } else {
            reply("""你是普通成员，权限有限呢|•ω•`)
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
不如向主人申请权限吧|•ω•`)""")
        }
    }

    val regexVerifier = RegexVerifier(arrayListOf(
            RegexNode(".*表白.*") {
                val result = ToAnalysis.parse(it)
                if (it.matches(".*表白云裂.*".toRegex()))
                    reply("表白${getGroupUserNick(currentGroupId, currentUserId)}|•ω•`)")
                else if (result.filter { it.realName == "表白" }.isNotEmpty()) reply("表白+1 |•ω•`)")
            },
            RegexNode(".*(有没有.*|有.{1,5}吗)") {
                if (it.contains("钱|毒|迪兰特".toRegex())) reply("有（逃|•ω•`)") else reply("没有（逃|•ω•`)")
            },
            RegexNode("(.*自检.*云裂.*)|(.*云裂.*自检.*)") {
                reply("自检完毕\n一切正常哦|･ω･｀)")
            },
            RegexNode(".*云裂.*") {
                val result = ToAnalysis.parse(it)
                if (result.filter { it.realName == "云裂" }.isNotEmpty())
                    reply("叫我做什么|•ω•`)")
            }
    ))

    @JvmStatic fun main(args: Array<String>) {
        ToAnalysis.parse("233").toString() //初始化分词库，无实际作用

        client = SmartQQClient(callback)
        setup()

        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 和 讨论组id到讨论组详情映射 将在第一次请求时创建
        //TODO: 可考虑在出现第一条讨论组消息时再建立相关映射，以防Api错误返回
    }
}
