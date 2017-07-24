package org.smileLee.cyls.cyls

import com.alibaba.fastjson.JSON
import com.scienjus.smartqq.constant.ApiURL
import com.scienjus.smartqq.model.GroupMessage
import com.scienjus.smartqq.model.Message
import org.smileLee.cyls.qqbot.QQBot
import org.smileLee.cyls.qqbot.Replier
import org.smileLee.cyls.util.Util
import org.smileLee.cyls.util.WebUtil
import java.io.File
import java.util.*

class Cyls(infoName: String) : QQBot<Cyls>(infoName) {
    override fun getThis() = this

    override val qqBotName = "cyls"

    override var data = CylsData()

    override var currentGroupMessage: GroupMessage = GroupMessage()
    override var currentFriendMessage: Message = Message()
    override val currentGroupId get() = currentGroupMessage.groupId
    override val currentGroupUserId get() = currentGroupMessage.userId
    override val currentFriendId get() = currentFriendMessage.userId ?: 0
    override val currentGroup get() = data._cylsGroupFromId[currentGroupMessage.groupId] ?:
            throw IllegalStateException("group not exist")
    override val currentGroupUser get() = data._cylsFriendFromId[currentGroupMessage.userId] ?:
            throw IllegalStateException("group user not exist")
    override val currentFriend get() = data._cylsFriendFromId[currentFriendMessage.userId] ?:
            throw IllegalStateException("friend not exist")

    override lateinit var loggerFile: File

    override fun illegalOrderReaction(replier: Replier) {
        replier.reply("请确保输入了正确的指令哦|•ω•`)")
    }

    override fun testIfReplyToFriend(content: String) = if (!currentFriend.isIgnored && currentFriend.isRepeated) {
        Util.runByChance(currentGroupUser.repeatFrequency) {
            currentFriendReplier.reply(content)
        }
        false
    } else true

    override fun testIfReplyToGroup(content: String) =
            if (!currentGroup.isPaused && !currentGroupUser.isIgnored && !currentGroup.hot) {
                currentGroup.addMessage()
                when {
                    currentGroupUser.isRepeated
                         -> {
                        Util.runByChance(currentGroupUser.repeatFrequency) {
                            currentGroupReplier.reply(content)
                        }
                        false
                    }
                    currentGroup.isRepeated
                         -> {
                        Util.runByChance(currentGroup.repeatFrequency) {
                            currentGroupReplier.reply(content)
                        }
                        false
                    }
                    else -> true
                }
            } else false

    private val savedFileName = "cylsData/savedFile.txt"
    private val savedFile = File(savedFileName)
    private val qrCodeFileName = "cylsData/qrcode.png"
    override val qrCodeFile = File(qrCodeFileName)

    /**
     * 加载群信息等
     */
    override fun load() {
        val json = savedFile.readText()
        data = JSON.parseObject(json, CylsData::class.java)
        working = false
        println("[${Util.timeName}] 开始建立索引，暂停接收消息")
        super.load()
        Util.doWithLog("设置主人") {
            initOwner(ownerName, data.cylsFriendList)
        }
        println("[${Util.timeName}] 索引建立完毕，开始接收消息\n")
        working = true                                            //为防止请求过多导致服务器启动自我保护
    }                                                             //群id到群详情映射 和 讨论组id到讨论组详情映射 将在第一次请求时创建

    /**
     * 储存群信息等
     */
    override fun save() = savedFile.outputStream().apply { write(JSON.toJSON(data).toString().toByteArray()) }.close()

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

    override fun setupInfo() {
        Properties().apply {
            load(loggerInfo.inputStream())
            ownerName = getProperty("owner")
            weatherKey = getProperty("weatherKey")
            loggerFile = File("cylsData/chattingLog[${Util.getTimeName("yyyy-MM-dd-hh-mm-ss")}].txt")
        }
    }

    companion object {
        fun initOwner(
                ownerName: String,
                cylsFriendList: ArrayList<CylsFriend>
        ) {
            cylsFriendList.filter { it.markName == ownerName }
                    .forEach { it.adminLevel = CylsFriend.AdminLevel.OWNER }
        }
    }
}