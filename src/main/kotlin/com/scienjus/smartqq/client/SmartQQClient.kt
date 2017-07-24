package com.scienjus.smartqq.client

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import com.scienjus.smartqq.*
import com.scienjus.smartqq.callback.*
import com.scienjus.smartqq.constant.*
import com.scienjus.smartqq.frame.*
import com.scienjus.smartqq.model.*
import net.dongliu.requests.*
import net.dongliu.requests.exception.*
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
        private val callback: MessageCallback,
        qrCodeFile: File = File("qrcode.png")
) : Closeable {

    //客户端
    private val client = Client.pooled().maxPerRoute(5).maxTotal(10).build() ?:
            throw IllegalStateException("获取client失败")

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
        LOGGER.info("${accountInfo.nick}，欢迎！") //登录成功欢迎语
    }

    private val runner = Runnable {
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
        //阻塞直到确认二维码认证成功
        while (true) {
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
            getQRCodeResponse.cookies.forEach { cookie ->
                if (cookie.name == "qrsig") {
                    qrsig = cookie.value
                    return@forEach
                }
            }
            LOGGER.info("二维码已保存在 $filePath 文件中，请打开手机QQ并扫描二维码")
            qrframe.showQRCode(filePath) //显示二维码

            LOGGER.debug("等待扫描二维码")

            while (true) {
                sleep(1)
                val verifyQRCodeResponse = get(ApiURL.VERIFY_QR_CODE, hash33(qrsig))
                val result = verifyQRCodeResponse.body
                if (result.contains("成功")) {
                    result.split("','".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().forEach { content ->
                        if (content.startsWith("http")) {
                            LOGGER.info("正在登录，请稍后")
                            qrframe.dispose() //认证成功后释放窗体资源
                            return content
                        }
                    }
                } else if (result.contains("已失效")) {
                    LOGGER.info("二维码已失效，尝试重新获取二维码")
                    qrframe.waitForQRCode() //等待新的二维码
                    break
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
        this.vfwebqq = getJsonObjectResult(response)?.getAsJsonPrimitive("vfwebqq")?.asString
                ?: throw IllegalStateException("fail to get vfwebqq")
    }

    //登录流程5：获取uin和psessionid
    private fun getUinAndPsessionid() {
        LOGGER.debug("开始获取uin和psessionid")

        val r = jsonObjectOf(
                "ptwebqq" to ptwebqq,
                "clientid" to Client_ID,
                "psessionid" to "",
                "status" to "online"
        )

        val response = post(ApiURL.GET_UIN_AND_PSESSIONID, r)
        val result = getJsonObjectResult(response)
        this.psessionid = result?.getAsJsonPrimitive("psessionid")?.asString ?:
                throw IllegalStateException("fail to get psessionid")
        this.uin = result.getAsJsonPrimitive("uin")?.asLong ?:
                throw IllegalStateException("fail to get uin")
    }

    /**
     * 获取群列表

     * @return
     */
    val groupList: List<Group>
        get() {
            LOGGER.debug("开始获取群列表")

            val r = jsonObjectOf(
                    "vfwebqq" to vfwebqq,
                    "hash" to hash()
            )

            val response = post(ApiURL.GET_GROUP_LIST, r)
            val result = getJsonObjectResult(response)
            return Gson().fromJson(result?.getAsJsonArrayOrNull("gnamelist") ?:
                    throw IllegalStateException("fail to get group list"))
        }

    /**
     * 拉取消息

     * @param callback 获取消息后的回调
     */
    private fun pollMessage(callback: MessageCallback) {
        LOGGER.debug("开始接收消息")

        val r = jsonObjectOf(
                "ptwebqq" to ptwebqq,
                "clientid" to Client_ID,
                "psessionid" to psessionid,
                "key" to ""
        )

        val response = post(ApiURL.POLL_MESSAGE, r)
        getJsonArrayResult(response)
                ?.forEach {
                    it as JsonObject

                    val type = it["poll_type"].asString
                    when (type) {
                        "message"       -> callback.onMessage(Message(it.getAsJsonObject("value")))
                        "group_message" -> callback.onGroupMessage(GroupMessage(it.getAsJsonObject("value")))
                        "discu_message" -> callback.onDiscussMessage(DiscussMessage(it.getAsJsonObject("value")))
                    }
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

        //注意这里虽然格式是Json，但是实际是String
        val content = Gson().toJson(arrayListOf(msg, arrayListOf("font", Font.DEFAULT_FONT)))
        val r = jsonObjectOf(
                "group_uin" to groupId,
                "content" to content,
                "face" to 573,
                "clientid" to Client_ID,
                "msg_id" to MESSAGE_ID++,
                "psessionid" to psessionid
        )

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

        //注意这里虽然格式是Json，但是实际是String
        val content = Gson().toJson(arrayListOf(msg, arrayListOf("font", Font.DEFAULT_FONT)))
        val r = jsonObjectOf(
                "did" to discussId,
                "content" to content,
                "face" to 573,
                "clientid" to Client_ID,
                "msg_id" to MESSAGE_ID++,
                "psessionid" to psessionid
        )

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

        //注意这里虽然格式是Json，但是实际是String
        val content = Gson().toJson(arrayListOf(msg, arrayListOf("font", Font.DEFAULT_FONT)))
        val r = jsonObjectOf(
                "to" to friendId,
                "content" to content,
                "face" to 573,
                "clientid" to Client_ID,
                "msg_id" to MESSAGE_ID++,
                "psessionid" to psessionid
        )

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
            return Gson().fromJson(getJsonObjectResult(response)?.getAsJsonArrayOrNull("dnamelist")
                    ?: throw IllegalStateException("fail to get discuss list"))
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

            val r = jsonObjectOf(
                    "vfwebqq" to vfwebqq,
                    "hash" to hash()
            )
            val response = post(ApiURL.GET_FRIEND_LIST, r)
            val result = getJsonObjectResult(response)
            val friendMap = parseFriendMap(result)
            val categoryMap = HashMap<Int, Category>()
            categoryMap.put(0, Category.defaultCategory())
            result?.getAsJsonArrayOrNull("categories")
                    ?.map { it.castTo<Category>() }
                    ?.forEach { categoryMap.put(it.index, it) }
            result?.getAsJsonArrayOrNull("friends")
                    ?.forEach {
                        it as JsonObject

                        val friend = friendMap[it["uin"].asLong]
                        if (friend != null) {
                            categoryMap[it["categories"].asInt]?.addFriend(friend)
                        }
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

            val r = jsonObjectOf(
                    "vfwebqq" to vfwebqq,
                    "hash" to hash()
            )

            val response = post(ApiURL.GET_FRIEND_LIST, r)
            return ArrayList(parseFriendMap(getJsonObjectResult(response) ?:
                    throw IllegalStateException("fail to get friend list")).values)
        }

    /**
     * 获得当前登录用户的详细信息

     * @return
     */
    private val accountInfo: UserInfo
        get() {
            LOGGER.debug("开始获取登录用户信息")

            val response = get(ApiURL.GET_ACCOUNT_INFO)
            return Gson().fromJson(getJsonObjectResult(response) ?:
                    throw IllegalStateException("fail to get account info"))
        }

    /**
     * 获得好友的详细信息

     * @return
     */
    fun getFriendInfo(friendId: Long): UserInfo {
        LOGGER.debug("开始获取好友信息")

        val response = get(ApiURL.GET_FRIEND_INFO, friendId, vfwebqq, psessionid)
        return Gson().fromJson(getJsonObjectResult(response) ?:
                throw IllegalStateException("fail to get friend info"))
    }

    /**
     * 获得最近会话列表

     * @return
     */
    val recentList: List<Recent>
        get() {
            LOGGER.debug("开始获取最近会话列表")

            val r = jsonObjectOf(
                    "vfwebqq" to vfwebqq,
                    "clientid" to Client_ID,
                    "psessionid" to ""
            )

            val response = post(ApiURL.GET_RECENT_LIST, r)
            return Gson().fromJson(getJsonArrayResult(response) ?:
                    throw IllegalStateException("recent list is null"))
        }

    /**
     * 获得登录状态

     * @return
     */
    val friendStatus: List<FriendStatus>
        get() {
            LOGGER.debug("开始获取好友状态")

            val response = get(ApiURL.GET_FRIEND_STATUS, vfwebqq, psessionid)
            return Gson().fromJson(getJsonArrayResult(response)
                    ?: throw IllegalStateException("fail to get friend status"))
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
        val groupInfo: GroupInfo = result?.getObject("ginfo") ?:
                throw IllegalStateException("fail to get group status")
        //获得群成员信息
        val groupUserMap = HashMap<Long, GroupUser>()
        result.getAsJsonArrayOrNull("minfo")
                ?.map { it.castTo<GroupUser>() }
                ?.forEach {
                    groupUserMap.put(it.userId, it)
                    groupInfo.addUser(it)
                }
        result.getAsJsonArrayOrNull("stats")
                ?.forEach {
                    it as JsonObject

                    val groupUser = groupUserMap[it["uin"].asLong]
                    groupUser?.clientType = it["client_type"].asInt
                    groupUser?.status = it["stat"].asInt
                }
        result.getAsJsonArrayOrNull("cards")
                ?.forEach {
                    it as JsonObject

                    groupUserMap[it["muin"].asLong]?.card = it["card"].asString
                }
        result.getAsJsonArrayOrNull("vipinfo")
                ?.forEach {
                    it as JsonObject

                    val groupUser = groupUserMap[it["u"].asLong]
                    groupUser?.isVip = it["is_vip"].asInt == 1
                    groupUser?.vipLevel = it["vip_level"].asInt
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
        val discussInfo: DiscussInfo = result?.getObject("info") ?:
                throw IllegalStateException("fail to get discuss info")
        //获得讨论组成员信息
        val discussUserMap = HashMap<Long, DiscussUser>()
        result.getAsJsonArrayOrNull("mem_info")
                ?.map { it.castTo<DiscussUser>() }
                ?.forEach {
                    discussUserMap.put(it.userId, it)
                    discussInfo.addUser(it)
                }
        result.getAsJsonArrayOrNull("mem_status")
                ?.forEach {
                    it as JsonObject

                    val discussUser = discussUserMap[it["uin"].asLong]
                    discussUser?.clientType = it["client_type"].asInt
                    discussUser?.status = it["status"].asString
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
    private fun post(url: ApiURL, r: JsonObject) = session.post(url.url)
            .addHeader("User-Agent", ApiURL.USER_AGENT)
            .addHeader("Referer", url.referer ?: throw IllegalStateException("post referer is null"))
            .addHeader("Origin", url.origin)
            .addForm("r", r.toString())
            .text()

    //发送post请求，失败时重试
    private fun postWithRetry(url: ApiURL, r: JsonObject): Response<String> {
        var times = 0
        var response: Response<String>
        do {
            response = post(url, r)
            times++
        } while (times < RETRY_TIMES && response.statusCode != 200)
        return response
    }

    //hash加密方法
    private fun hash() = hash(uin, ptwebqq)

    override fun close() {
        this.pollStarted = false
        this.client.close()
    }

    companion object {

        //消息id，这个好像可以随便设置，所以设成全局的
        @JvmStatic private var MESSAGE_ID: Long = 43690001

        //客户端id，固定的
        @JvmStatic private val Client_ID: Long = 53999199

        //消息发送失败重发次数
        @JvmStatic private val RETRY_TIMES: Long = 5

        //用于生成ptqrtoken的哈希函数
        @JvmStatic private fun hash33(s: String): Int {
            var e = 0
            val n = s.length
            for (i in 0 until n) {
                e += (e shl 5) + s[i].toInt()
            }
            return Int.MAX_VALUE and e
        }

        //将json解析为好友列表
        @JvmStatic private fun parseFriendMap(result: JsonObject?): Map<Long, Friend> {
            val friendMap = HashMap<Long, Friend>()
            result?.getAsJsonArrayOrNull("info")
                    ?.forEach {
                        it as JsonObject

                        val friend = Friend()
                        friend.userId = it["uin"].asLong
                        friend.nickname = it["nick"].asString
                        friendMap.put(friend.userId, friend)
                    }
            result?.getAsJsonArrayOrNull("marknames")
                    ?.forEach {
                        it as JsonObject

                        friendMap[it["uin"].asLong]?.markname = it["markname"].asString
                    }
            result?.getAsJsonArrayOrNull("vipinfo")
                    ?.forEach {
                        it as JsonObject

                        val friend = friendMap[it["u"].asLong]
                        friend?.isVip = it["is_vip"].asInt == 1
                        friend?.vipLevel = it["vip_level"].asInt
                    }
            return friendMap
        }

        //获取返回json的result字段（JSONObject类型）
        private fun getJsonObjectResult(response: Response<String>) =
                getResponseJson(response).getAsJsonObject("result")

        //获取返回json的result字段（JSONArray类型）
        private fun getJsonArrayResult(response: Response<String>) =
                getResponseJson(response).getAsJsonArrayOrNull("result")

        //检查消息是否发送成功
        @JvmStatic private fun checkSendMsgResult(response: Response<String>) {
            if (response.statusCode != 200) {
                LOGGER.error(String.format("发送失败，Http返回码[%d]", response.statusCode))
            }
            val json = JsonParser().parseAsJsonObject(response.body)
            val errCode = json["errCode"].asInt
            if (errCode == 0) {
                LOGGER.debug("发送成功")
            } else {
                LOGGER.error(String.format("发送失败，Api返回码[%d]", json["retcode"].asInt))
            }
        }

        //检验Json返回结果
        @JvmStatic private fun getResponseJson(response: Response<String>): JsonObject {
            if (response.statusCode != 200) {
                throw RequestException(String.format("请求失败，Http返回码[%d]", response.statusCode))
            }
            val json = JsonParser().parseAsJsonObject(response.body)
            val retCode = try {
                json.getAsJsonPrimitive("retcode")?.asInt
            } catch (_: ClassCastException) {
                null
            }
            when (retCode) {
                null   -> throw RequestException("请求失败，Api返回异常")
                0      -> run {}
                103    -> LOGGER.error("请求失败，Api返回码[103]。你需要进入http://w.qq.com，检查是否能正常接收消息。如果可以的话点击[设置]->[退出登录]后查看是否恢复正常")
                100100 -> LOGGER.debug("请求失败，Api返回码[100100]")
                else   -> throw RequestException("请求失败，Api返回码[$retCode]")
            }
            return json
        }

        //线程暂停
        @JvmStatic private fun sleep(seconds: Long) = Thread.sleep(seconds * 1000)

        //hash加密方法
        private fun hash(x: Long, K: String): String {
            val N = IntArray(4)
            (0 until K.length).forEach { T -> N[T % 4] = N[T % 4] xor K[T].toInt() }
            val U = arrayOf("EC", "OK")
            val V = LongArray(4)
            V[0] = x shr 24 and 255 xor U[0][0].toLong()
            V[1] = x shr 16 and 255 xor U[0][1].toLong()
            V[2] = x shr 8 and 255 xor U[1][0].toLong()
            V[3] = x and 255 xor U[1][1].toLong()

            val U1 = LongArray(8) { T -> if (T % 2 == 0) N[T shr 1].toLong() else V[T shr 1] }

            val N1 = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
            var V1 = ""
            U1.forEach { aU1 ->
                V1 += N1[(aU1 shr 4 and 15).toInt()]
                V1 += N1[(aU1 and 15).toInt()]
            }
            return V1
        }
    }
}
