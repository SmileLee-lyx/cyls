package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 讨论组.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/23.
 */
class Discuss {

    @JSONField(name = "did")
    var discussId: Long = 0

    var name: String? = null
}
