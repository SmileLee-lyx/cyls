package org.smileLee.cyls.cyls

class PredefinedMatchingVerifier private constructor() {
    class GoodNightVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("晚安")
            contain("好梦")
        }
    }

    class GoodMorningVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("早安")
            equal("早")
        }
    }

    class HasOrNotVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("有没有")
            containRegex("有.{0,5}((?<!什)么|吗)")
        }
    }

    class IsOrNotVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("是不是")
            containRegex("是.{0,5}((?<!什)么|吗)")
        }
    }

    class CanOrNotVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("会不会")
            containRegex("会.{0,5}((?<!什)么|吗)")
        }
    }

    class LikeOrNotVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("喜不喜欢")
            containRegex("喜欢.{0,5}((?<!什)么|吗)")
        }
    }

    class ConfessVerifierNode(runner: (String, Cyls) -> Unit) : ContainNode("表白云裂", runner)

    class ConfessOtherVerifierNode(runner: (String, Cyls) -> Unit) : ContainNode("表白", runner)

    class CheckVerifierNode(runner: (String, Cyls) -> Unit) : AllRunnerNode(arrayListOf(), runner) {
        init {
            contain("云裂")
            contain("自检")
        }
    }

    class MentionedVerifierNode(runner: (String, Cyls) -> Unit) : ContainNode("云裂", runner)

    class NewOperationVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            contain("什么操作")
            contain("这种操作")
            contain("新的操作")
        }
    }

    class ImproperToContinueVerifierNode(runner: (String, Cyls) -> Unit)
        : ContainRegexNode("你们?(再?一直再?|再?继续再?)?这样(下去)?是不行的", runner)

    class MoPhoenixLiVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            containRegex("原因么?要?(自己)?找一?找")
            contain("什么原因")
            contain("引起重视")
            contain("知名度")
        }
    }

    class MakeBigNewsVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
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
        }
    }

    class DeviationInReportVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
            containRegex("高[到了]不知道?(那里去|多少)")
            containRegex("报道上?([江将]来)?(要是)?出了偏差")
            containRegex("(我(今天)?)?(算是)?得罪了?你们?一下")
        }
    }

    class MaintainLifeVerifierNode(runner: (String, Cyls) -> Unit)
        : EqualNode("续", runner)

    class NotForgetWhenWealthyVerifierNode(runner: (String, Cyls) -> Unit)
        : RegexNode("苟(\\.|…|。|\\[\"face\",\\d+])*", runner)

    class MohaCompleteWorkVerifierNode(runner: (String, Cyls) -> Unit) : AnyRunnerNode(arrayListOf(), runner) {
        init {
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
        }
    }
}