package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.smileLee.cyls.Main.client
import org.smileLee.cyls.Main.currentFriend
import org.smileLee.cyls.Main.currentGroup
import org.smileLee.cyls.Main.currentGroupId
import org.smileLee.cyls.Main.data
import org.smileLee.cyls.Main.getGroupInfoFromID
import org.smileLee.cyls.Main.getGroupUserNick
import org.smileLee.cyls.Main.getWeather
import org.smileLee.cyls.Main.load
import org.smileLee.cyls.Main.replyToFriend
import org.smileLee.cyls.Main.save
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
            childNode("sudo", {
                replyToFriend("""输入
cyls.help.sudo
查看帮助信息|•ω•`)""")
            }) {
                childNode("ignore", {
                    val uin = it.toLong()
                    val destUser = data._cylsFriendFromId[uin] ?: {
                        replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }()
                    if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                        if (!destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.IGNORED
                        replyToFriend("${destUser.markName}已被屏蔽，然而这么做是不是不太好…… |•ω•`)")
                        save()
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("recognize", {
                    val uin = it.toLong()
                    val destUser = data._cylsFriendFromId[uin] ?: {
                        replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }()
                    if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                        if (destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.RECOGNIZED
                        replyToFriend("${destUser.markName}已被解除屏蔽，当初为什么要屏蔽他呢…… |•ω•`)")
                        save()
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("authorize", {
                    val uin = it.toLong()
                    val destUser = data._cylsFriendFromId[uin] ?: {
                        replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }()
                    if (currentFriend.isOwner) {
                        if (!destUser.isAdmin) {
                            destUser.adminLevel = CylsFriend.AdminLevel.ADMIN
                            replyToFriend("${destUser.markName}已被设置为管理员啦 |•ω•`)")
                            save()
                        } else {
                            replyToFriend("${destUser.markName}已经是管理员了， 再设置一次有什么好处么…… |•ω•`)")
                        }
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧|•ω•`)")
                    }
                })
                childNode("unauthorize", {
                    val uin = it.toLong()
                    val destUser = data._cylsFriendFromId[uin] ?: {
                        replyToFriend("未找到此人哦|•ω•`)")
                        null!!
                    }()
                    if (currentFriend.isOwner) {
                        if (destUser.isOwner) {
                            replyToFriend("${destUser.markName}是云裂的主人哦，不能被取消管理员身份…… |•ω•`)")
                        } else if (destUser.isAdmin) {
                            currentFriend.adminLevel = CylsFriend.AdminLevel.NORMAL
                            replyToFriend("${destUser.markName}已被取消管理员身份……不过，真的要这样么 |•ω•`)")
                            save()
                        } else {
                            replyToFriend("${destUser.markName}并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)")
                        }
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("pause", {
                    if (currentFriend.isAdmin) {
                        if (!currentGroup.isPaused) {
                            println(2333)
                            replyToFriend("通讯已中断（逃 |•ω•`)")
                            currentGroup.isPaused = true
                            save()
                        } else {
                            replyToFriend("已处于中断状态了啊……不能再中断一次了 |•ω•`)")
                        }
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("resume", {
                    if (currentFriend.isAdmin) {
                        if (currentGroup.isPaused) {
                            replyToFriend("通讯恢复啦 |•ω•`)")
                            currentGroup.isPaused = false
                            save()
                        } else {
                            replyToFriend("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
                        }
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                })
                childNode("repeat", {
                    if (currentFriend.isAdmin) {
                        replyToFriend("请选择重复对象哦|•ω•`)")
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("group", {
                        if (currentFriend.isAdmin) {
                            replyToFriend("请选择重复模式哦|•ω•`)")
                        } else {
                            replyToFriend("你的权限不足哦")
                            replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", {
                            val frequency = it.toDoubleOrNull() ?: 0.3
                            if (currentFriend.isAdmin) {
                                if (!currentGroup.isRepeated) {
                                    currentGroup.isRepeated = true
                                    currentGroup.repeatFrequency = frequency
                                    replyToFriend("本群的所有发言将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    save()
                                } else {
                                    currentGroup.repeatFrequency = frequency
                                    replyToFriend("重复本群发言的概率被设置为$frequency |•ω•`)")
                                    save()
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", {
                            if (currentFriend.isAdmin) {
                                if (!currentGroup.isRepeated) {
                                    currentGroup.isRepeated = false
                                    replyToFriend("本群已取消重复 |•ω•`)")
                                    save()
                                } else {
                                    replyToFriend("本来就没有在重复本群的发言啊 |•ω•`)")
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                    childNode("friend", {
                        if (currentFriend.isAdmin) {
                            replyToFriend("请选择重复模式哦|•ω•`)")
                        } else {
                            replyToFriend("你的权限不足哦")
                            replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", {
                            var str = it.replace("  ", " ")
                            if (str.startsWith(" ")) str = str.substring(1)
                            val strs = str.split(" ")
                            if (strs.isEmpty()) {
                                replyToFriend("请输入被重复的用户的uin|•ω•`)")
                                null!!
                            }
                            val uin = strs[0].toLong()
                            val destUser = data._cylsFriendFromId[uin] ?: {
                                replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }()
                            val frequency = strs[1].toDoubleOrNull() ?: 0.3
                            if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    destUser.isRepeated = true
                                    destUser.repeatFrequency = frequency
                                    replyToFriend("${destUser.markName}的话将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)")
                                    save()
                                } else {
                                    destUser.repeatFrequency = frequency
                                    replyToFriend("重复${destUser.markName}的话的概率被设置为$frequency |•ω•`)")
                                    save()
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", {
                            val uin = it.toLong()
                            val destUser = data._cylsFriendFromId[uin] ?: {
                                replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }()
                            if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isRepeated) {
                                    replyToFriend("${destUser.markName}并没有被重复啊 |•ω•`)")
                                } else {
                                    destUser.isRepeated = false
                                    replyToFriend("${destUser.markName}已被取消重复 |•ω•`)")
                                    save()
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("moha", {
                    if (currentFriend.isAdmin) {
                        replyToFriend("请选择被设置为moha的对象哦|•ω•`)")
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("friend", {
                        if (currentFriend.isAdmin) {
                            replyToFriend("请选择moha模式哦|•ω•`)")
                        } else {
                            replyToFriend("你的权限不足哦")
                            replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                        }
                    }) {
                        childNode("on", {
                            val uin = it.toLong()
                            val destUser = data._cylsFriendFromId[uin] ?: {
                                replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }()
                            if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (!destUser.isMoha) {
                                    destUser.isMoha = true
                                    replyToFriend("${destUser.markName}已被设置为moha专家，真是搞个大新闻 |•ω•`)")
                                    save()
                                } else {
                                    replyToFriend("${destUser.markName}本来就是moha专家啊 |•ω•`)")
                                    save()
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                        childNode("off", {
                            val uin = it.toLong()
                            val destUser = data._cylsFriendFromId[uin] ?: {
                                replyToFriend("未找到此人哦|•ω•`)")
                                null!!
                            }()
                            if (currentFriend.isOwner || (currentFriend.isAdmin && !destUser.isAdmin)) {
                                if (destUser.isMoha) {
                                    destUser.isMoha = false
                                    replyToFriend("${destUser.markName}已被取消moha专家，一定是知识水平不够 |•ω•`)")
                                    save()
                                } else {
                                    replyToFriend("${destUser.markName}本来就不是moha专家啊，为什么要搞大新闻 |•ω•`)")
                                    save()
                                }
                            } else {
                                replyToFriend("你的权限不足哦")
                                replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                            }
                        })
                    }
                }
                childNode("check", {
                    replyToFriend("自检完毕\n一切正常哦|•ω•`)")
                })
                childNode("test", {
                    if (currentFriend.isOwner) {
                        replyToFriend("你是云裂的主人哦|•ω•`)")
                        replyToFriend("输入cyls.help.sudo查看……说好的主人呢，" +
                                "为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴")
                    } else if (currentFriend.isAdmin) {
                        replyToFriend("你是云裂的管理员呢|•ω•`)")
                        replyToFriend("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    } else {
                        replyToFriend("你暂时只是个普通成员呢……|•ω•`)")
                        replyToFriend("输入cyls.help.sudo来查看你的权限哦|•ω•`)")
                    }
                })
                childNode("say", {
                    if (currentFriend.isAdmin) {
                        replyToFriend(it)
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧，也可以让主人给你授权哦 |•ω•`)")
                    }
                }) {
                    childNode("friend", {
                        val index = it.indexOf(" ")
                        val userId = it.substring(0, index).toLong()
                        val content = it.substring(index + 1)
                        client.sendMessageToFriend(userId, content)
                    })
                    childNode("group", {
                        val index = it.indexOf(" ")
                        val groupId = it.substring(0, index).toLong()
                        val content = it.substring(index + 1)
                        client.sendMessageToGroup(groupId, content)
                    })
                }
                childNode("save", {
                    if (currentFriend.isOwner) {
                        save()
                        replyToFriend("已保存完毕|•ω•`)")
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("load", {
                    if (currentFriend.isOwner) {
                        load()
                        replyToFriend("已读取完毕|•ω•`)")
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
                childNode("quit", {
                    if (currentFriend.isOwner) {
                        replyToFriend("尝试关闭通讯中…… |•ω•`)")
                        replyToFriend("通讯已关闭，大家再……")
                        save()
                        System.exit(0)
                    } else {
                        replyToFriend("你的权限不足哦")
                        replyToFriend("不如输入cyls.sudo.test查看你的权限吧 |•ω•`)")
                    }
                })
            }
            childNode("util", {
                replyToFriend("""输入
cyls.help.util
查看帮助信息|•ω•`)""")
            }) {
                childNode("query", {
                    replyToFriend("请选择查找的范围哦|•ω•`)")
                }) {
                    childNode("groupuser", {
                        replyToFriend("开始查找|•ω•`)")
                        val groupUsers = getGroupInfoFromID(currentGroupId).users
                        groupUsers.forEach { user ->
                            val userName = getGroupUserNick(currentGroupId, user.uid)
                            if (userName.contains(it)) {
                                replyToFriend("${user.uid}:$userName")
                                Thread.sleep(100)
                            }
                        }
                    })
                    childNode("friend", {
                        replyToFriend("开始查找|•ω•`)")
                        data.cylsFriendList.forEach { friend ->
                            val friendInfo = friend.friend
                            if (friendInfo != null && friendInfo.nickname.contains(it)) {
                                replyToFriend("${friendInfo.userId}:${friendInfo.nickname}")
                            }
                        }
                    })
                    childNode("group", {
                        replyToFriend("开始查找|•ω•`)")
                        data.cylsGroupList.forEach { group ->
                            val groupInfo = group.group
                            if (groupInfo != null && groupInfo.name.contains(it)) {
                                replyToFriend("${groupInfo.id}:${groupInfo.name}")
                            }
                        }
                    })
                }
                childNode("weather", {
                    var str = it.replace("  ", " ")
                    if (str.startsWith(" ")) str = str.substring(1)
                    val strs = str.split(" ")
                    if (strs.size >= 2) {
                        getWeather(strs[0], strs[1].toInt())
                    } else replyToFriend("请输入城市名与天数|•ω•`)")
                }) {
                    childNode("day0", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isNotEmpty()) {
                            getWeather(strs[0], 0)
                        } else replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("day1", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isNotEmpty()) {
                            getWeather(strs[0], 1)
                        } else replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("day2", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isNotEmpty()) {
                            getWeather(strs[0], 2)
                        } else replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("today", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isNotEmpty()) {
                            getWeather(strs[0], 0)
                        } else replyToFriend("请输入城市名|•ω•`)")
                    })
                    childNode("tomorrow", {
                        var str = it.replace("  ", " ")
                        if (str.startsWith(" ")) str = str.substring(1)
                        val strs = str.split(" ")
                        if (strs.isNotEmpty()) {
                            getWeather(strs[0], 1)
                        } else replyToFriend("请输入城市名|•ω•`)")
                    })
                }
                childNode("dice", {
                    replyToFriend("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                    Thread.sleep(200)
                    replyToFriend("结果是：${Util.randomInt(6) + 1}")
                })
                childNode("random", {
                    val x = it.toIntOrNull()
                    when (x) {
                        null                -> {
                            replyToFriend("还有这种骰子? |•ω•`)")
                            null!!
                        }
                        in Int.MIN_VALUE..1 -> {
                            replyToFriend("我这里可没有你要的面数的骰子|•ω•`)\n然而我可以现做一个")
                        }
                        2                   -> {
                            replyToFriend("这么重要的事情，你却抛硬币决定|•ω•`)")
                        }
                        6                   -> {
                            replyToFriend("人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)")
                        }
                        else                -> {
                            replyToFriend("有的人已经不满足于六面体骰子了，他们需要一个${x}面体的骰子|•ω•`)")
                        }
                    }
                    Thread.sleep(200)
                    replyToFriend("结果是：${Util.randomInt(x) + Util.sign(x)}")
                })
                childNode("cal", {
                    val expression = it.replace("&gt;", ">").replace("&lt;", "<")
                    replyToFriend("结果是：${currentGroup.calculate(expression)}")
                })
            }
            childNode("help", {
                replyToFriend("""欢迎来到帮助系统|•ω•`)
cyls.help.sudo
查看关于云裂控制系统的帮助
cyls.help.util
查看云裂工具箱的帮助
更多功能等待你去发现哦|•ω•`)""")
            }) {
                childNode("sudo", {
                    if (currentFriend.isOwner) {
                        replyToFriend("""你可是云裂的主人呢，连这都不知道 |•ω•`)
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
                    } else if (currentFriend.isAdmin) {
                        replyToFriend("""你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)
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
                        replyToFriend("""你是普通成员，权限有限呢|•ω•`)
可以测试自己的权限
cyls.sudo.test
可以让云裂自检
cyls.sudo.check
不如向主人申请权限吧|•ω•`)""")
                    }
                })
                childNode("util", {
                    replyToFriend("""目前云裂的工具功能还不怎么完善呢|•ω•`)
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
                    childNode("weather", {
                        replyToFriend("""
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
            }) {
                replyToFriend("早啊|•ω•`)")
            }
            contain("晚安", {
                replyToFriend("晚安，好梦|•ω•`)")
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