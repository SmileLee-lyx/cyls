package org.smileLee.cyls

import com.alibaba.fastjson.*
import java.text.*
import java.util.*

object Util {
    private val weatherKey = "3511aebb46e04a59b77da9b1c648c398"               //天气查询密钥
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

    fun String.firstIndexOf(vararg char: Char): Int {
        var ret = length
        fun check(x: Int) = if (x != -1) x else length
        char.forEach {
            ret = minOf(ret, check(indexOf(it)))
        }
        return ret
    }

    /**
     * 将指令转为路径
     */
    fun readOrder(string: String): Order {
        var str = string
        val path = ArrayList<String>()
        while (true) {
            val dotIndex = str.firstIndexOf('.')
            val blankIndex = str.firstIndexOf(' ', '\n')
            if (blankIndex == dotIndex) {
                path.add(str)
                return Order(path, "")
            } else if (dotIndex < blankIndex) {
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

    inline fun byChance(chance: Double, action: () -> Unit) {
        if (Math.random() < chance) action()
    }

    inline fun <T> byChance(chance: Double, a: () -> T, b: () -> T)
            = if (Math.random() < chance) a() else b()
}