package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.scienjus.smartqq.model.*
import org.ansj.splitWord.analysis.*
import org.smileLee.cyls.cyls.PredefinedMatchingVerifier.*
import org.smileLee.cyls.cyls.PredefinedReplyInfo.*
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
        var status: ChattingStatus = ChattingStatus.COMMON,
        var group: Group? = null,
        var groupInfo: GroupInfo? = null
) {
    private var _groupUsersFromId: HashMap<Long, GroupUser> = HashMap()
    val groupUsersFromId = InitNonNullMap(_groupUsersFromId) { key ->
        groupInfo?.users?.forEach {
            if (it.userId == key) {
                _groupUsersFromId.put(key, it)
                return@InitNonNullMap it
            }
        }
        null!!
    }

    private val messageCount get() = _messageCount
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
        name = group.name ?: ""
        this.group = group
    }

    private val stack = Stack()
    fun calculate(s: String) = Block.parse(s).invoke(stack)

    companion object {
        val MAX_MESSAGE_COUNT = 50
        val commonCommand = createTree {
            childNode("sudo", { _, cyls ->
                SudoHint().replyTo(cyls.currentGroupReplier)
            }) {
                childNode("ignore", { str, cyls ->
                    val uid = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uid]
                    if (destUser == null) {
                        MemberNotFound().replyTo(cyls.currentGroupReplier)
                    } else {
                        if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                            if (!destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.IGNORED
                            GroupMemberIgnored(cyls.currentGroup, uid).replyTo(cyls.currentGroupReplier)
                            cyls.save()
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("recognize", { str, cyls ->
                    val uid = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uid]
                    if (destUser == null) {
                        MemberNotFound().replyTo(cyls.currentGroupReplier)
                    } else {
                        if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                            if (destUser.isIgnored) destUser.ignoreLevel = CylsFriend.IgnoreLevel.RECOGNIZED
                            GroupMemberRecognized(cyls.currentGroup, uid).replyTo(cyls.currentGroupReplier)
                            cyls.save()
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("authorize", { str, cyls ->
                    val uid = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uid]
                    if (destUser == null) {
                        MemberNotFound().replyTo(cyls.currentGroupReplier)
                    } else {
                        if (cyls.currentGroupUser.isOwner) when {
                            destUser.isAdmin -> {
                                GroupAdminAuthorized(cyls.currentGroup, uid)
                                        .replyTo(cyls.currentGroupReplier)
                            }
                            else             -> {
                                destUser.adminLevel = CylsFriend.AdminLevel.ADMIN
                                GroupMemberRecognized(cyls.currentGroup, uid)
                                        .replyTo(cyls.currentGroupReplier)
                                cyls.save()
                            }
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("unauthorize", { str, cyls ->
                    val uid = str.toLong()
                    val destUser = cyls.data._cylsFriendFromId[uid]
                    if (destUser == null) {
                        MemberNotFound().replyTo(cyls.currentGroupReplier)
                    } else {
                        if (cyls.currentGroupUser.isOwner) when {
                            destUser.isOwner -> {
                                GroupOwnerUnauthorized(cyls.currentGroup, uid).replyTo(cyls.currentGroupReplier)
                            }
                            destUser.isAdmin -> {
                                cyls.currentGroupUser.adminLevel = CylsFriend.AdminLevel.NORMAL
                                GroupAdminUnauthorized(cyls.currentGroup, uid).replyTo(cyls.currentGroupReplier)
                                cyls.save()
                            }
                            else             -> {
                                GroupMemberUnauthorized(cyls.currentGroup, uid).replyTo(cyls.currentGroupReplier)
                            }
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("pause", { _, cyls ->
                    if (cyls.currentGroupUser.isAdmin) {
                        if (!cyls.currentGroup.isPaused) {
                            println(2333)
                            GroupPaused().replyTo(cyls.currentGroupReplier)
                            cyls.currentGroup.isPaused = true
                            cyls.save()
                        } else {
                            GroupPausedAgain().replyTo(cyls.currentGroupReplier)
                        }
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                })
                childNode("resume", { _, cyls ->
                    if (cyls.currentGroupUser.isAdmin) {
                        if (cyls.currentGroup.isPaused) {
                            GroupResumed().replyTo(cyls.currentGroupReplier)
                            cyls.currentGroup.isPaused = false
                            cyls.save()
                        } else {
                            GroupResumedAgain().replyTo(cyls.currentGroupReplier)
                        }
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                })
                childNode("repeat", { _, cyls ->
                    if (cyls.currentGroupUser.isAdmin) {
                        ChooseRepeatTarget().replyTo(cyls.currentGroupReplier)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                }) {
                    childNode("group", { _, cyls ->
                        if (cyls.currentGroupUser.isAdmin) {
                            ChooseRepeatMode().replyTo(cyls.currentGroupReplier)
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val frequency = str.toDoubleOrNull() ?: 0.3
                            if (cyls.currentGroupUser.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = true
                                    cyls.currentGroup.repeatFrequency = frequency
                                    RepeatGroup(frequency).replyTo(cyls.currentGroupReplier)
                                    cyls.save()
                                } else {
                                    cyls.currentGroup.repeatFrequency = frequency
                                    SetRepeatGroupFrequency(frequency)
                                            .replyTo(cyls.currentGroupReplier)
                                    cyls.save()
                                }
                            } else {
                                AuthorityRequired().replyTo(cyls.currentGroupReplier)
                            }
                        })
                        childNode("off", { _, cyls ->
                            if (cyls.currentGroupUser.isAdmin) {
                                if (!cyls.currentGroup.isRepeated) {
                                    cyls.currentGroup.isRepeated = false
                                    CancelRepeatGroup().replyTo(cyls.currentGroupReplier)
                                    cyls.save()
                                } else {
                                    CancelRepeatNonRepeatedGroup()
                                            .replyTo(cyls.currentGroupReplier)
                                }
                            } else {
                                AuthorityRequired().replyTo(cyls.currentGroupReplier)
                            }
                        })
                    }
                    childNode("friend", { _, cyls ->
                        if (cyls.currentGroupUser.isAdmin) {
                            ChooseRepeatMode().replyTo(cyls.currentGroupReplier)
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            var s = str.replace("  ", " ")
                            if (s.startsWith(" ")) s = s.substring(1)
                            val strs = s.split(" ")
                            if (strs.isEmpty()) {
                                GroupUserUidRequired().replyTo(cyls.currentGroupReplier)
                            } else {
                                val uid = strs[0].toLong()
                                val destUser = cyls.data._cylsFriendFromId[uid]
                                if (destUser == null) {
                                    MemberNotFound().replyTo(cyls.currentGroupReplier)
                                } else {
                                    val frequency = try {
                                        strs[1]
                                    } catch (e: ArrayIndexOutOfBoundsException) {
                                        ""
                                    }.toDoubleOrNull() ?: 0.3
                                    if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                                        if (!destUser.isRepeated) {
                                            destUser.isRepeated = true
                                            destUser.repeatFrequency = frequency
                                            RepeatGroupUser(cyls.currentGroup, uid, frequency)
                                                    .replyTo(cyls.currentGroupReplier)
                                            cyls.save()
                                        } else {
                                            destUser.repeatFrequency = frequency
                                            SetRepeatGroupUserFrequency(cyls.currentGroup, uid, frequency)
                                                    .replyTo(cyls.currentGroupReplier)
                                            cyls.save()
                                        }
                                    } else {
                                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                                    }
                                }
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uid = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uid]
                            if (destUser == null) {
                                MemberNotFound().replyTo(cyls.currentGroupReplier)
                            } else if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                                if (!destUser.isRepeated) {
                                    CancelRepeatNonRepeatedGroupUser(cyls.currentGroup, uid)
                                            .replyTo(cyls.currentGroupReplier)
                                } else {
                                    destUser.isRepeated = false
                                    CancelRepeatGroupUser(cyls.currentGroup, uid)
                                            .replyTo(cyls.currentGroupReplier)
                                    cyls.save()
                                }
                            } else {
                                AuthorityRequired().replyTo(cyls.currentGroupReplier)
                            }
                        })
                    }
                }
                childNode("moha", { _, cyls ->
                    if (cyls.currentGroupUser.isAdmin) {
                        ChooseMohaTarget().replyTo(cyls.currentGroupReplier)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                }) {
                    childNode("groupuser", { _, cyls ->
                        if (cyls.currentGroupUser.isAdmin) {
                            ChooseMohaMode().replyTo(cyls.currentGroupReplier)
                        } else {
                            AuthorityRequired().replyTo(cyls.currentGroupReplier)
                        }
                    }) {
                        childNode("on", { str, cyls ->
                            val uid = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uid]
                            if (destUser == null) {
                                MemberNotFound().replyTo(cyls.currentGroupReplier)
                            } else {
                                if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                                    if (!destUser.isMoha) {
                                        destUser.isMoha = true
                                        MohaGroupUser(cyls.currentGroup, uid)
                                                .replyTo(cyls.currentGroupReplier)
                                        cyls.save()
                                    } else {
                                        MohaGroupUserAgain(cyls.currentGroup, uid)
                                                .replyTo(cyls.currentGroupReplier)
                                        cyls.save()
                                    }
                                } else {
                                    AuthorityRequired().replyTo(cyls.currentGroupReplier)
                                }
                            }
                        })
                        childNode("off", { str, cyls ->
                            val uid = str.toLong()
                            val destUser = cyls.data._cylsFriendFromId[uid]
                            if (destUser == null) {
                                MemberNotFound().replyTo(cyls.currentGroupReplier)
                            } else {
                                if (cyls.currentGroupUser.authorityGreaterThan(destUser)) {
                                    if (destUser.isMoha) {
                                        destUser.isMoha = false
                                        MohaGroupUser(cyls.currentGroup, uid)
                                                .replyTo(cyls.currentGroupReplier)
                                        cyls.save()
                                    } else {
                                        CancelMohaNonMohaGroupUser(cyls.currentGroup, uid)
                                                .replyTo(cyls.currentGroupReplier)
                                        cyls.save()
                                    }
                                } else {
                                    AuthorityRequired().replyTo(cyls.currentGroupReplier)
                                }
                            }
                        })
                    }
                }
                childNode("check", { _, cyls ->
                    Check().replyTo(cyls.currentGroupReplier)
                })
                childNode("test", { _, cyls ->
                    when {
                        cyls.currentGroupUser.isOwner -> {
                            TestOwner().replyTo(cyls.currentGroupReplier)
                        }
                        cyls.currentGroupUser.isAdmin -> {
                            TestAdmin().replyTo(cyls.currentGroupReplier)
                        }
                        else                          -> {
                            TestMember().replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("say", { str, cyls ->
                    if (cyls.currentGroupUser.isAdmin) {
                        RepeatWord(str).replyTo(cyls.currentGroupReplier)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                }) {
                    childNode("friend", { str, cyls ->
                        val index = str.indexOf(" ")
                        val userId = str.substring(0, index).toLong()
                        val content = str.substring(index + 1)
                        RepeatWord(content).replyTo(cyls.replierToFriend(userId))
                    })
                    childNode("group", { str, cyls ->
                        val index = str.indexOf(" ")
                        val groupId = str.substring(0, index).toLong()
                        val content = str.substring(index + 1)
                        RepeatWord(content).replyTo(cyls.replierToGroup(groupId))
                    })
                }
                childNode("save", { _, cyls ->
                    if (cyls.currentGroupUser.isOwner) {
                        cyls.save()
                        Save().replyTo(cyls.currentGroupReplier)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                })
                childNode("load", { _, cyls ->
                    if (cyls.currentGroupUser.isOwner) {
                        cyls.load()
                        Load().replyTo(cyls.currentGroupReplier)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                })
                childNode("quit", { _, cyls ->
                    if (cyls.currentGroupUser.isOwner) {
                        Quit().replyTo(cyls.currentGroupReplier)
                        cyls.save()
                        System.exit(0)
                    } else {
                        AuthorityRequired().replyTo(cyls.currentGroupReplier)
                    }
                })
            }
            childNode("util", { _, cyls ->
                UtilHint().replyTo(cyls.currentGroupReplier)
            }) {
                childNode("query", { _, cyls ->
                    ChooseQueryRange().replyTo(cyls.currentGroupReplier)
                }) {
                    childNode("groupuser", { str, cyls ->
                        QueryStart().replyTo(cyls.currentGroupReplier)
                        val groupId = cyls.currentGroupId
                        val groupInfoFromID = cyls.getGroupInfoFromID(groupId)
                        val groupUsers = groupInfoFromID.users
                        groupUsers?.forEach { user ->
                            val userName = cyls.getGroupUserNick(groupId, user.userId)
                            if (userName.contains(str)) {
                                QueryResult(user.userId, userName)
                                        .replyTo(cyls.currentGroupReplier)
                                sleep(100)
                            }
                        }
                    })
                    childNode("friend", { str, cyls ->
                        QueryStart().replyTo(cyls.currentGroupReplier)
                        cyls.data.cylsFriendList.forEach { friend ->
                            val friendInfo = friend.friend
                            if (friendInfo != null && friendInfo.nickname?.contains(str) == true) {
                                QueryResult(friendInfo.userId, friend.markName)
                                        .replyTo(cyls.currentGroupReplier)
                            }
                        }
                    })
                    childNode("group", { str, cyls ->
                        QueryStart().replyTo(cyls.currentGroupReplier)
                        cyls.data.cylsGroupList.forEach { group ->
                            val groupInfo = group.group
                            if (groupInfo != null && groupInfo.name?.contains(str) == true) {
                                QueryResult(groupInfo.groupId, group.name)
                                        .replyTo(cyls.currentGroupReplier)
                            }
                        }
                    })
                }
                childNode("weather", { str, cyls ->
                    var s = str.replace("  ", " ")
                    if (s.startsWith(" ")) s = s.substring(1)
                    val strs = s.split(" ")
                    if (strs.size >= 2) {
                        cyls.getWeather(strs[0], strs[1].toInt(), cyls.currentGroupReplier)
                    } else CityNameAndDateRequired().replyTo(cyls.currentGroupReplier)
                }) {
                    childNode("day0", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0, cyls.currentGroupReplier)
                        } else CityNameRequired().replyTo(cyls.currentGroupReplier)
                    })
                    childNode("day1", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1, cyls.currentGroupReplier)
                        } else CityNameRequired().replyTo(cyls.currentGroupReplier)
                    })
                    childNode("day2", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 2, cyls.currentGroupReplier)
                        } else CityNameRequired().replyTo(cyls.currentGroupReplier)
                    })
                    childNode("today", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 0, cyls.currentGroupReplier)
                        } else CityNameRequired().replyTo(cyls.currentGroupReplier)
                    })
                    childNode("tomorrow", { str, cyls ->
                        var s = str.replace("  ", " ")
                        if (s.startsWith(" ")) s = s.substring(1)
                        val strs = s.split(" ")
                        if (strs.isNotEmpty()) {
                            cyls.getWeather(strs[0], 1, cyls.currentGroupReplier)
                        } else CityNameRequired().replyTo(cyls.currentGroupReplier)
                    })
                }
                childNode("dice", { str, cyls ->
                    val x = str.toIntOrNull()
                    when (x) {
                        null, 6
                             -> {
                            CubeDice(Util.randomInt(6) + 1).replyTo(cyls.currentGroupReplier)
                        }
                        4, 8, 12, 20
                             -> {
                            RegularDice(x, Util.randomInt(x) + 1).replyTo(cyls.currentGroupReplier)
                        }
                        0    -> {
                            DotDice().replyTo(cyls.currentGroupReplier)
                        }
                        1    -> {
                            BallDice().replyTo(cyls.currentGroupReplier)
                        }
                        2    -> {
                            CoinDice(Util.randomBool).replyTo(cyls.currentGroupReplier)
                        }
                        in 1..Int.MAX_VALUE
                             -> {
                            IrregularDice(x, Util.randomInt(x) + 1).replyTo(cyls.currentGroupReplier)
                        }
                        else -> {
                            NegativeDice(x, Util.randomInt(x) - 1).replyTo(cyls.currentGroupReplier)
                        }
                    }
                })
                childNode("coin", { _, cyls ->
                    CoinDice(Util.randomBool).replyTo(cyls.currentGroupReplier)
                })
                childNode("cal", { str, cyls ->
                    val expression = str.replace("&gt;", ">").replace("&lt;", "<")
                    CalculationResult(cyls.currentGroup.calculate(expression))
                            .replyTo(cyls.currentGroupReplier)
                })
            }
            childNode("help", { _, cyls ->
                HelpHint().replyTo(cyls.currentGroupReplier)
            }) {
                childNode("sudo", { _, cyls ->
                    when {
                        cyls.currentGroupUser.isOwner ->
                            OwnerHelpSudo().replyTo(cyls.currentGroupReplier)
                        cyls.currentGroupUser.isAdmin ->
                            AdminHelpSudo().replyTo(cyls.currentGroupReplier)
                        else                          ->
                            MemberHelpSudo().replyTo(cyls.currentGroupReplier)
                    }
                })
                childNode("util", { _, cyls ->
                    HelpUtil().replyTo(cyls.currentGroupReplier)
                }) {
                    childNode("weather", { _, cyls ->
                        HelpUtil().replyTo(cyls.currentGroupReplier)
                    })
                }
            }
        }
        val commonVerifier = createVerifier {
            +GoodNightVerifierNode { _, cyls ->
                val hasGreeted = cyls.currentGroup.hasGreeted
                cyls.currentGroup.addGreeting()
                if (!hasGreeted) GoodNight(cyls.currentGroup, cyls.currentGroupUserId)
                        .replyTo(cyls.currentGroupReplier)
            }
            +GoodMorningVerifierNode { _, cyls ->
                val hasGreeted = cyls.currentGroup.hasGreeted
                cyls.currentGroup.addGreeting()
                if (!hasGreeted) GoodMorning(cyls.currentGroup, cyls.currentGroupUserId)
                        .replyTo(cyls.currentGroupReplier)
            }
            +HasOrNotVerifierNode { _, cyls ->
                HasOrNot().replyTo(cyls.currentGroupReplier)
            }
            +IsOrNotVerifierNode { _, cyls ->
                IsOrNot().replyTo(cyls.currentGroupReplier)
            }
            +CanOrNotVerifierNode { _, cyls ->
                CanOrNot().replyTo(cyls.currentGroupReplier)
            }
            +LikeOrNotVerifierNode { _, cyls ->
                LikeOrNot().replyTo(cyls.currentGroupReplier)
            }
            +ConfessVerifierNode { _, cyls ->
                Confess(cyls.currentGroup, cyls.currentGroupUserId)
                        .replyTo(cyls.currentGroupReplier)
            }
            +ConfessOtherVerifierNode { str, cyls ->
                if (ToAnalysis.parse(str).any { it.realName == "表白" })
                    ConfessOther(cyls.currentGroup, cyls.currentGroupUserId)
                            .replyTo(cyls.currentGroupReplier)
            }
            +CheckVerifierNode { _, cyls ->
                Check().replyTo(cyls.currentGroupReplier)
            }
            +MentionedVerifierNode { str, cyls ->
                if (ToAnalysis.parse(str).any { it.realName == "云裂" || it.realName == "穿云裂石" })
                    Mentioned().replyTo(cyls.currentGroupReplier)
            }
            +NewOperationVerifierNode { _, cyls ->
                NewOperation().replyTo(cyls.currentGroupReplier)
            }
            +ImproperToContinueVerifierNode { _, cyls ->
                ImproperToContinue().replyTo(cyls.currentGroupReplier)
            }
            +MoPhoenixLiVerifierNode { _, cyls ->
                MoPhoenixLi().replyTo(cyls.currentGroupReplier)
            }
            special { str, cyls ->
                if (!cyls.currentGroupUser.isMoha)
                    commonMohaVerifier.findAndRun(str, cyls)
                else
                    mohaExpertVerifier.findAndRun(str, cyls)
            }
        }
        private val commonMohaVerifier = createVerifier {
            +MakeBigNewsVerifierNode { _, cyls ->
                MakeBigNews().replyTo(cyls.currentGroupReplier)
            }
            +DeviationInReportVerifierNode { _, cyls ->
                DeviationInReport().replyTo(cyls.currentGroupReplier)
            }
            +MaintainLifeVerifierNode { _, cyls ->
                SoonerOrLater().replyTo(cyls.currentGroupReplier)
            }
            +NotForgetWhenWealthyVerifierNode { _, cyls ->
                NotForgetWhenWealthy().replyTo(cyls.currentGroupReplier)
            }
        }
        private val mohaExpertVerifier = createVerifier {
            +MohaCompleteWorkVerifierNode { _, cyls ->
                MohaCompleteWork().replyTo(cyls.currentGroupReplier)
            }
            +NotForgetWhenWealthyVerifierNode { _, cyls ->
                NotForgetWhenWealthy().replyTo(cyls.currentGroupReplier)
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
