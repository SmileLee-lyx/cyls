package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.JSONType
import org.smileLee.cyls.qqbot.QQBotData
import org.smileLee.cyls.util.InitNonNullMap

@JSONType(ignores = arrayOf("cylsFriendFromId", "cylsGroupFromId"))
class CylsData(
        var cylsFriendList: ArrayList<CylsFriend> = ArrayList(),
        var cylsGroupList: ArrayList<CylsGroup> = ArrayList()
) : QQBotData<Cyls>() {
    var _cylsFriendFromId: HashMap<Long, CylsFriend> = HashMap()
    var _cylsGroupFromId: HashMap<Long, CylsGroup> = HashMap()
    val cylsFriendFromId = InitNonNullMap(_cylsFriendFromId) { key ->
        cylsFriendList.filter { it.friend?.userId == key }.forEach {
            _cylsFriendFromId.put(key, it)
            return@InitNonNullMap it
        }
        val cylsFriend = CylsFriend()
        cylsFriendList.add(cylsFriend)
        _cylsFriendFromId.put(key, cylsFriend)
        return@InitNonNullMap cylsFriend
    }
    val cylsGroupFromId = InitNonNullMap(_cylsGroupFromId) { key ->
        cylsGroupList.filter { it.group?.groupId == key }.forEach {
            _cylsGroupFromId.put(key, it)
            return@InitNonNullMap it
        }
        val cylsGroup = CylsGroup()
        cylsGroupList.add(cylsGroup)
        _cylsGroupFromId.put(key, cylsGroup)
        return@InitNonNullMap cylsGroup
    }

    override val qqBotFriendList get() = cylsFriendList
    override val qqBotGroupList get() = cylsGroupList
    override val qqBotFriendFromId get() = cylsFriendFromId
    override val qqBotGroupFromId get() = cylsGroupFromId
}