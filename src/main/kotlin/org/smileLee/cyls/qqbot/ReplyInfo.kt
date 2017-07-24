package org.smileLee.cyls.qqbot

import org.smileLee.cyls.util.Util

abstract class ReplyInfo(private val replies: Array<Array<String>>) {
    fun replyTo(replier: Replier) = Util.itemByChance(*replies).forEach {
        replier.reply(it)
        Thread.sleep(200)
    }

    fun replyToByChance(replier: Replier, chance: Double) = Util.runByChance(chance) {
        replyTo(replier)
    }
}