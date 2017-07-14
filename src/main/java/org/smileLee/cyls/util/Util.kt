package org.smileLee.cyls.util

import java.text.*
import java.util.*

object Util {
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

    fun randomInt(x: Int) = (Math.random() * x).toInt()
    fun sign(x: Int) = if (x > 0) 1 else if (x < 0) -1 else 0

    inline fun runByChance(chance: Double, action: () -> Unit) {
        if (Math.random() < chance) action()
    }

    inline fun <T> runByChance(chance: Double, a: () -> T, b: () -> T)
            = if (Math.random() < chance) a() else b()

    fun <T> runByChance(vararg actions: () -> T): T {
        val index = randomInt(actions.size)
        return actions[index]()
    }

    fun <T> itemByChance(vararg items: T): T {
        val index = randomInt(items.size)
        return items[index]
    }
}