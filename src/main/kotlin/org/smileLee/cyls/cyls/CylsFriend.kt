package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.util.*

@JSONType(ignores = arrayOf(
        "friend",
        "admin",
        "owner",
        "ignored"
))
class CylsFriend(
        var markName: String = "",
        var adminLevel: AdminLevel = AdminLevel.NORMAL,
        var ignoreLevel: IgnoreLevel = IgnoreLevel.RECOGNIZED,
        var isRepeated: Boolean = false,
        var repeatFrequency: Double = 0.0,
        var isMoha: Boolean = false,
        var friend: Friend? = null,
        var status: ChattingStatus = CylsFriend.ChattingStatus.COMMON
) {
    enum class AdminLevel {
        NORMAL,
        ADMIN,
        OWNER
    }

    enum class IgnoreLevel {
        RECOGNIZED,
        IGNORED
    }

    val isAdmin get() = adminLevel == AdminLevel.ADMIN || adminLevel == AdminLevel.OWNER
    val isOwner get() = adminLevel == AdminLevel.OWNER
    val isIgnored get() = ignoreLevel == IgnoreLevel.IGNORED

    fun set(friend: Friend) {
        markName = friend.markname ?: ""
        this.friend = friend
    }

    companion object {
        val commonCommand = createTree {
            childNode("sudo", { _, cyls ->
                cyls.replyToFriend("""输入
cyls.help.sudo
查看帮助信息|•ω•`)""")
            }) {
                childNode("ignore", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                        if (!destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.IGNORED
                        cyls.replyToFriend("${destUser.markName}已被屏蔽，然而这么做是不是不太好…… |•ω•`)")
                        cyls.save()
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("recognize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                        if (destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.RECOGNIZED
                        cyls.replyToFriend("${destUser.markName}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
                        cyls.save()
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("authorize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentFriend.isOwner) {
                        if (!destUser.isAdmin) {
                            destUser.adminLevel = CylsFriend.AdminLevel.ADMIN
                            cyls.replyToFriend("${destUser.markName}已被设置为管理员啦 |•ω•`)")
                            cyls.save()
                        } else {
                            cyls.replyToFriend("${destUser.markName}已经是管理员了， 再设置一次有什么好处么…… |•ω•`)")
                        }
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧|•ω•`)")
                    }
                })
                childNode("unauthorize", { str, cyls ->
                    val uin = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                        cyls.replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }
                    if (cyls.currentFriend.isOwner) {
                        if (destUser.isOwner) {
                            cyls.replyToFriend("${destUser.markName}是云裂的主人哦，不能被取消管理员身份…… |•ω•`)")
                        } else if (destUser.isAdmin) {
                            cyls.currentFriend.adminLevel = CylsFriend.AdminLevel.NORMAL
                            cyls.replyToFriend("${destUser.markName}已被取消管理员身份……不过，真的要这样么 |•ω•`)")
                            cyls.save()
                        } else {
                            cyls.replyToFriend("${destUser.markName}并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)")
                        }
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("pause", { _, cyls ->
                    if (cyls.currentFriend.isAdmin) {
                        if (!cyls.currentGroup.isPaused) {
                            println(2333)
                            cyls.replyToFriend("通讯已中断（逃 |•ω•`)")
                            cyls.currentGroup.isPaused = true
                            cyls.save()
                        } else {
                            cyls.replyToFriend("已处于中断状态了啊……不能再中断一次了 |•ω•`)")
                        }
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("resume", { _, cyls ->
                    if (cyls.currentFriend.isAdmin) {
                        if (cyls.currentGroup.isPaused) {
                            cyls.replyToFriend("通讯恢复啦 |•ω•`)")
                            cyls.currentGroup.isPaused = false
                            cyls.save()
                        } else {
                            cyls.replyToFriend("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
                        }
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("repeat", { _, cyls ->
                    if (cyls.currentFriend.isAdmin) {
                        cyls.replyToFriend("请选择重复对象哦|•ω•`)")
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("group", { _, cyls ->
                        if (cyls.currentFriend.isAdmin) {
                            cyls.replyToFriend("请选择重复模式哦|•ω•`)")
                        } else {
                            cyls.replyToFriend("你的权限不足哦")
                            cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val frequency = str.toDoubleOrNull() ?: 0.3
                            if (cyls.currentFriend.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = true
                                    cyls.currentGroup.repeatFrequency = frequency
                                    cyls.replyToFriend("本群的所有发言将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.currentGroup.repeatFrequency = frequency
                                    cyls.replyToFriend("重复本群发言的概率被设置为$frequency |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { _, cyls ->
                            if (cyls.currentFriend.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = false
                                    cyls.replyToFriend("本群已取消重复 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.replyToFriend("本来就没有在重复本群的发言啊 |•ω•`)")
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                    childNode("friend", { _, cyls ->
                        if (cyls.currentFriend.isAdmin) {
                            cyls.replyToFriend("请选择重复模式哦|•ω•`)")
                        } else {
                            cyls.replyToFriend("你的权限不足哦")
                            cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            var s = str.replace("  ", " ")
                            if (s.startsWith(" ")) s = s.substring(1)
                            val strs = s.split(" ")
                            if (strs.isEmpty()) {
                                cyls.replyToFriend("请输入被重复的用户的uin|•ω•`)")
                                null!!
                            }
                            val uin = strs[0].toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }
                            val frequency = if (strs.size == 1) 0.3 else strs[1].toDoubleOrNull() ?: 0.3
                            if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    destUser.isRepeated = true
                                    destUser.repeatFrequency = frequency
                                    cyls.replyToFriend("${destUser.markName}的话将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    cyls.save()
                                } else {
                                    destUser.repeatFrequency = frequency
                                    cyls.replyToFriend("重复${destUser.markName}的话的概率被设置为$frequency |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    cyls.replyToFriend("${destUser.markName}并没有被重复啊 |•ω•`)")
                                } else {
                                    destUser.isRepeated = false
                                    cyls.replyToFriend("${destUser.markName}已被取消重复 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("moha", { _, cyls ->
                    if (cyls.currentFriend.isAdmin) {
                        cyls.replyToFriend("请选择被设置为moha的对象哦|•ω•`)")
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("friend", { _, cyls ->
                        if (cyls.currentFriend.isAdmin) {
                            cyls.replyToFriend("请选择moha模式哦|•ω•`)")
                        } else {
                            cyls.replyToFriend("你的权限不足哦")
                            cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isMoha) {
                                    destUser.isMoha = true
                                    cyls.replyToFriend("${destUser.markName}已被设置为moha专家，真是搞个大新闻 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.replyToFriend("${destUser.markName}本来就是moha专家啊 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uin = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uin] ?: let {
                                cyls.replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }
                            if (cyls.currentFriend.isOwner || (cyls.currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (destUser.isMoha) {
                                    destUser.isMoha = false
                                    cyls.replyToFriend("${destUser.markName}已被取消moha专家，一定是知识水平不够 |•ω•`)")
                                    cyls.save()
                                } else {
                                    cyls.replyToFriend("${destUser.markName}本来就不是moha专家啊，为什么要搞大新闻 |•ω•`)")
                                    cyls.save()
                                }
                            } else {
                                cyls.replyToFriend("你的权限不足哦")
                                cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("check", { _, cyls ->
                    cyls.replyToFriend("自检完毕\n一切正常哦|•ω•`)")
                })
                childNode("test", { _, cyls ->
                    if (cyls.currentFriend.isOwner) {
                        cyls.replyToFriend("你是云裂的主人哦|•ω•`)")
                        cyls.replyToFriend("输入cyls.help.sudo查看……说好的主人呢，" +
                                "为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴")
                    } else if (cyls.currentFriend.isAdmin) {
                        cyls.replyToFriend("你是云裂的管理员呢|•ω•`)")
                        cyls.replyToFriend("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    } else {
                        cyls.replyToFriend("你暂时只是个普通成员呢……|•ω•`)")
                        cyls.replyToFriend("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    }
                })
                childNode("say", { str, cyls ->
                    if (cyls.currentFriend.isAdmin) {
                        cyls.replyToFriend(str)
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
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
                    if (cyls.currentFriend.isOwner) {
                        cyls.save()
                        cyls.replyToFriend("已保存完毕|•ω•`)")
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("load", { _, cyls ->
                    if (cyls.currentFriend.isOwner) {
                        cyls.load()
                        cyls.replyToFriend("已读取完毕|•ω•`)")
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("quit", { _, cyls ->
                    if (cyls.currentFriend.isOwner) {
                        cyls.replyToFriend("尝试关闭通讯中…… |•ω•`)")
                        cyls.replyToFriend("通讯已关闭，大家再……")
                        cyls.save()
                        System.exit(0)
                    } else {
                        cyls.replyToFriend("你的权限不足哦")
                        cyls.replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
            }
            childNode("util", { _, cyls ->
                cyls.replyToFriend("""输入
cyls.help.util
查看帮助信息|•ω•`)""")
            }) {
                childNode("query", { _, cyls ->
                    cyls.replyToFriend("请选择查找的范围哦|•ω•`)")
                }) {
                    childNode("groupuser", { str, cyls ->
                        cyls.replyToFriend("开始查找|•ω•`)")
                        val groupUsers = cyls.getGroupInfoFromID(cyls.currentGroupId).users
                        groupUsers.forEach { user ->
                            val userName = cyls.getGroupUserNick(cyls.currentGroupId, user.uid)
                            if (userName.contains(str)) {
                                cyls.replyToFriend("${user.uid}:$userName")
                                Thread.sleep(100)
                            }
                        }
                    })
                    childNode("friend", { str, cyls ->
                        cyls.replyToFriend("开始查找|•ω•`)")
                        cyls.data.cylsFriendList.forEach { friend ->
                            val friendInfo = friend.friend
                            if (friendInfo != null && friendInfo.nickname.contains(str)) {
                                cyls.replyToFriend("${friendInfo.userId}:${friendInfo.nickname}")
                            }
                        }
                    })
                    childNode("group", { str, cyls ->
                        cyls.replyToFriend("开始查找|•ω•`)")
                        cyls.data.cylsGroupList.forEach { group ->
                            val groupInfo = group.group
                            if (groupInfo != null && groupInfo.name.contains(str)) {
                                cyls.replyToFriend("${groupInfo.id}:${groupInfo.name}")
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
                    } else cyls.replyToFriend("请输入城市名与天数|•ω•`)")
                }) {
                    childNode("day0", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0)
                        } else cyls.replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("day1", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1)
                        } else cyls.replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("day2", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 2)
                        } else cyls.replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("today", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0)
                        } else cyls.replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("tomorrow", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1)
                        } else cyls.replyToFriend("请输入城市名|•ω•`)")
                    })
                }
                childNode("dice", { _, cyls ->
                    cyls.replyToFriend("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                    Thread.sleep(200)
                    cyls.replyToFriend("结果是：${Util.randomInt(6) + 1}")
                })
                childNode("random", { str, cyls ->
                    val x = str.toIntOrNull()
                    when (x) {
                        null                -> {
                            cyls.replyToFriend("还有这种骰子? |•ω•`)")
                            null!!
                        }
                        in Int.MIN_VALUE..1 -> {
                            cyls.replyToFriend("我这里可没有你要的面数的骰子|•ω•`)\n然而我可以现做一个")
                        }
                        2                   -> {
                            cyls.replyToFriend("这么重要的事情，你却抛硬币决定|•ω•`)")
                        }
                        6                   -> {
                            cyls.replyToFriend("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                        }
                        else                -> {
                            cyls.replyToFriend("有的人已经不满足于六面体骰子了，他们需要一个${x}面体的骰子|•ω•`)")
                        }
                    }
                    Thread.sleep(200)
                    cyls.replyToFriend("结果是：${Util.randomInt(x) + Util.sign(x)}")
                })
                childNode("cal", { str, cyls ->
                    val expression = str.replace("&gt;", ">").replace("&lt;", "<")
                    cyls.replyToFriend("结果是：${cyls.currentGroup.calculate(expression)}")
                })
            }
            childNode("help", { _, cyls ->
                cyls.replyToFriend("""欢迎来到帮助系统|•ω•`)
cyls.help.sudo
查看关于云裂控制系统的帮助
cyls.help.util
查看云裂工具箱的帮助
更多功能等待你去发现哦|•ω•`)""")
            }) {
                childNode("sudo", { _, cyls ->
                    if (cyls.currentFriend.isOwner) {
                        cyls.replyToFriend("""你可是云裂的主人呢，连这都不知道 |•ω•`)
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
                    } else if (cyls.currentFriend.isAdmin) {
                        cyls.replyToFriend("""你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)
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
                        cyls.replyToFriend("""你是普通成员，权限有限呢|•ω•`)
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
不如向主人申请权限吧|•ω•`)""")
                    }
                })
                childNode("util", { _, cyls ->
                    cyls.replyToFriend("""目前云裂的工具功能还不怎么完善呢|•ω•`)
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
                        cyls.replyToFriend("""
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
            anyOf({
                equal("早")
                contain("早安")
            }) { _, cyls ->
                cyls.replyToFriend("早啊|•ω•`)")
            }
            contain("晚安", { _, cyls ->
                cyls.replyToFriend("晚安，好梦|•ω•`)")
            })
        }
    }

    enum class ChattingStatus(
            val commandTree: TreeNode,
            val replyVerifier: MatchingVerifier
    ) {
        COMMON(commonCommand, commonVerifier);
    }
}