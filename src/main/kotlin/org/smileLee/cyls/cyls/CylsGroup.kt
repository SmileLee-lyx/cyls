package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.ansj.splitWord.analysis.*
import org.smileLee.cyls.cyls.CylsGroup.*
import org.smileLee.cyls.util.*
import org.smileLee.smilescript.expression.controlExpression.*
import org.smileLee.smilescript.stack.*
import java.lang.Thread.*

@JSONType(ignores = arrayOf(
        "hot",
        "hasGreeted",
        "group",
        "groupInfo",
        "_groupUsersFromId",
        "groupUsersFromId",
        "messageCount",
        "_messageCount",
        "_greetingCount",
        "stack"
))
class CylsGroup(
        var name: String = "",
        var isPaused: Boolean = false,
        var isRepeated: Boolean = false,
        var repeatFrequency: Double = 0.0,
        var status: ChattingStatus = ChattingStatus.COMMON
) {

    var group: Group? = null
    var groupInfo: GroupInfo? = null
    var _groupUsersFromId: HashMap<Long, GroupUser> = HashMap()
    val groupUsersFromId = InitSafeMap(_groupUsersFromId) { key ->
        groupInfo?.users?.forEach {
            if (it.uid == key) {
                _groupUsersFromId.put(key, it)
                return@InitSafeMap it
            }
        }
        null!!
    }

    val messageCount get() = _messageCount
    val hasGreeted get() = _greetingCount != 0
    val hot get() = messageCount > MAX_MESSAGE_COUNT

    private var _messageCount = 0
    private var _greetingCount = 0

    fun addMessage() {
        ++_messageCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_messageCount }).start()
    }

    fun addGreeting() {
        ++_greetingCount
        Thread(Runnable { sleep(5 * 60 * 1000); --_greetingCount }).start()
    }

    fun set(group: Group) {
        name = group.name
        this.group = group
    }

    val stack = Stack()
    fun calculate(s: String) = Block.parse(s).invoke(stack).toString()

    companion object {
        val MAX_MESSAGE_COUNT = 50

        val commonCommand = createTree {
            childNode("sudo", { _, cyls ->
                cyls.reply("""输入
cyls.help.sudo
查看帮助信息|•ω•`)""")
            }) {
                childNode("ignore", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.reply("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                        if (!destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.IGNORED
                        cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被屏蔽，然而这么做是不是不太好…… |•ω•`)")
                        cyls.save()
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("recognize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.reply("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                        if (destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.RECOGNIZED
                        cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
                        cyls.save()
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("authorize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.reply("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentUser.isOwner) {
                        if (!destUser.isAdmin) {
                            destUser.adminLevel = CylsFriend.AdminLevel.ADMIN
                            cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被设置为管理员啦 |•ω•`)")
                            cyls.save()
                        } else {
                            cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已经是管理员了， 再设置一次有什么好处么…… |•ω•`)")
                        }
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧|•ω•`)")
                    }
                })
                childNode("unauthorize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.reply("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentUser.isOwner) {
                        if (destUser.isOwner) {
                            cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}是云裂的主人哦，不能被取消管理员身份…… |•ω•`)")
                        } else if (destUser.isAdmin) {
                            cyls.currentUser.adminLevel = CylsFriend.AdminLevel.NORMAL
                            cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被取消管理员身份……不过，真的要这样么 |•ω•`)")
                            cyls.save()
                        } else {
                            cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)")
                        }
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("pause", { _, cyls ->
                    if (cyls.currentUser.isAdmin) {
                        if (!cyls.currentGroup.isPaused) {
                            println(2333)
                            cyls.reply("通讯已中断（逃 |•ω•`)")
                            cyls.currentGroup.isPaused = true
                            cyls.save()
                        } else {
                            cyls.reply("已处于中断状态了啊……不能再中断一次了 |•ω•`)")
                        }
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("resume", { _, cyls ->
                    if (cyls.currentUser.isAdmin) {
                        if (cyls.currentGroup.isPaused) {
                            cyls.reply("通讯恢复啦 |•ω•`)")
                            cyls.currentGroup.isPaused = false
                            cyls.save()
                        } else {
                            cyls.reply("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
                        }
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("repeat", { _, cyls ->
                    if (cyls.currentUser.isAdmin) {
                        cyls.reply("请选择重复对象哦|•ω•`)")
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("group", { _, cyls ->
                        if (cyls.currentUser.isAdmin) {
                            cyls.reply("请选择重复模式哦|•ω•`)")
                        } else {
                            cyls.reply("你的权限不足哦")
                            cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val frequency = str.toDoubleOrNull() ?: 0.3
                            if (cyls.currentUser.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = true
                                    cyls.currentGroup.repeatFrequency = frequency
                                    cyls.reply("本群的所有发言将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.currentGroup.repeatFrequency = frequency
                                    cyls.reply("重复本群发言的概率被设置为$frequency |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { _, cyls ->
                            if (cyls.currentUser.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = false
                                    cyls.reply("本群已取消重复 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.reply("本来就没有在重复本群的发言啊 |•ω•`)")
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                    childNode("friend", { _, cyls ->
                        if (cyls.currentUser.isAdmin) {
                            cyls.reply("请选择重复模式哦|•ω•`)")
                        } else {
                            cyls.reply("你的权限不足哦")
                            cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            var s = str.replace("  ", " ")
                            if (s.startsWith(" ")) s = s.substring(1)
                            val strs = s.split(" ")
                            if (strs.isEmpty()) {
                                cyls.reply("请输入被重复的用户的uin|•ω•`)")
                                null!!
                            }
                            val uin = strs[0].toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.reply("未找到此人哦|•ω•`)")
                                null!!
                            }
                            val frequency = strs[1].toDoubleOrNull() ?: 0.3
                            if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    destUser.isRepeated = true
                                    destUser.repeatFrequency = frequency
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}的话将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    cyls.save()
                                } else {
                                    destUser.repeatFrequency = frequency
                                    cyls.reply("重复${cyls.getGroupUserNick(cyls.currentGroupId, uin)}的话的概率被设置为$frequency |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.reply("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}并没有被重复啊 |•ω•`)")
                                } else {
                                    destUser.isRepeated = false
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被取消重复 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("moha", { _, cyls ->
                    if (cyls.currentUser.isAdmin) {
                        cyls.reply("请选择被设置为moha的对象哦|•ω•`)")
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("friend", { _, cyls ->
                        if (cyls.currentUser.isAdmin) {
                            cyls.reply("请选择moha模式哦|•ω•`)")
                        } else {
                            cyls.reply("你的权限不足哦")
                            cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.reply("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isMoha) {
                                    destUser.isMoha = true
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被设置为moha专家，真是搞个大新闻 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}本来就是moha专家啊 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.reply("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentUser.isOwner || (cyls.currentUser.isAdmin && !destUser.isAdmin)) {
                                if (destUser.isMoha) {
                                    destUser.isMoha = false
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}已被取消moha专家，一定是知识水平不够 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.reply("${cyls.getGroupUserNick(cyls.currentGroupId, uin)}本来就不是moha专家啊，为什么要搞大新闻 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.reply("你的权限不足哦")
                                cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("check", { _, cyls ->
                    cyls.reply("自检完毕\n一切正常哦|•ω•`)")
                })
                childNode("test", { _, cyls ->
                    if (cyls.currentUser.isOwner) {
                        cyls.reply("你是云裂的主人哦|•ω•`)")
                        cyls.reply("输入cyls.help.sudo查看……说好的主人呢，" +
                                "为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴")
                    } else if (cyls.currentUser.isAdmin) {
                        cyls.reply("你是云裂的管理员呢|•ω•`)")
                        cyls.reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    } else {
                        cyls.reply("你暂时只是个普通成员呢……|•ω•`)")
                        cyls.reply("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    }
                })
                childNode("say", { str, cyls ->
                    if (cyls.currentUser.isAdmin) {
                        cyls.reply(str)
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("friend", { str, cyls ->
                        val index = str.indexOf(" ")
                        val userId = str.substring(0, index).toLong()
                        val content = str.substring(index + 1)
                        cyls.client.sendMessageToFriend(userId, content)
                    })
                    childNode("group", { str, cyls ->
                        val index = str.indexOf(" ")
                        val groupId = str.substring(0, index).toLong()
                        val content = str.substring(index + 1)
                        cyls.client.sendMessageToGroup(groupId, content)
                    })
                }
                childNode("save", { _, cyls ->
                    if (cyls.currentUser.isOwner) {
                        cyls.save()
                        cyls.reply("已保存完毕|•ω•`)")
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("load", { _, cyls ->
                    if (cyls.currentUser.isOwner) {
                        cyls.load()
                        cyls.reply("已读取完毕|•ω•`)")
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("quit", { _, cyls ->
                    if (cyls.currentUser.isOwner) {
                        cyls.reply("尝试关闭通讯中…… |•ω•`)")
                        cyls.reply("通讯已关闭，大家再……")
                        cyls.save()
                        System.exit(0)
                    } else {
                        cyls.reply("你的权限不足哦")
                        cyls.reply("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
            }
            childNode("util", { _, cyls ->
                cyls.reply("""输入
cyls.help.util
查看帮助信息|•ω•`)""")
            }) {
                childNode("query", { _, cyls ->
                    cyls.reply("请选择查找的范围哦|•ω•`)")
                }) {
                    childNode("groupuser", { str, cyls ->
                        cyls.reply("开始查找|•ω•`)")
                        val groupUsers = cyls.getGroupInfoFromID(cyls.currentGroupId).users
                        groupUsers.forEach { user ->
                            val userName = cyls.getGroupUserNick(cyls.currentGroupId, user.uid)
                            if (userName.contains(str)) {
                                cyls.reply("${user.uid}:$userName")
                                sleep(100)
                            }
                        }
                    })
                    childNode("friend", { str, cyls ->
                        cyls.reply("开始查找|•ω•`)")
                        cyls.data.cylsFriendList.forEach { friend ->
                            val friendInfo = friend.friend
                            if (friendInfo != null && friendInfo.nickname.contains(str)) {
                                cyls.reply("${friendInfo.userId}:${friendInfo.nickname}")
                            }
                        }
                    })
                    childNode("group", { str, cyls ->
                        cyls.reply("开始查找|•ω•`)")
                        cyls.data.cylsGroupList.forEach { group ->
                            val groupInfo = group.group
                            if (groupInfo != null && groupInfo.name.contains(str)) {
                                cyls.reply("${groupInfo.id}:${groupInfo.name}")
                            }
                        }
                    })
                }
                childNode("weather", { str, cyls ->
                    var s = str.replace("  ", " ")
                    if (s.startsWith(" ")) s = s.substring(1)
                    val strs = s.split(" ")
                    if (strs.size >= 2) {
                        cyls.getWeather(strs[0], strs[1].toInt())
                    } else cyls.reply("请输入城市名与天数|•ω•`)")
                }) {
                    childNode("day0", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0)
                        } else cyls.reply("请输入城市名|•ω•`)")
                    })
                    childNode("day1", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1)
                        } else cyls.reply("请输入城市名|•ω•`)")
                    })
                    childNode("day2", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 2)
                        } else cyls.reply("请输入城市名|•ω•`)")
                    })
                    childNode("today", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0)
                        } else cyls.reply("请输入城市名|•ω•`)")
                    })
                    childNode("tomorrow", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1)
                        } else cyls.reply("请输入城市名|•ω•`)")
                    })
                }
                childNode("dice", { _, cyls ->
                    cyls.reply("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                    sleep(200)
                    cyls.reply("结果是：${Util.randomInt(6) + 1}")
                })
                childNode("random", { str, cyls ->
                    val x = str.toIntOrNull()
                    when (x) {
                        null                -> {
                            cyls.reply("还有这种骰子? |•ω•`)")
                            null!!
                        }
                        in Int.MIN_VALUE..1 -> {
                            cyls.reply("我这里可没有你要的面数的骰子|•ω•`)\n然而我可以现做一个")
                        }
                        2                   -> {
                            cyls.reply("这么重要的事情，你却抛硬币决定|•ω•`)")
                        }
                        6                   -> {
                            cyls.reply("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                        }
                        else                -> {
                            cyls.reply("有的人已经不满足于六面体骰子了，他们需要一个${x}面体的骰子|•ω•`)")
                        }
                    }
                    sleep(200)
                    cyls.reply("结果是：${Util.randomInt(x) + Util.sign(x)}")
                })
                childNode("cal", { str, cyls ->
                    val expression = str.replace("&gt;", ">").replace("&lt;", "<")
                    cyls.reply("结果是：${cyls.currentGroup.calculate(expression)}")
                })
            }
            childNode("help", { _, cyls ->
                cyls.reply("""欢迎来到帮助系统|•ω•`)
cyls.help.sudo
查看关于云裂控制系统的帮助
cyls.help.util
查看云裂工具箱的帮助
更多功能等待你去发现哦|•ω•`)""")
            }) {
                childNode("sudo", { _, cyls ->
                    if (cyls.currentUser.isOwner) {
                        cyls.reply("""你可是云裂的主人呢，连这都不知道 |•ω•`)
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
                    } else if (cyls.currentUser.isAdmin) {
                        cyls.reply("""你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)
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
                        cyls.reply("""你是普通成员，权限有限呢|•ω•`)
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
不如向主人申请权限吧|•ω•`)""")
                    }
                })
                childNode("util", { _, cyls ->
                    cyls.reply("""目前云裂的工具功能还不怎么完善呢|•ω•`)
你可以查询群成员：
cyls.util.query.groupuser [群名片的一部分]
查询云裂的好友：
cyls.util.query.friend [昵称的一部分]
查询群：
cyls.util.query.group [群名的一部分]
你可以查询天气：
cyls.util.weather
可以掷骰子或计算1至[最大值]的随机数:
cyls.util.dice
cyls.util.random [最大值]
可以进行简单的计算：
cyls.util.cal [表达式/代码块]
关于天气功能的更多内容，输入
cyls.help.util.weather
来查看哦|•ω•`)""")
                }) {
                    childNode("weather", { _, cyls ->
                        cyls.reply("""
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
        val commonVerifier = createVerifier {
            contain("晚安") { _, cyls ->
                val hasGreeted = cyls.currentGroup.hasGreeted
                cyls.currentGroup.addGreeting()
                if (!hasGreeted) cyls.reply("晚安，好梦|•ω•`)")
            }
            anyOf({
                contain("早安")
                equal("早")
            }) { _, cyls ->
                val hasGreeted = cyls.currentGroup.hasGreeted
                cyls.currentGroup.addGreeting()
                if (!hasGreeted) cyls.reply("早|•ω•`)")
            }
            anyOf({
                contain("有没有")
                containRegex("有.{0,5}((?<!什)么|吗)")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "没有（逃|•ω•`)",
                        "有（逃|•ω•`)"
                ))
            }
            anyOf({
                contain("是不是")
                containRegex("是.{0,5}((?<!什)么|吗)")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "不是（逃|•ω•`)",
                        "是（逃|•ω•`)"
                ))
            }
            anyOf({
                contain("会不会")
                containRegex("会.{0,5}((?<!什)么|吗)")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "不会（逃|•ω•`)",
                        "会（逃|•ω•`)"
                ))
            }
            anyOf({
                contain("喜不喜欢")
                containRegex("喜欢.{0,5}((?<!什)么|吗)")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "喜欢（逃|•ω•`)",
                        "不喜欢（逃|•ω•`)"
                ))
            }
            containPath("云裂") {
                contain("自检") { _, cyls ->
                    cyls.reply("自检完毕\n一切正常哦|•ω•`)")
                }
                default { str, cyls ->
                    val result = ToAnalysis.parse(str)
                    if (result.filter { it.realName == "云裂" || it.realName == "穿云裂石" }.isNotEmpty())
                        cyls.reply("叫我做什么|•ω•`)")
                }
            }
            contain("表白", { str, cyls ->
                val result = ToAnalysis.parse(str)
                if (str.matches(".*表白云裂.*".toRegex()))
                    cyls.reply("表白${cyls.getGroupUserNick(cyls.currentGroupMessage.groupId, cyls.currentGroupMessage.userId)}|•ω•`)")
                else if (result.filter { it.realName == "表白" }.isNotEmpty()) cyls.reply("表白+1 |•ω•`)")
            })
            anyOf({
                contain("什么操作")
                contain("这种操作")
                contain("新的操作")
            }) { _, cyls ->
                cyls.reply("一直都有这种操作啊|•ω•`)")
            }
            containRegex("你们?(再?一直再?|再?继续再?)?这样(下去)?是不行的", { _, cyls ->
                cyls.reply("再这样的话是不行的|•ω•`)")
            })
            anyOf({
                containRegex("原因么?要?(自己)?找一?找")
                contain("什么原因")
                contain("引起重视")
                contain("知名度")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "什么原因么自己找一找|•ω•`)",
                        "这个么要引起重视|•ω•`)",
                        "lw:我的知名度很高了，不用你们宣传了|•ω•`)"
                ))
            }
            special { str, cyls ->
                if (!cyls.currentUser.isMoha)
                    commonMohaVerifier.findAndRun(str, cyls)
                else
                    mohaExpertVerifier.findAndRun(str, cyls)
            }
        }

        private val commonMohaVerifier = createVerifier {
            anyOf({
                contain("大新闻")
                contain("知识水平")
                contain("谈笑风生")
                contain("太暴力了")
                contain("这样暴力")
                contain("暴力膜")
                contain("江来")
                contain("泽任")
                contain("民白")
                contain("批判一番")
                contain("真正的粉丝")
                contain("江信江疑")
                contain("听风就是雨")
                contain("长者")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "不要整天搞个大新闻|•ω•`)",
                        "你们还是要提高自己的知识水平|•ω•`)",
                        "你们这样是要被拉出去续的|•ω•`)",
                        "真正的粉丝……|•ω•`)"
                ))
            }
            anyOf({
                containRegex("高[到了]不知道?(那里去|多少)")
                containRegex("报道上?([江将]来)?(要是)?出了偏差")
                containRegex("(我(今天)?)?(算是)?得罪了?你们?一下")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "迪兰特比你们不知道高到哪里去了，我和他谈笑风生|•ω•`)",
                        "江来报道上出了偏差，你们是要负泽任的，民不民白?|•ω•`)",
                        "我今天算是得罪了你们一下|•ω•`)"
                ))
            }
            equal("续") { _, cyls ->
                cyls.reply("吃枣药丸|•ω•`)")
            }
            regex("苟(\\.|…|。|\\[\"face\",\\d+])*") { _, cyls ->
                cyls.reply("富贵，无相忘|•ω•`)")
            }
        }

        private val mohaExpertVerifier = createVerifier {
            anyOf({
                contain("大新闻")
                contain("知识水平")
                contain("谈笑风生")
                contain("太暴力了")
                contain("这样暴力")
                contain("暴力膜")
                contain("江")
                contain("泽任")
                contain("民白")
                contain("批判一番")
                contain("知识水平")
                contain("angry")
                contain("moha")
                contain("真正的粉丝")
                contain("听风就是雨")
                contain("长者")
                contain("苟")
                contain("哪里去")
                contain("他")
                contain("得罪")
                contain("报道")
                contain("偏差")
            }) { _, cyls ->
                cyls.reply(Util.itemByChance(
                        "不要整天搞个大新闻|•ω•`)",
                        "你们还是要提高自己的知识水平|•ω•`)",
                        "你们这样是要被拉出去续的|•ω•`)",
                        "真正的粉丝……|•ω•`)",
                        "迪兰特比你们不知道高到哪里去了，我和他谈笑风生|•ω•`)",
                        "江来报道上出了偏差，你们是要负泽任的，民不民白?|•ω•`)",
                        "我今天算是得罪了你们一下|•ω•`)",
                        "吃枣药丸|•ω•`)"
                ))
            }
            contain("苟") { _, cyls ->
                cyls.reply("富贵，无相忘|•ω•`)")
            }
        }
    }

    enum class ChattingStatus(
            val commandTree: TreeNode,
            val replyVerifier: MatchingVerifier
    ) {
        COMMON(commonCommand, commonVerifier);
    }
}
