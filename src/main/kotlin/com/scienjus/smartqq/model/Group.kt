package com.scienjus.smartqq.model

import com.alibaba.fastjson.annotation.*

/**
 * 群.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/18.
 */
class Group {

    @JSONField(name = "gid")
    var id: Long = 0

    var name: String = ""

    var flag: Long = 0

    var code: Long = 0
}
