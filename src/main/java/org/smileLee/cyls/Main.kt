package org.smileLee.cyls

import com.alibaba.fastjson.*
import com.scienjus.smartqq.callback.*
import com.scienjus.smartqq.client.*
import com.scienjus.smartqq.constant.*
import com.scienjus.smartqq.model.*
import org.ansj.splitWord.analysis.*
import org.smileLee.cyls.cyls.*
import org.smileLee.cyls.util.*
import org.smileLee.cyls.util.Util.itemByChance
import org.smileLee.cyls.util.Util.runByChance
import org.smileLee.cyls.util.Util.time
import sun.dc.path.*
import java.io.*
import java.lang.Thread.*

/**
 * @author 2333
 */
object Main {
    val MAX_RETRY = 3
    private inline fun <T> retry(action: () -> T): T {
        for (retry in 0..MAX_RETRY) {
            try {
                return action()
            } catch (e: Exception) {
                if (retry != MAX_RETRY) {
                    println("[$time] 第${retry + 1}次尝试失败。正在重试...")
                }
            }
        }
        println("[$time] 重试次数达到最大限制，程序无法继续进行。")
        System.exit(1)
        throw Error("Unreachable code")
    }

    private var working = true

    private var data = Data()

    private var currentMessage: GroupMessage = GroupMessage()
    private val currentGroupId get() = currentMessage.groupId
    private val currentGroup get() = data._cylsGroupFromId[currentMessage.groupId]!!
    private val currentUser get() = data._cylsFriendFromId[currentMessage.userId]!!

    fun reply(message: String) {
        println("[$time] [${currentGroup.name}] > $message")
        client.sendMessageToGroup(currentMessage.groupId, message)
    }

    /**
     * SmartQQ客户端
     */
    private lateinit var client: SmartQQClient
    val callback = object : MessageCallback {
        override fun onMessage(message: Message) {
            if (working) {
                try {
                    println("[$time] [私聊] ${getFriendNick(message.userId)}：${message.content}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onGroupMessage(message: GroupMessage) {
            if (working) {
                try {
                    val content = message.content!!
                    println("[$time] [${getGroupName(message.groupId)}] " +
                            "${getGroupUserNick(message.groupId, message.userId)}：$content")
                    currentMessage = message
                    currentGroup
                    currentUser
                    if (content.startsWith("cyls.")) try {
                        val order = Util.readOrder(content.substring(5))
                        root.findPath(order.path).run(order.message)
                    } catch (e: PathException) {
                        reply("请确保输入了正确的指令哦|•ω•`)")
                    } else {
                        if (!currentGroup.isPaused && !currentUser.isIgnored && !currentGroup.hot
                                && getGroupUserNick(message.groupId, message.userId) != "系统消息") {
                            currentGroup.addMessage()
                            if (currentUser.isRepeated) {
                                runByChance(currentUser.repeatFrequency) {
                                    reply(content)
                                }
                            } else {
                                regexVerifier.findAndRun(content)
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    val savedFileName = "cylsData/savedFile.txt"
    val savedFile = File(savedFileName)
    val qrCodeFileName = "cylsData/qrcode.png"
    val qrCodeFile = File(qrCodeFileName)

    /**
     * 加载群信息等
     */
    private fun load() {
        val file = savedFile
        val fin = FileInputStream(file)
        val length = fin.available()
        val bytes = ByteArray(length)
        fin.read(bytes)
        val json = String(bytes)
        data = JSON.parseObject(json, Data::class.java)
        working = false   //映射建立完毕前暂停接收消息以避免NullPointerException
        println()
        println("[$time] 开始建立索引，暂停接收消息")
        println("[$time] 尝试建立好友列表索引...")
        val friendList = retry { client.friendList }
        retry {
            friendList.forEach { friend ->
                data.cylsFriendList.filter { cylsFriend -> cylsFriend.markName == friend.markname }
                        .forEach { cylsFriend -> cylsFriend.set(friend) }
                data.cylsFriendFromId[friend.userId].set(friend)
            }
        }
        println("[$time] 建立好友列表索引成功。")
        println("[$time] 尝试建立群列表索引...")
        val groupList = retry { client.groupList }
        retry {
            groupList.forEach { group ->
                data.cylsGroupList.filter { cylsGroup -> cylsGroup.name == group.name }
                        .forEach { cylsGroup -> cylsGroup.set(group) }
                data.cylsGroupFromId[group.id].set(group)
            }
        }
        println("[$time] 建立群列表索引成功。")
        data.cylsFriendList.forEach {
            if (it.markName == "smileLee") it.adminLevel = CylsFriend.AdminLevel.OWNER
        }
        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 和 讨论组id到讨论组详情映射 将在第一次请求时创建
        println("[$time] 索引建立完毕，开始接收消息\n")
        working = true                                     //映射建立完毕后恢复工作
    }

    /**
     * 储存群信息等
     */
    private fun save() {
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
    private fun getGroupInfoFromID(id: Long): GroupInfo {
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
    private fun getGroupName(id: Long): String {
        return getGroup(id).name
    }

    /**
     * 获取群消息所在群

     * @param id 被查询的群消息
     * *
     * @return 该消息所在群
     */
    private fun getGroup(id: Long): CylsGroup {
        return data.cylsGroupFromId[id]
    }

    /**
     * 获取私聊消息发送者昵称

     * @param id 被查询的私聊消息
     * *
     * @return 该消息发送者
     */
    private fun getFriendNick(id: Long): String {
        val user = data.cylsFriendFromId[id].friend
        return user?.markname ?: user?.nickname ?: null!!
    }

    private fun getGroupUserNick(gid: Long, uid: Long): String {
        getGroupInfoFromID(gid)
        val user = data.cylsGroupFromId[gid].groupUsersFromId[uid]
        return user.card ?: user.nick ?: null!!
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
            } catch(e: Exception) {
                reply("啊呀，真抱歉，查询失败的说，请确认这个地名是国内的城市名……|•ω•`)")
            }
        }
    }

    val root = createTree("", {

    }) {
        childNode("sudo", {
            reply("""输入
cyls.help.sudo
查看帮助信息|•ω•`)""")
        }) {
            childNode("ignore", {
                val uin = it.toLong()
                val destUser = data._cylsFriendFromId[uin] ?: {
                    reply("未找到此人哦|•ω•`)")
                    null!!
                }()
                if (currentUser.isOwner || (currentUser.isAdmin && !destUser.isAdmin)) {
                    if (!destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.IGNORED
                    reply("${getGroupUserNick(currentGroupId, uin)}已被屏蔽，然而这么做是不是不太好…… |•ω•`)")
                    save()
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            })
            childNode("recognize", {
                val uin = it.toLong()
                val destUser = data._cylsFriendFromId[uin] ?: {
                    reply("未找到此人哦|•ω•`)")
                    null!!
                }()
                if (currentUser.isOwner || (currentUser.isAdmin && !destUser.isAdmin)) {
                    if (destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.RECOGNIZED
                    reply("${getGroupUserNick(currentGroupId, uin)}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
                    save()
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            })
            childNode("authorize", {
                val uin = it.toLong()
                val destUser = data._cylsFriendFromId[uin] ?: {
                    reply("未找到此人哦|•ω•`)")
                    null!!
                }()
                if (currentUser.isOwner) {
                    if (!destUser.isAdmin) {
                        destUser.adminLevel = CylsFriend.AdminLevel.ADMIN
                        reply("${getGroupUserNick(currentGroupId, uin)}已被设置为管理员啦 |•ω•`)")
                        save()
                    } else {
                        reply("${getGroupUserNick(currentGroupId, uin)}已经是管理员了， 再设置一次有什么好处么…… |•ω•`)")
                    }
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧|･ω･｀)")
                }
            })
            childNode("unauthorize", {
                val uin = it.toLong()
                val destUser = data._cylsFriendFromId[uin] ?: {
                    reply("未找到此人哦|•ω•`)")
                    null!!
                }()
                if (currentUser.isOwner) {
                    if (destUser.isOwner) {
                        reply("${getGroupUserNick(currentGroupId, uin)}是云裂的主人哦，不能被取消管理员身份…… |•ω•`)")
                    } else if (destUser.isAdmin) {
                        currentUser.adminLevel = CylsFriend.AdminLevel.NORMAL
                        reply("${getGroupUserNick(currentGroupId, uin)}已被取消管理员身份……不过，真的要这样么 |•ω•`)")
                        save()
                    } else {
                        reply("${getGroupUserNick(currentGroupId, uin)}并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)")
                    }
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                }
            })
            childNode("pause", {
                if (currentUser.isAdmin) {
                    if (!currentGroup.isPaused) {
                        println(2333)
                        client.sendMessageToGroup(currentMessage.groupId, "通讯已中断（逃 |•ω•`)")
                        currentGroup.isPaused = true
                        save()
                    } else {
                        client.sendMessageToGroup(currentMessage.groupId, "已处于中断状态了啊……不能再中断一次了 |•ω•`)")
                    }
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            })
            childNode("resume", {
                if (currentUser.isAdmin) {
                    if (currentGroup.isPaused) {
                        reply("通讯恢复啦 |•ω•`)")
                        currentGroup.isPaused = false
                        save()
                    } else {
                        reply("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
                    }
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            })
            childNode("repeat", {
                if (currentUser.isAdmin) {
                    reply("请选择重复对象哦|•ω•`)")
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            }) {
                childNode("friend", {
                    if (currentUser.isAdmin) {
                        reply("请选择重复模式哦|•ω•`)")
                    } else {
                        reply("你的权限不足哦")
                        reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("on", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isEmpty()) {
                            reply("请输入被重复的用户的uin|•ω•`)")
                            null!!
                        }
                        val uin = strs[0].toLong()
                        val destUser = data._cylsFriendFromId[uin] ?: {
                            reply("未找到此人哦|•ω•`)")
                            null!!
                        }()
                        val frequency = strs[1].toDoubleOrNull() ?: 0.3
                        if (currentUser.isOwner || (currentUser.isAdmin && !destUser.isAdmin)) {
                            if (!destUser.isRepeated) {
                                destUser.isRepeated = true
                                destUser.repeatFrequency = frequency
                                reply("${getGroupUserNick(currentGroupId, uin)}的话将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                save()
                            } else {
                                destUser.repeatFrequency = frequency
                                reply("重复${getGroupUserNick(currentGroupId, uin)}的话的概率被设置为$frequency |•ω•`)")
                                save()
                            }
                        } else {
                            reply("你的权限不足哦")
                            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    })
                    childNode("off", {
                        val uin = it.toLong()
                        val destUser = data._cylsFriendFromId[uin] ?: {
                            reply("未找到此人哦|•ω•`)")
                            null!!
                        }()
                        if (currentUser.isOwner || (currentUser.isAdmin && !destUser.isAdmin)) {
                            if (!destUser.isRepeated) {
                                reply("${getGroupUserNick(currentGroupId, uin)}并没有被重复啊 |•ω•`)")
                            } else {
                                destUser.isRepeated = false
                                reply("${getGroupUserNick(currentGroupId, uin)}已被取消重复 |•ω•`)")
                                save()
                            }
                        } else {
                            reply("你的权限不足哦")
                            reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    })
                }
            }
            childNode("check", {
                reply("自检完毕\n一切正常哦|･ω･｀)")
            })
            childNode("test", {
                if (currentUser.isOwner) {
                    reply("你是云裂的主人哦|•ω•`)")
                    reply("输入cyls.help.sudo查看……说好的主人呢，" +
                            "为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴")
                } else if (currentUser.isAdmin) {
                    reply("你是云裂的管理员呢|•ω•`)")
                    reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                } else {
                    reply("你暂时只是个普通成员呢……|•ω•`)")
                    reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                }
            })
            childNode("say", {
                if (currentUser.isAdmin) {
                    client.sendMessageToGroup(currentMessage.groupId, it)
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                }
            })
            childNode("save", {
                if (currentUser.isOwner) {
                    save()
                    reply("已保存完毕|•ω•`)")
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                }
            })
            childNode("load", {
                if (currentUser.isOwner) {
                    load()
                    reply("已读取完毕|•ω•`)")
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                }
            })
            childNode("quit", {
                if (currentUser.isOwner) {
                    reply("尝试关闭通讯中…… |•ω•`)")
                    reply("通讯已关闭，大家再……")
                    save()
                    System.exit(0)
                } else {
                    reply("你的权限不足哦")
                    reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                }
            })
        }
        childNode("util", {
            reply("""输入
cyls.help.util
查看帮助信息|•ω•`)""")
        }) {
            childNode("query", {
                reply("开始查找|•ω•`)")
                val groupUsers = getGroupInfoFromID(currentGroupId).users
                for (user in groupUsers) {
                    val userName = getGroupUserNick(currentGroupId, user.uin)
                    if (userName.contains(it)) {
                        reply("${user.uin}:$userName")
                        sleep(100)
                    }
                }
            })
            childNode("weather", {
                var str = it.replace("  ", " ")
                if (str.startsWith(" ")) str = str.substring(1)
                val strs = str.split(" ")
                if (strs.size >= 2) {
                    getWeather(strs[0], strs[1].toInt())
                } else reply("请输入城市名与天数|•ω•`)")
            }) {
                childNode("day0", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.isNotEmpty()) {
                        getWeather(strs[0], 0)
                    } else reply("请输入城市名|•ω•`)")
                })
                childNode("day1", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.isNotEmpty()) {
                        getWeather(strs[0], 1)
                    } else reply("请输入城市名|•ω•`)")
                })
                childNode("day2", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.isNotEmpty()) {
                        getWeather(strs[0], 2)
                    } else reply("请输入城市名|•ω•`)")
                })
                childNode("today", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.isNotEmpty()) {
                        getWeather(strs[0], 0)
                    } else reply("请输入城市名|•ω•`)")
                })
                childNode("tomorrow", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.isNotEmpty()) {
                        getWeather(strs[0], 1)
                    } else reply("请输入城市名|•ω•`)")
                })
            }
            childNode("cal", {
                val expression = it.replace("&gt;", ">").replace("&lt;", "<")
                reply("结果是：${currentGroup.calculate(expression)}")
            })
        }
        childNode("help", {
            reply("""欢迎来到帮助系统|•ω•`)
cyls.help.sudo
查看关于云裂控制系统的帮助
cyls.help.util
查看云裂工具箱的帮助
更多功能等待你去发现哦|•ω•`)""")
        }) {
            childNode("sudo", {
                if (currentUser.isOwner) {
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
                } else if (currentUser.isAdmin) {
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
            })
            childNode("util", {
                reply("""目前云裂的工具功能还不怎么完善呢|•ω•`)
你可以查询群成员：
cyls.util.query [群名片的一部分]
你可以查询天气：
cyls.util.weather
可以进行简单的计算：
cyls.util.cal [表达式/代码块]
关于天气功能的更多内容，输入
cyls.help.util.weather
来查看哦|•ω•`)""")
            }) {
                childNode("weather", {
                    reply("""
云裂的天气查询功能目前只能查到近几日的天气|•ω•`)
[天数]: 0 -> 今天, 1 -> 明天, 2 -> 后天
cyls.util.weather [城市名] [天数]
cyls.util.weather.today [城市名]
cyls.util.weather.tomorrow [城市名]
cyls.util.weather.day[天数] [城市名]
例如：
cyls.util.weather.day2 无锡
查询无锡后天的天气。
""")
                })
            }
        }
    }

    val regexVerifier = createVerifier {
        regex(".*表白.*") {
            val result = ToAnalysis.parse(it)
            if (it.matches(".*表白云裂.*".toRegex()))
                reply("表白${getGroupUserNick(currentMessage.groupId, currentMessage.userId)}|•ω•`)")
            else if (result.filter { it.realName == "表白" }.isNotEmpty()) reply("表白+1 |•ω•`)")
        }
        regex("(\\[\"face\",\\d+])*晚安(\\[\"face\",\\d+])*") {
            val hasGreeted = currentGroup.hasGreeted
            currentGroup.addGreeting()
            if (!hasGreeted) reply("晚安，好梦|•ω•`)")
        }
        regex("(\\[\"face\",\\d+])*早安?(\\[\"face\",\\d+])*") {
            val hasGreeted = currentGroup.hasGreeted
            currentGroup.addGreeting()
            if (!hasGreeted) reply("早|•ω•`)")
        }
        regex(".*(有没有.*|有.{1,5}吗)") {
            reply(itemByChance(
                    "没有（逃|•ω•`)",
                    "有（逃|•ω•`)"
            ))
        }
        regex(".*(是不是.*|是.{1,5}吗)") {
            reply(itemByChance(
                    "不是（逃|•ω•`)",
                    "是（逃|•ω•`)"
            ))
        }
        regex("(.*自检.*云裂.*)|(.*云裂.*自检.*)") {
            reply("自检完毕\n一切正常哦|•ω•`)")
        }
        regex(".*云裂.*") {
            val result = ToAnalysis.parse(it)
            if (result.filter { it.realName == "云裂" }.isNotEmpty())
                reply("叫我做什么|•ω•`)")
        }
        regex(".*(大新闻|知识水平|谈笑风生|太暴力了|这样暴力|暴力膜|高到不知道?那里去|报道上?([江将]来)?(要是)?出了偏差|江来|泽任|民白|(我(今天)?)?(算是)?得罪了?你们?一下|批判一番|真正的粉丝).*") {
            reply(itemByChance(
                    "不要整天搞个大新闻|•ω•`)",
                    "迪兰特比你们不知道高到哪里去了，我和他谈笑风生|•ω•`)",
                    "你们还是要提高自己的知识水平|•ω•`)",
                    "你们这样是要被拉出去续的|•ω•`)",
                    "江来报道上出了偏差，你们是要负泽任的，民不民白?|•ω•`)",
                    "我今天算是得罪了你们一下|•ω•`)",
                    "真正的粉丝……|•ω•`)"
            ))
        }
        regex("苟(.|…|。|\\[\"face\",\\d+])*") {
            reply("富贵，无相忘|•ω•`)")
        }
        regex(".*(这种操作|什么操作|新的操作).*") {
            reply("一直都有这种操作啊|•ω•`)")
        }
        regex(".*((什么)?原因么?要?(自己)?找一?找?|什么原因|引起重视|知名度).*") {
            reply(itemByChance(
                    "什么原因么自己找一找|•ω•`)",
                    "这个么要引起重视|•ω•`)",
                    "lw:我的知名度很高了，不用你们宣传了|•ω•`)"
            ))
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        ToAnalysis.parse("233").toString() //初始化分词库，无实际作用

        client = SmartQQClient(callback, qrCodeFile)
        client.start()
        load()

        //为防止请求过多导致服务器启动自我保护
        //群id到群详情映射 将在第一次请求时创建
    }
}
