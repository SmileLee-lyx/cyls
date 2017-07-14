package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.alibaba.fastjson.serializer.*
import org.smileLee.cyls.util.*

@JSONType(serialzeFeatures = arrayOf(SerializerFeature.IgnoreNonFieldGetter))
class Data(
        @JSONField
        var cylsFriendList: ArrayList<CylsFriend> = ArrayList(),
        @JSONField
        var cylsGroupList: ArrayList<CylsGroup> = ArrayList()
) {

    @JSONField(serialize = false) var _cylsFriendFromId: HashMap<Long, CylsFriend> = HashMap()
    @JSONField(serialize = false) var _cylsGroupFromId: HashMap<Long, CylsGroup> = HashMap()
    @JSONField(serialize = false) val cylsFriendFromId = InitSafeMap(_cylsFriendFromId) { key ->
        for (it in cylsFriendList) {
            if (it.friend?.userId == key) {
                _cylsFriendFromId.put(key, it)
                return@InitSafeMap it
            }
        }
        val cylsFriend = CylsFriend()
        cylsFriendList.add(cylsFriend)
        _cylsFriendFromId.put(key, cylsFriend)
        return@InitSafeMap cylsFriend
    }
    @JSONField(serialize = false) val cylsGroupFromId = InitSafeMap(_cylsGroupFromId) { key ->
        for (it in cylsGroupList) {
            if (it.group?.id == key) {
                _cylsGroupFromId.put(key, it)
                return@InitSafeMap it
            }
        }
        val cylsGroup = CylsGroup()
        cylsGroupList.add(cylsGroup)
        _cylsGroupFromId.put(key, cylsGroup)
        return@InitSafeMap cylsGroup
    }
}