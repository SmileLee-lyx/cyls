package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import org.smileLee.cyls.util.*

@JSONType(ignores = arrayOf("cylsFriendFromId", "cylsGroupFromId"))
class Data(
        var cylsFriendList: ArrayList<CylsFriend> = ArrayList(),
        var cylsGroupList: ArrayList<CylsGroup> = ArrayList()
) {

    var _cylsFriendFromId: HashMap<Long, CylsFriend> = HashMap()
    var _cylsGroupFromId: HashMap<Long, CylsGroup> = HashMap()
    val cylsFriendFromId = InitNonNullMap(_cylsFriendFromId) { key ->
        for (it in cylsFriendList) {
            if (it.friend?.userId == key) {
                _cylsFriendFromId.put(key, it)
                return@InitNonNullMap it
            }
        }
        val cylsFriend = CylsFriend()
        cylsFriendList.add(cylsFriend)
        _cylsFriendFromId.put(key, cylsFriend)
        return@InitNonNullMap cylsFriend
    }
    val cylsGroupFromId = InitNonNullMap(_cylsGroupFromId) { key ->
        for (it in cylsGroupList) {
            if (it.group?.groupId == key) {
                _cylsGroupFromId.put(key, it)
                return@InitNonNullMap it
            }
        }
        val cylsGroup = CylsGroup()
        cylsGroupList.add(cylsGroup)
        _cylsGroupFromId.put(key, cylsGroup)
        return@InitNonNullMap cylsGroup
    }
}