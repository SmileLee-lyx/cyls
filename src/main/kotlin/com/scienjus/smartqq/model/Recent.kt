package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 最近会话.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/24.
 */
class Recent {

    @JSONField(name = "uin")
    var userId: Long = 0

    //0:好友、1:群、2:讨论组
    var type: Int = 0
}
