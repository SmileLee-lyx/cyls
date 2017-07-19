package com.scienjus.smartqq.constant

/**
 * Api的请求地址和Referer

 * @author ScienJus
 * *
 * @date 2015/12/19
 */
class ApiURL(val url: String, val referer: String?) {

    fun buildUrl(vararg params: Any): String {
        var i = 1
        var url = this.url
        for (param in params) {
            url = url.replace("{${i++}}", param.toString())
        }
        return url
    }

    val origin: String
        get() = this.url.substring(0, url.lastIndexOf("/"))

    companion object {
        @JvmStatic val USER_AGENT =
                "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"

        @JvmStatic val GET_QR_CODE = ApiURL(
                "https://ssl.ptlogin2.qq.com/ptqrshow?appid=501004106&e=0&l=M&s=5&d=72&v=4&t=0.1",
                ""
        )
        @JvmStatic val VERIFY_QR_CODE = ApiURL(
                "https://ssl.ptlogin2.qq.com/ptqrlogin?" +
                        "ptqrtoken={1}&webqq_type=10&remember_uin=1&login2qq=1&aid=501004106&" +
                        "u1=http%3A%2F%2Fw.qq.com%2Fproxy.html%3Flogin2qq%3D1%26webqq_type%3D10&" +
                        "ptredirect=0&ptlang=2052&daid=164&from_ui=1&pttype=1&dumy=&fp=loginerroralert&0-0-157510&" +
                        "mibao_css=m_webqq&t=undefined&g=1&js_type=0&js_ver=10184&login_sig=&pt_randsalt=3",
                "https://ui.ptlogin2.qq.com/cgi-bin/login?" +
                        "daid=164&target=self&style=16&mibao_css=m_webqq&" +
                        "appid=501004106&enable_qlogin=0&no_verifyimg=1&" +
                        "s_url=http%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&" +
                        "strong_login=1&login_state=10&t=20131024001"
        )
        @JvmStatic val GET_PTWEBQQ = ApiURL(
                "{1}",
                null
        )
        @JvmStatic val GET_VFWEBQQ = ApiURL(
                "http://s.web2.qq.com/api/getvfwebqq?ptwebqq={1}&clientid=53999199&psessionid=&t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val GET_UIN_AND_PSESSIONID = ApiURL(
                "http://d1.web2.qq.com/channel/login2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_GROUP_LIST = ApiURL(
                "http://s.web2.qq.com/api/get_group_name_list_mask2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val POLL_MESSAGE = ApiURL(
                "http://d1.web2.qq.com/channel/poll2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val SEND_MESSAGE_TO_GROUP = ApiURL(
                "http://d1.web2.qq.com/channel/send_qun_msg2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_FRIEND_LIST = ApiURL(
                "http://s.web2.qq.com/api/get_user_friends2",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val SEND_MESSAGE_TO_FRIEND = ApiURL(
                "http://d1.web2.qq.com/channel/send_buddy_msg2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_DISCUSS_LIST = ApiURL(
                "http://s.web2.qq.com/api/get_discus_list?clientid=53999199&psessionid={1}&vfwebqq={2}&t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val SEND_MESSAGE_TO_DISCUSS = ApiURL(
                "http://d1.web2.qq.com/channel/send_discu_msg2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_ACCOUNT_INFO = ApiURL(
                "http://s.web2.qq.com/api/get_self_info2?t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val GET_RECENT_LIST = ApiURL(
                "http://d1.web2.qq.com/channel/get_recent_list2",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_FRIEND_STATUS = ApiURL(
                "http://d1.web2.qq.com/channel/get_online_buddies2?vfwebqq={1}&clientid=53999199&psessionid={2}&t=0.1",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_GROUP_INFO = ApiURL(
                "http://s.web2.qq.com/api/get_group_info_ext2?gcode={1}&vfwebqq={2}&t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val GET_QQ_BY_ID = ApiURL(
                "http://s.web2.qq.com/api/get_friend_uin2?tuin={1}&type=1&vfwebqq={2}&t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
        @JvmStatic val GET_DISCUSS_INFO = ApiURL(
                "http://d1.web2.qq.com/channel/get_discu_info?did={1}&vfwebqq={2}&clientid=53999199&psessionid={3}&t=0.1",
                "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"
        )
        @JvmStatic val GET_FRIEND_INFO = ApiURL(
                "http://s.web2.qq.com/api/get_friend_info2?tuin={1}&vfwebqq={2}&clientid=53999199&psessionid={3}&t=0.1",
                "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"
        )
    }
}
