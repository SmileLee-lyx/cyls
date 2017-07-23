package org.smileLee.cyls.cyls

import org.smileLee.cyls.util.*

abstract class ReplyInfo(private val replies: Array<Array<String>>) {
    fun replyTo(replier: Replier) = Util.itemByChance(*replies).forEach {
        replier.reply(it)
        Thread.sleep(200)
    }

    fun replyToByChance(replier: Replier, chance: Double) = Util.runByChance(chance) {
        replyTo(replier)
    }
}