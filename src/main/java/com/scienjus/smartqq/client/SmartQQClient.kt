package com.scienjus.smartqq.client

import com.alibaba.fastjson.*
import com.scienjus.smartqq.callback.*
import com.scienjus.smartqq.constant.*
import com.scienjus.smartqq.frame.*
import com.scienjus.smartqq.model.*
import com.scienjus.smartqq.model.Category
import net.dongliu.requests.*
import net.dongliu.requests.exception.*
import org.apache.log4j.*
import java.io.*
import java.net.*
import java.util.*

/**
 * Api客户端.

 * @author ScienJus
 * *
 * @author [Liang Ding](http://88250.b3log.org)
 * *
 * @date 2015/12/18.
 */
class SmartQQClient @JvmOverloads constructor(
        val callback: MessageCallback,
        qrCodeFile: File = File("qrcode.png")
) : Closeable {

    //客户端
    private val client = Client.pooled().maxPerRoute(5).maxTotal(10).build()!!

    //会话
    private val session: Session

    //二维码令牌
    private lateinit var qrsig: String

    //二维码窗口
    private val qrframe = QRCodeFrame()

    //鉴权参数
    private lateinit var ptwebqq: String
    private lateinit var vfwebqq: String
    private lateinit var psessionid: String
    private var uin = 0L

    //线程开关
    @Volatile private var pollStarted = false

    init {
        this.session = client.session()
        login(qrCodeFile)
    }

    /**
     * 登录
     */
    private fun login(qrCodeFile: File) {
        val url = getAndVerifyQRCode(qrCodeFile)
        getPtwebqq(url)
        getVfwebqq()
        getUinAndPsessionid()
        friendStatus //修复Api返回码[103]的问题
        LOGGER.info(accountInfo.nick!! + "，欢迎！") //登录成功欢迎语
    }

    val runner = Runnable {
        while (true) {
            if (!pollStarted) {
                return@Runnable
            }
            try {
                pollMessage(callback)
            } catch (e: RequestException) {
                //忽略SocketTimeoutException
                if (e.cause !is SocketTimeoutException) {
                    LOGGER.error(e.message)
                }
            } catch (e: Exception) {
                LOGGER.error(e.message)
            }
        }
    }

    fun start() {
        this.pollStarted = true
        val pollThread = Thread(runner)
        pollThread.start()
    }

    //登录流程1：获取二维码
    //登录流程2：验证二维码扫描
    private fun getAndVerifyQRCode(qrCodeFile: File): String {
        getAndVerify@ while (true) {
            LOGGER.debug("开始获取二维码")

            //本地存储二维码图片
            val filePath: String
            try {
                filePath = qrCodeFile.canonicalPath
            } catch (e: IOException) {
                throw IllegalStateException("二维码保存失败")
            }

            val getQRCodeResponse = session.get(ApiURL.GET_QR_CODE.url)
                    .addHeader("User-Agent", ApiURL.USER_AGENT)
                    .file(filePath)
            for (cookie in getQRCodeResponse.cookies) {
                if (cookie.name == "qrsig") {
                    qrsig = cookie.value
                    break
                }
            }
            LOGGER.info("二维码已保存在 $filePath 文件中，请打开手机QQ并扫描二维码")
            qrframe.showQRCode(filePath) //显示二维码

            LOGGER.debug("等待扫描二维码")

            //阻塞直到确认二维码认证成功
            verify@ while (true) {
                sleep(1)
                val verifyQRCodeResponse = get(ApiURL.VERIFY_QR_CODE, hash33(qrsig))
                val result = verifyQRCodeResponse.body
                if (result.contains("成功")) {
                    for (content in result.split("','".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (content.startsWith("http")) {
                            LOGGER.info("正在登录，请稍后")
                            qrframe.dispose() //认证成功后释放窗体资源
                            return content
                        }
                    }
                } else if (result.contains("已失效")) {
                    LOGGER.info("二维码已失效，尝试重新获取二维码")
                    qrframe.waitForQRCode() //等待新的二维码
                    continue@getAndVerify
                }
            }
        }
    }

    //登录流程3：获取ptwebqq
    private fun getPtwebqq(url: String) {
        LOGGER.debug("开始获取ptwebqq")

        val response = get(ApiURL.GET_PTWEBQQ, url)
        this.ptwebqq = response.cookies.get("ptwebqq").iterator().next().value
    }

    //登录流程4：获取vfwebqq
    private fun getVfwebqq() {
        LOGGER.debug("开始获取vfwebqq")

        val response = get(ApiURL.GET_VFWEBQQ, ptwebqq)
        this.vfwebqq = getJsonObjectResult(response).getString("vfwebqq")
    }

    //登录流程5：获取uin和psessionid
    private fun getUinAndPsessionid() {
        LOGGER.debug("开始获取uin和psessionid")

        val r = JSONObject()
        r.put("ptwebqq", ptwebqq)
        r.put("clientid", Client_ID)
        r.put("psessionid", "")
        r.put("status", "online")

        val response = post(ApiURL.GET_UIN_AND_PSESSIONID, r)
        val result = getJsonObjectResult(response)
        this.psessionid = result.getString("psessionid")
        this.uin = result.getLongValue("uin")
    }

    /**
     * 获取群列表

     * @return
     */
    val groupList: List<Group>
        get() {
            LOGGER.debug("开始获取群列表")

            val r = JSONObject()
            r.put("vfwebqq", vfwebqq)
            r.put("hash", hash())

            val response = post(ApiURL.GET_GROUP_LIST, r)
            val result = getJsonObjectResult(response)
            return JSON.parseArray(result.getJSONArray("gnamelist").toJSONString(), Group::class.java)
        }

    /**
     * 拉取消息

     * @param callback 获取消息后的回调
     */
    private fun pollMessage(callback: MessageCallback) {
        LOGGER.debug("开始接收消息")

        val r = JSONObject()
        r.put("ptwebqq", ptwebqq)
        r.put("clientid", Client_ID)
        r.put("psessionid", psessionid)
        r.put("key", "")

        val response = post(ApiURL.POLL_MESSAGE, r)
        val array = getJsonArrayResult(response)
        var i = 0
        if (array != null) while (i < array.size) {
            val message = array.getJSONObject(i)
            val type = message.getString("poll_type")
            if ("message" == type) {
                callback.onMessage(Message(message.getJSONObject("value")))
            } else if ("group_message" == type) {
                callback.onGroupMessage(GroupMessage(message.getJSONObject("value")))
            } else if ("discu_message" == type) {
                callback.onDiscussMessage(DiscussMessage(message.getJSONObject("value")))
            }
            i++
        }
    }

    /**
     * 发送群消息

     * @param groupId 群id
     * *
     * @param msg     消息内容
     */
    fun sendMessageToGroup(groupId: Long, msg: String) {
        LOGGER.debug("开始发送群消息")

        val r = JSONObject()
        r.put("group_uin", groupId)
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))))  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573)
        r.put("clientid", Client_ID)
        r.put("msg_id", MESSAGE_ID++)
        r.put("psessionid", psessionid)

        val response = postWithRetry(ApiURL.SEND_MESSAGE_TO_GROUP, r)
        checkSendMsgResult(response)
    }

    /**
     * 发送讨论组消息

     * @param discussId 讨论组id
     * *
     * @param msg       消息内容
     */
    fun sendMessageToDiscuss(discussId: Long, msg: String) {
        LOGGER.debug("开始发送讨论组消息")

        val r = JSONObject()
        r.put("did", discussId)
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))))  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573)
        r.put("clientid", Client_ID)
        r.put("msg_id", MESSAGE_ID++)
        r.put("psessionid", psessionid)

        val response = postWithRetry(ApiURL.SEND_MESSAGE_TO_DISCUSS, r)
        checkSendMsgResult(response)
    }

    /**
     * 发送消息

     * @param friendId 好友id
     * *
     * @param msg      消息内容
     */
    fun sendMessageToFriend(friendId: Long, msg: String) {
        LOGGER.debug("开始发送消息")

        val r = JSONObject()
        r.put("to", friendId)
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))))  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573)
        r.put("clientid", Client_ID)
        r.put("msg_id", MESSAGE_ID++)
        r.put("psessionid", psessionid)

        val response = postWithRetry(ApiURL.SEND_MESSAGE_TO_FRIEND, r)
        checkSendMsgResult(response)
    }

    /**
     * 获得讨论组列表

     * @return
     */
    val discussList: List<Discuss>
        get() {
            LOGGER.debug("开始获取讨论组列表")

            val response = get(ApiURL.GET_DISCUSS_LIST, psessionid, vfwebqq)
            return JSON.parseArray(getJsonObjectResult(response).getJSONArray("dnamelist").toJSONString(), Discuss::class.java)
        }

    /**
     * 获得好友列表（包含分组信息）

     * @return
     */
    //获得好友信息
    //获得分组
    val friendListWithCategory: List<Category>
        get() {
            LOGGER.debug("开始获取好友列表")

            val r = JSONObject()
            r.put("vfwebqq", vfwebqq)
            r.put("hash", hash())

            val response = post(ApiURL.GET_FRIEND_LIST, r)
            val result = getJsonObjectResult(response)
            val friendMap = parseFriendMap(result)
            val categories = result.getJSONArray("categories")
            val categoryMap = HashMap<Int, Category>()
            categoryMap.put(0, Category.defaultCategory())
            run {
                var i = 0
                while (categories != null && i < categories.size) {
                    val category = categories.getObject(i, Category::class.java)
                    categoryMap.put(category.index, category)
                    i++
                }
            }
            val friends = result.getJSONArray("friends")
            var i = 0
            while (friends != null && i < friends.size) {
                val item = friends.getJSONObject(i)
                val friend = friendMap[item.getLongValue("uin")]
                categoryMap[item.getIntValue("categories")]!!.addFriend(friend!!)
                i++
            }
            return ArrayList(categoryMap.values)
        }

    /**
     * 获取好友列表

     * @return
     */
    val friendList: List<Friend>
        get() {
            LOGGER.debug("开始获取好友列表")

            val r = JSONObject()
            r.put("vfwebqq", vfwebqq)
            r.put("hash", hash())

            val response = post(ApiURL.GET_FRIEND_LIST, r)
            return ArrayList(parseFriendMap(getJsonObjectResult(response)).values)
        }

    /**
     * 获得当前登录用户的详细信息

     * @return
     */
    val accountInfo: UserInfo
        get() {
            LOGGER.debug("开始获取登录用户信息")

            val response = get(ApiURL.GET_ACCOUNT_INFO)
            return JSON.parseObject(getJsonObjectResult(response).toJSONString(), UserInfo::class.java)
        }

    /**
     * 获得好友的详细信息

     * @return
     */
    fun getFriendInfo(friendId: Long): UserInfo {
        LOGGER.debug("开始获取好友信息")

        val response = get(ApiURL.GET_FRIEND_INFO, friendId, vfwebqq, psessionid)
        return JSON.parseObject(getJsonObjectResult(response).toJSONString(), UserInfo::class.java)
    }

    /**
     * 获得最近会话列表

     * @return
     */
    val recentList: List<Recent>
        get() {
            LOGGER.debug("开始获取最近会话列表")

            val r = JSONObject()
            r.put("vfwebqq", vfwebqq)
            r.put("clientid", Client_ID)
            r.put("psessionid", "")

            val response = post(ApiURL.GET_RECENT_LIST, r)
            return JSON.parseArray(getJsonArrayResult(response)!!.toJSONString(), Recent::class.java)
        }

    /**
     * 获得qq号

     * @param friendId 用户id
     * *
     * @return
     */
    fun getQQById(friendId: Long): Long {
        LOGGER.debug("开始获取QQ号")

        val response = get(ApiURL.GET_QQ_BY_ID, friendId, vfwebqq)
        return getJsonObjectResult(response).getLongValue("account")
    }

    /**
     * 获得好友的qq号

     * @param friend 好友对象
     * *
     * @return
     */
    fun getQQById(friend: Friend): Long {
        return getQQById(friend.userId)
    }

    /**
     * 获得群友的qq号

     * @param user 群友对象
     * *
     * @return
     */
    fun getQQById(user: GroupUser): Long {
        return getQQById(user.uin)
    }

    /**
     * 获得讨论组成员的qq号

     * @param user 讨论组成员对象
     * *
     * @return
     */
    fun getQQById(user: DiscussUser): Long {
        return getQQById(user.uin)
    }

    /**
     * 获得私聊消息发送者的qq号

     * @param msg 私聊消息
     * *
     * @return
     */
    fun getQQById(msg: Message): Long {
        return getQQById(msg.userId)
    }

    /**
     * 获得群消息发送者的qq号

     * @param msg 群消息
     * *
     * @return
     */
    fun getQQById(msg: GroupMessage): Long {
        return getQQById(msg.userId)
    }

    /**
     * 获得讨论组消息发送者的qq号

     * @param msg 讨论组消息
     * *
     * @return
     */
    fun getQQById(msg: DiscussMessage): Long {
        return getQQById(msg.userId)
    }

    /**
     * 获得登录状态

     * @return
     */
    val friendStatus: List<FriendStatus>
        get() {
            LOGGER.debug("开始获取好友状态")

            val response = get(ApiURL.GET_FRIEND_STATUS, vfwebqq, psessionid)
            return JSON.parseArray(getJsonArrayResult(response)!!.toJSONString(), FriendStatus::class.java)
        }

    /**
     * 获得群的详细信息

     * @param groupCode 群编号
     * *
     * @return
     */
    fun getGroupInfo(groupCode: Long): GroupInfo {
        LOGGER.debug("开始获取群资料")

        val response = get(ApiURL.GET_GROUP_INFO, groupCode, vfwebqq)
        val result = getJsonObjectResult(response)
        val groupInfo = result.getObject("ginfo", GroupInfo::class.java)
        //获得群成员信息
        val groupUserMap = HashMap<Long, GroupUser>()
        val minfo = result.getJSONArray("minfo")
        run {
            var i = 0
            while (minfo != null && i < minfo.size) {
                val groupUser = minfo.getObject(i, GroupUser::class.java)
                groupUserMap.put(groupUser.uin, groupUser)
                groupInfo.addUser(groupUser)
                i++
            }
        }
        val stats = result.getJSONArray("stats")
        run {
            var i = 0
            while (stats != null && i < stats.size) {
                val item = stats.getJSONObject(i)
                val groupUser = groupUserMap[item.getLongValue("uin")]
                groupUser!!.clientType = item.getIntValue("client_type")
                groupUser.status = item.getIntValue("stat")
                i++
            }
        }
        val cards = result.getJSONArray("cards")
        run {
            var i = 0
            while (cards != null && i < cards.size) {
                val item = cards.getJSONObject(i)
                groupUserMap[item.getLongValue("muin")]!!.card = item.getString("card")
                i++
            }
        }
        val vipinfo = result.getJSONArray("vipinfo")
        var i = 0
        while (vipinfo != null && i < vipinfo.size) {
            val item = vipinfo.getJSONObject(i)
            val groupUser = groupUserMap[item.getLongValue("u")]!!
            groupUser.isVip = item.getIntValue("is_vip") == 1
            groupUser.vipLevel = item.getIntValue("vip_level")
            i++
        }
        return groupInfo
    }

    /**
     * 获得讨论组的详细信息

     * @param discussId 讨论组id
     * *
     * @return
     */
    fun getDiscussInfo(discussId: Long): DiscussInfo {
        LOGGER.debug("开始获取讨论组资料")

        val response = get(ApiURL.GET_DISCUSS_INFO, discussId, vfwebqq, psessionid)
        val result = getJsonObjectResult(response)
        val discussInfo = result.getObject("info", DiscussInfo::class.java)
        //获得讨论组成员信息
        val discussUserMap = HashMap<Long, DiscussUser>()
        val minfo = result.getJSONArray("mem_info")
        run {
            var i = 0
            while (minfo != null && i < minfo.size) {
                val discussUser = minfo.getObject(i, DiscussUser::class.java)
                discussUserMap.put(discussUser.uin, discussUser)
                discussInfo.addUser(discussUser)
                i++
            }
        }
        val stats = result.getJSONArray("mem_status")
        var i = 0
        while (stats != null && i < stats.size) {
            val item = stats.getJSONObject(i)
            val discussUser = discussUserMap[item.getLongValue("uin")]!!
            discussUser.clientType = item.getIntValue("client_type")
            discussUser.status = item.getString("status")
            i++
        }
        return discussInfo
    }

    //发送get请求
    private operator fun get(url: ApiURL, vararg params: Any): Response<String> {
        val request = session.get(url.buildUrl(*params))
                .addHeader("User-Agent", ApiURL.USER_AGENT)
        if (url.referer != null) {
            request.addHeader("Referer", url.referer)
        }
        return request.text()
    }

    //发送post请求
    private fun post(url: ApiURL, r: JSONObject): Response<String> {
        return session.post(url.url)
                .addHeader("User-Agent", ApiURL.USER_AGENT)
                .addHeader("Referer", url.referer!!)
                .addHeader("Origin", url.origin)
                .addForm("r", r.toJSONString())
                .text()
    }

    //发送post请求，失败时重试
    private fun postWithRetry(url: ApiURL, r: JSONObject): Response<String> {
        var times = 0
        var response: Response<String>
        do {
            response = post(url, r)
            times++
        } while (times < RETRY_TIMES && response.statusCode != 200)
        return response
    }

    //hash加密方法
    private fun hash(): String {
        return hash(uin, ptwebqq)
    }

    @Throws(IOException::class)
    override fun close() {
        this.pollStarted = false
        this.client.close()
    }

    companion object {

        //日志
        private val LOGGER = Logger.getLogger(SmartQQClient::class.java)

        //消息id，这个好像可以随便设置，所以设成全局的
        private var MESSAGE_ID: Long = 43690001

        //客户端id，固定的
        private val Client_ID: Long = 53999199

        //消息发送失败重发次数
        private val RETRY_TIMES: Long = 5

        //用于生成ptqrtoken的哈希函数
        private fun hash33(s: String): Int {
            var e = 0
            val n = s.length
            var i = 0
            while (n > i) {
                e += (e shl 5) + s[i].toInt()
                ++i
            }
            return 2147483647 and e
        }

        //将json解析为好友列表
        private fun parseFriendMap(result: JSONObject): Map<Long, Friend> {
            val friendMap = HashMap<Long, Friend>()
            val info = result.getJSONArray("info")
            run {
                var i = 0
                while (info != null && i < info.size) {
                    val item = info.getJSONObject(i)
                    val friend = Friend()
                    friend.userId = item.getLongValue("uin")
                    friend.nickname = item.getString("nick")
                    friendMap.put(friend.userId, friend)
                    i++
                }
            }
            val marknames = result.getJSONArray("marknames")
            run {
                var i = 0
                while (marknames != null && i < marknames.size) {
                    val item = marknames.getJSONObject(i)
                    friendMap[item.getLongValue("uin")]!!.markname = item.getString("markname")
                    i++
                }
            }
            val vipinfo = result.getJSONArray("vipinfo")
            var i = 0
            while (vipinfo != null && i < vipinfo.size) {
                val item = vipinfo.getJSONObject(i)
                val friend = friendMap[item.getLongValue("u")]!!
                friend.isVip = item.getIntValue("is_vip") == 1
                friend.vipLevel = item.getIntValue("vip_level")
                i++
            }
            return friendMap
        }

        //获取返回json的result字段（JSONObject类型）
        private fun getJsonObjectResult(response: Response<String>): JSONObject {
            return getResponseJson(response).getJSONObject("result")
        }

        //获取返回json的result字段（JSONArray类型）
        private fun getJsonArrayResult(response: Response<String>): JSONArray? {
            return getResponseJson(response).getJSONArray("result")
        }

        //检查消息是否发送成功
        private fun checkSendMsgResult(response: Response<String>) {
            if (response.statusCode != 200) {
                LOGGER.error(String.format("发送失败，Http返回码[%d]", response.statusCode))
            }
            val json = JSON.parseObject(response.body)
            val errCode = json.getInteger("errCode")
            if (errCode != null && errCode == 0) {
                LOGGER.debug("发送成功")
            } else {
                LOGGER.error(String.format("发送失败，Api返回码[%d]", json.getInteger("retcode")))
            }
        }

        //检验Json返回结果
        private fun getResponseJson(response: Response<String>): JSONObject {
            if (response.statusCode != 200) {
                throw RequestException(String.format("请求失败，Http返回码[%d]", response.statusCode))
            }
            val json = JSON.parseObject(response.body)
            val retCode = json.getInteger("retcode")
            when (retCode) {
                null   -> throw RequestException("请求失败，Api返回异常")
                0      -> {
                }
                103    -> LOGGER.error("请求失败，Api返回码[103]。你需要进入http://w.qq.com，检查是否能正常接收消息。如果可以的话点击[设置]->[退出登录]后查看是否恢复正常")
                100100 -> LOGGER.debug("请求失败，Api返回码[100100]")
                else   -> throw RequestException("请求失败，Api返回码[$retCode]")
            }
            return json
        }

        //线程暂停
        private fun sleep(seconds: Long) {
            try {
                Thread.sleep(seconds * 1000)
            } catch (e: InterruptedException) {
                //忽略InterruptedException
            }

        }

        //hash加密方法
        private fun hash(x: Long, K: String): String {
            val N = IntArray(4)
            for (T in 0..K.length - 1) {
                N[T % 4] = N[T % 4] xor K[T].toInt()
            }
            val U = arrayOf("EC", "OK")
            val V = LongArray(4)
            V[0] = x shr 24 and 255 xor U[0][0].toLong()
            V[1] = x shr 16 and 255 xor U[0][1].toLong()
            V[2] = x shr 8 and 255 xor U[1][0].toLong()
            V[3] = x and 255 xor U[1][1].toLong()

            val U1 = LongArray(8)

            for (T in 0..7) {
                U1[T] = if (T % 2 == 0) N[T shr 1].toLong() else V[T shr 1]
            }

            val N1 = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
            var V1 = ""
            for (aU1 in U1) {
                V1 += N1[(aU1 shr 4 and 15).toInt()]
                V1 += N1[(aU1 and 15).toInt()]
            }
            return V1
        }
    }
}
