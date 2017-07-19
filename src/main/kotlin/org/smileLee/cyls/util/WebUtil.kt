package org.smileLee.cyls.util

import java.io.*
import java.net.*

object WebUtil {

    /**
     * @param urlString :请求接口
     *
     * @return 返回结果
     */
    fun request(urlString: String, parameters: Map<String, String> = HashMap(), mode: String = "GET"): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = mode
        for ((key, value) in parameters) {
            connection.addRequestProperty(key, value)
        }
        connection.connect()
        val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
        val ret = StringBuilder()
        while (true) ret.append((reader.readLine() ?: break))
        return ret.toString()
    }

}
