package org.smileLee.cyls.cyls

import com.alibaba.fastjson.annotation.*
import com.alibaba.fastjson.serializer.*
import org.smileLee.cyls.*

@JSONType(serialzeFeatures = arrayOf(SerializerFeature.IgnoreNonFieldGetter))
class Data(
        @JSONField
        var cylsFriendList: ArrayList<CylsFriend> = ArrayList(),
        @JSONField
        var cylsGroupList: ArrayList<CylsGroup> = ArrayList()
) {
    @JSONField(serialize = false) var _cylsFriendFromId: HashMap<Long, CylsFriend> = HashMap()
    @JSONField(serialize = false) var _cylsGroupFromId: HashMap<Long, CylsGroup> = HashMap()
    @JSONField(serialize = false) val cylsFriendFromId = object : SafeMap<Long, CylsFriend> {
        override fun put(key: Long, value: CylsFriend) = _cylsFriendFromId.put(key, value)
        override fun putAll(from: Map<Long, CylsFriend>) = _cylsFriendFromId.putAll(from)
        override fun iterator() = _cylsFriendFromId.iterator()
        override fun get(key: Long): CylsFriend {
            val friend = _cylsFriendFromId[key]
            if (friend != null) return friend else {
                for (it in cylsFriendList) {
                    if (it.friend?.userId == key) {
                        _cylsFriendFromId.put(key, it)
                        return it
                    }
                }
                val cylsFriend = CylsFriend()
                cylsFriendList.add(cylsFriend)
                _cylsFriendFromId.put(key, cylsFriend)
                return cylsFriend
            }
        }
    }
    @JSONField(serialize = false) val cylsGroupFromId = object : SafeMap<Long, CylsGroup> {
        override fun put(key: Long, value: CylsGroup) = _cylsGroupFromId.put(key, value)
        override fun putAll(from: Map<Long, CylsGroup>) = _cylsGroupFromId.putAll(from)
        override fun iterator() = _cylsGroupFromId.iterator()
        override fun get(key: Long): CylsGroup {
            val group = _cylsGroupFromId[key]
            if (group != null) return group else {
                for (it in cylsGroupList) {
                    if (it.group?.id == key) {
                        _cylsGroupFromId.put(key, it)
                        return it
                    }
                }
                val cylsGroup = CylsGroup()
                cylsGroupList.add(cylsGroup)
                _cylsGroupFromId.put(key, cylsGroup)
                return cylsGroup
            }
        }
    }
}