package org.smileLee.cyls.cyls

import org.smileLee.smilescript.value.*

class PredefinedReplyInfo private constructor() {
    class SudoHint : ReplyInfo(arrayOf {
        +arrayOf(
                "输入\ncyls.help.sudo\n查看帮助信息|•ω•`)"
        )
    })

    class MemberNotFound : ReplyInfo(arrayOf {
        +arrayOf("未找到此人哦|•ω•`)")
        +arrayOf("查无此人哦|•ω•`)")
    })

    class AuthorityRequired : ReplyInfo(arrayOf {
        +arrayOf(
                "你的权限不足哦",
                "不如输入 cyls.sudo.test 查看你的权限吧，也可以让主人给你授权哦 |•ω•`)"
        )
    })

    class GroupMemberIgnored(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 已被屏蔽，然而这么做是不是不太好…… |•ω•`)"
        )
    })

    class GroupMemberRecognized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 已被解除屏蔽|•ω•`)"
        )
    })

    class GroupMemberAuthorized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 已被设置为管理员啦 |•ω•`)"
        )
    })

    class GroupAdminAuthorized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 已经是管理员了， 再设置一次有什么好处么…… |•ω•`)"
        )
    })

    class GroupMemberUnauthorized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 并不是管理员啊，主人你是怎么想到这么做的啊…… |•ω•`)"
        )
    })

    class GroupAdminUnauthorized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 已被取消管理员身份……不过，真的要这样么 |•ω•`)"
        )
    })

    class GroupOwnerUnauthorized(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "$nick 是云裂的主人哦，不能被取消管理员身份…… |•ω•`)"
        )
    })

    class GroupPaused : ReplyInfo(arrayOf {
        +arrayOf("通讯已中断（逃 |•ω•`)")
    })

    class GroupPausedAgain : ReplyInfo(arrayOf {
        +arrayOf("已处于中断状态了啊……不能再中断一次了 |•ω•`)")
    })

    class GroupResumed : ReplyInfo(arrayOf {
        +arrayOf("通讯恢复啦 |•ω•`)")
    })

    class GroupResumedAgain : ReplyInfo(arrayOf {
        +arrayOf("通讯并没有中断啊，为什么要恢复呢 |•ω•`)")
    })

    class ChooseRepeatTarget : ReplyInfo(arrayOf {
        +arrayOf(
                "请选择重复对象哦|•ω•`)"
        )
    })

    class ChooseRepeatMode : ReplyInfo(arrayOf {
        +arrayOf(
                "请选择重复模式哦|•ω•`)"
        )
    })

    class RepeatGroup(frequency: Double) : ReplyInfo(arrayOf {
        +arrayOf(
                "本群的所有发言将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)"
        )
    })

    class SetRepeatGroupFrequency(frequency: Double) : ReplyInfo(arrayOf {
        +arrayOf(
                "重复本群发言的概率被设置为$frequency |•ω•`)"
        )
    })

    class CancelRepeatGroup : ReplyInfo(arrayOf {
        +arrayOf("已取消重复本群的发言 |•ω•`)")
    })

    class CancelRepeatNonRepeatedGroup : ReplyInfo(arrayOf {
        +arrayOf("本来就没有在重复本群的发言啊 |•ω•`)")
    })

    class GroupUserUidRequired : ReplyInfo(arrayOf {
        +arrayOf("请输入群成员的uid |•ω•`)")
    })

    class RepeatGroupUser(group: CylsGroup, userId: Long, frequency: Double) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "${nick}的发言将被以${frequency}的概率重复，然而这真是无聊 |•ω•`)"
        )
    })

    class SetRepeatGroupUserFrequency(group: CylsGroup, userId: Long, frequency: Double) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "重复${nick}的发言的概率被设置为$frequency |•ω•`)"
        )
    })

    class CancelRepeatGroupUser(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "已取消重复${nick}的发言 |•ω•`)"
        )
    })

    class CancelRepeatNonRepeatedGroupUser(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "本来就没有在重复${nick}的发言啊 |•ω•`)"
        )
    })

    class ChooseMohaTarget : ReplyInfo(arrayOf {
        +arrayOf("请选择被设置为moha的对象哦|•ω•`)")
    })

    class ChooseMohaMode : ReplyInfo(arrayOf {
        +arrayOf("请选择moha模式哦|•ω•`)")
    })

    class MohaGroupUser(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "${nick}已被设置为moha专家，真是搞个大新闻 |•ω•`)"
        )
    })

    class MohaGroupUserAgain(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "${nick}本来就是moha专家啊 |•ω•`)"
        )
    })

    class CancelMohaGroupUser(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "${nick}已被取消moha专家，一定是知识水平不够 |•ω•`)"
        )
    })

    class CancelMohaNonMohaGroupUser(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf(
                "${nick}本来就不是moha专家啊，为什么要搞大新闻 |•ω•`)"
        )
    })

    class Check : ReplyInfo(arrayOf {
        +arrayOf("自检完毕，\n一切正常哦|•ω•`)")
    })

    class TestOwner : ReplyInfo(arrayOf {
        +arrayOf(
                "你是云裂的主人哦|•ω•`)",
                "输入cyls.help.sudo查看……说好的主人呢，为什么连自己的权限都不知道(╯‵□′)╯︵┴─┴"
        )
    })

    class TestAdmin : ReplyInfo(arrayOf {
        +arrayOf(
                "你是云裂的管理员呢|•ω•`)",
                "输入cyls.help.sudo来查看你的权限哦|•ω•`)"
        )
    })

    class TestMember : ReplyInfo(arrayOf {
        +arrayOf(
                "你暂时只是个普通成员呢……|•ω•`)",
                "输入cyls.help.sudo来查看你的权限哦|•ω•`)"
        )
    })

    class RepeatWord(str: String) : ReplyInfo(arrayOf {
        +arrayOf(str)
    })

    class Save : ReplyInfo(arrayOf {
        +arrayOf("已保存完毕|•ω•`)")
    })

    class Load : ReplyInfo(arrayOf {
        +arrayOf("已读取完毕|•ω•`)")
    })

    class Quit : ReplyInfo(arrayOf {
        +arrayOf(
                "尝试关闭通讯中…… |•ω•`)",
                "通讯即将关闭，大家再……"
        )
    })

    class UtilHint : ReplyInfo(arrayOf {
        +arrayOf("输入\ncyls.help.util\n查看帮助信息|•ω•`)")
    })

    class ChooseQueryRange : ReplyInfo(arrayOf {
        +arrayOf("请选择查找的范围哦|•ω•`)")
    })

    class QueryStart : ReplyInfo(arrayOf {
        +arrayOf("开始查找|•ω•`)")
    })

    class QueryResult(id: Long, name: String) : ReplyInfo(arrayOf {
        +arrayOf("$id : $name")
    })

    class CityNameAndDateRequired : ReplyInfo(arrayOf {
        +arrayOf("请输入城市名与天数|•ω•`)")
    })

    class CityNameRequired : ReplyInfo(arrayOf {
        +arrayOf("请输入城市名|•ω•`)")
    })

    class CubeDice(result: Int) : ReplyInfo(arrayOf {
        +arrayOf(
                "人生有许多精彩，有些人却寄希望于这枚普通的六面体骰子|•ω•`)",
                "结果是：$result"
        )
    })

    class DotDice : ReplyInfo(arrayOf {
        +arrayOf(
                "看，这是一个无穷小的点，它没有表面|•ω•`)"
        )
    })

    class BallDice : ReplyInfo(arrayOf {
        +arrayOf(
                "你给我一颗弹珠做什么|•ω•`)",
                "结果是……你猜|•ω•`)"
        )
    })

    class CoinDice(result: Boolean) : ReplyInfo(arrayOf {
        +arrayOf(
                "这么重要的事情，你却抛硬币决定|•ω•`)",
                "结果是${if (result) "正" else "反"}面朝上哦"
        )
    })

    class RegularDice(face: Int, result: Int) : ReplyInfo(arrayOf {
        +arrayOf(
                "有的人已经不满足于六面体骰子了，他们需要一个${face}面体的骰子|•ω•`)",
                "结果是：$result"
        )
    })

    class IrregularDice(face: Int, result: Int) : ReplyInfo(arrayOf {
        +arrayOf(
                "没见过这种${face}面体的骰子啊，让我来试着做一个|•ω•`)",
                "结果是：$result"
        )
    })

    class NegativeDice(face: Int, result: Int) : ReplyInfo(arrayOf {
        +arrayOf(
                "我这里可没有负数面的骰子|•ω•`)\n然而我可以做一个假的",
                "这可不是普通的${-face}面骰子|•ω•`)",
                "结果是：$result"
        )
    })

    class CalculationResult(value: Value) : ReplyInfo(arrayOf {
        +arrayOf(
                "结果是：${value}"
        )
    })

    class HelpHint : ReplyInfo(arrayOf {
        +arrayOf(
                "你可是云裂的主人呢，连这都不知道 |•ω•`)",
                "可以让云裂屏蔽与解除屏蔽任何一名成员:\ncyls.sudo.ignore/recognize userId\n" +
                        "可以将其他成员设置为云裂的管理员或取消管理员身份:\ncyls.sudo.authorize/unauthorize userId\n" +
                        "可以进行通讯的中断与恢复:\ncyls.sudo.pause/resume\n" +
                        "可以测试自己的权限:\ncyls.sudo.test\n" +
                        "可以让云裂自检:\ncyls.sudo.check\n" +
                        "可以让云裂说特定的内容:\ncyls.sudo.say 要说的话\n" +
                        "还可以终止连接:\ncyls.sudo.quit\n",
                "看你的权限这么多，你还全忘了 |•ω•`)"
        )
    })

    class OwnerHelpSudo : ReplyInfo(arrayOf {
        +arrayOf(
                "你可是云裂的主人呢，连这都不知道 |•ω•`)",
                "可以让云裂屏蔽与解除屏蔽任何一名成员:\ncyls.sudo.ignore/recognize userId\n" +
                        "可以将其他成员设置为云裂的管理员或取消管理员身份:\ncyls.sudo.authorize/unauthorize userId\n" +
                        "可以进行通讯的中断与恢复:\ncyls.sudo.pause/resume\n" +
                        "可以测试自己的权限:\ncyls.sudo.test\n" +
                        "可以让云裂自检:\ncyls.sudo.check\n" +
                        "可以让云裂说特定的内容:\ncyls.sudo.say 要说的话\n" +
                        "还可以终止连接:\ncyls.sudo.quit\n",
                "看你的权限这么多，你还全忘了 |•ω•`)"
        )
    })

    class AdminHelpSudo : ReplyInfo(arrayOf {
        +arrayOf(
                "你是云裂的管理员，连这都不知道，一看就是新上任的|•ω•`)",
                "可以让云裂屏蔽与解除屏蔽普通成员或自己:\ncyls.sudo.ignore/recognize userId\n" +
                        "可以进行通讯的中断与恢复:\ncyls.sudo.pause/resume\n" +
                        "可以测试自己的权限:\ncyls.sudo.test\n" +
                        "可以让云裂自检:\ncyls.sudo.check\n" +
                        "可以让云裂说特定的内容:\ncyls.sudo.say 要说的话"
        )
    })

    class MemberHelpSudo : ReplyInfo(arrayOf {
        +arrayOf(
                "你是普通成员，权限有限呢|•ω•`)\n",
                "可以测试自己的权限:\ncyls.sudo.test\n" +
                        "可以让云裂自检:\ncyls.sudo.check\n",
                "不如向主人申请权限吧|•ω•`)"
        )
    })

    class HelpUtil : ReplyInfo(arrayOf {
        +arrayOf(
                "目前云裂的工具功能还不怎么完善呢|•ω•`)\n",
                "你可以查询群成员:\ncyls.util.query.groupuser [群名片的一部分]" +
                        "\n查询云裂的好友:\ncyls.util.query.friend [昵称的一部分]\n" +
                        "查询群:\ncyls.util.query.group [群名的一部分]\n" +
                        "你可以查询天气:\ncyls.util.weather\n" +
                        "可以掷骰子:\ncyls.util.dice\n" +
                        "抛硬币:\ncyls.util.coin\n" +
                        "或计算1至[最大值]的随机数:\ncyls.util.dice [最大值]\n" +
                        "可以进行简单的计算：\ncyls.util.cal [表达式/代码块]\n",
                "关于天气功能的更多内容，输入\ncyls.help.util.weather\n来查看哦|•ω•`)"
        )
    })

    class HelpWeather : ReplyInfo(arrayOf {
        +arrayOf(
                "云裂的天气查询功能目前只能查到近几日的天气|•ω•`)\n" +
                        "[天数]: 0 -> 今天， 1 -> 明天， 2 -> 后天\n" +
                        "cyls.util.weather [城市名] [天数]\n" +
                        "cyls.util.weather.today [城市名]\n" +
                        "cyls.util.weather.tomorrow [城市名]\n" +
                        "cyls.util.weather.day[天数] [城市名]\n",
                "例如:\ncyls.util.weather.day2 无锡\n查询无锡后天的天气|•ω•`)\n"
        )
    })

    class GoodNight : ReplyInfo {
        constructor(group: CylsGroup, userId: Long) : super(arrayOf {
            val nick = Cyls.getGroupUserNick(group, userId)
            +arrayOf("晚安|•ω•`)")
            +arrayOf("好梦|•ω•`)")
            +arrayOf("$nick ，好梦|•ω•`)")
        })

        constructor() : super(arrayOf {
            +arrayOf("晚安|•ω•`)")
            +arrayOf("好梦|•ω•`)")
        })
    }

    class GoodMorning : ReplyInfo {
        constructor(group: CylsGroup, userId: Long) : super(arrayOf {
            val nick = Cyls.getGroupUserNick(group, userId)
            +arrayOf("早|•ω•`)")
            +arrayOf("早安|•ω•`)")
            +arrayOf("$nick ，早啊|•ω•`)")
        })

        constructor() : super(arrayOf {
            +arrayOf("早|•ω•`)")
            +arrayOf("早安|•ω•`)")
        })
    }

    class HasOrNot : ReplyInfo(arrayOf {
        +arrayOf("有啊（逃 |•ω•`)")
        +arrayOf("这不是显然的么 |•ω•`)")
        +arrayOf("没有啊（逃 |•ω•`)")
        +arrayOf("只有极差的人才会这么认为 |•ω•`)")
        +arrayOf("我为什么要告诉你 |•ω•`)")
        +arrayOf("作业没写完不要和我说话 |•ω•`)")
    })

    class IsOrNot : ReplyInfo(arrayOf {
        +arrayOf("是啊（逃 |•ω•`)")
        +arrayOf("这不是显然的么 |•ω•`)")
        +arrayOf("不是啊（逃 |•ω•`)")
        +arrayOf("只有极差的人才会这么认为 |•ω•`)")
        +arrayOf("我为什么要告诉你 |•ω•`)")
        +arrayOf("作业没写完不要和我说话 |•ω•`)")
    })

    class CanOrNot : ReplyInfo(arrayOf {
        +arrayOf("会啊（逃 |•ω•`)")
        +arrayOf("这不是显然会么 |•ω•`)")
        +arrayOf("不会啊（逃 |•ω•`)")
        +arrayOf("只有极差的人才会认为会 |•ω•`)")
        +arrayOf("我为什么要告诉你 |•ω•`)")
        +arrayOf("作业没写完不要和我说话 |•ω•`)")
    })

    class LikeOrNot : ReplyInfo(arrayOf {
        +arrayOf("喜欢啊（逃 |•ω•`)")
        +arrayOf("这不是显然的么 |•ω•`)")
        +arrayOf("不喜欢啊（逃 |•ω•`)")
        +arrayOf("我也不知道 |•ω•`)")
        +arrayOf("少年不要学坏 |•ω•`)")
    })

    class Mentioned : ReplyInfo(arrayOf {
        +arrayOf("叫我做什么 |•ω•`)")
        +arrayOf("不要整天搞个大新闻，把我叫出来 |•ω•`)")
    })

    class Confess(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf("不要整天搞个大新闻 |•ω•`)")
        +arrayOf("表白$nick |•ω•`)")
        +arrayOf("接受表白 |•ω•`)")
        +arrayOf("好啊 |•ω•`)")
        +arrayOf("好啊 |•ω•`)")
        +arrayOf("丑拒 |•ω•`)")
    })

    class ConfessOther(group: CylsGroup, userId: Long) : ReplyInfo(arrayOf {
        val nick = Cyls.getGroupUserNick(group, userId)
        +arrayOf("$nick 你这么喜欢表白么 |•ω•`)")
        +arrayOf("表白+1 |•ω•`)")
        +arrayOf("围观dalao表白 |•ω•`)")
        +arrayOf("在一起 |•ω•`)")
    })

    class NewOperation : ReplyInfo(arrayOf {
        +arrayOf("本来就有这种操作啊 |•ω•`)")
    })

    class ImproperToContinue : ReplyInfo(arrayOf {
        +arrayOf("你们再这么下去是不行的 |•ω•`)")
    })

    class MoPhoenixLi : ReplyInfo(arrayOf {
        +arrayOf("什么原因么自己找一找|•ω•`)")
        +arrayOf("这个么要引起重视|•ω•`)")
        +arrayOf("lw:我的知名度很高了，不用你们宣传了|•ω•`)")
    })

    class MakeBigNews : ReplyInfo(arrayOf {
        +arrayOf("不要整天搞个大新闻|•ω•`)")
        +arrayOf("你们还是要提高自己的知识水平|•ω•`)")
        +arrayOf("你们这样是要被拉出去续的|•ω•`)")
        +arrayOf("真正的粉丝……|•ω•`)")
    })

    class DeviationInReport : ReplyInfo(arrayOf {
        +arrayOf("读心特比你们不知道高到哪里去了，我和他谈笑风生|•ω•`)")
        +arrayOf("江来报道上出了偏差，你们是要负泽任的，民不民白?|•ω•`)")
        +arrayOf("我今天算是得罪了你们一下|•ω•`)")
    })

    class SoonerOrLater : ReplyInfo(arrayOf {
        +arrayOf("吃枣药丸|•ω•`)")
    })

    class NotForgetWhenWealthy : ReplyInfo(arrayOf {
        +arrayOf("富贵，无相忘|•ω•`)")
    })

    class MohaCompleteWork : ReplyInfo(arrayOf {
        +arrayOf("不要整天搞个大新闻|•ω•`)")
        +arrayOf("你们还是要提高自己的知识水平|•ω•`)")
        +arrayOf("你们这样是要被拉出去续的|•ω•`)")
        +arrayOf("真正的粉丝……|•ω•`)")
        +arrayOf("迪兰特比你们不知道高到哪里去了，我和他谈笑风生|•ω•`)")
        +arrayOf("江来报道上出了偏差，你们是要负泽任的，民不民白?|•ω•`)")
        +arrayOf("我今天算是得罪了你们一下|•ω•`)")
        +arrayOf("吃枣药丸|•ω•`)")
    })
}