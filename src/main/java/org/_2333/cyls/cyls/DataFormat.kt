package org._2333.cyls.cyls

import com.alibaba.fastjson.annotation.*

class Data(
        @JSONField
        var friendList: ArrayList<CylsFriend> = ArrayList(),
        @JSONField(serialize = false)
        var friendFromId: HashMap<Long, CylsFriend> = HashMap(),
        @JSONField
        var groupList: ArrayList<CylsGroup> = ArrayList(),
        @JSONField(serialize = false)
        var groupFromId: HashMap<Long, CylsGroup> = HashMap()
)