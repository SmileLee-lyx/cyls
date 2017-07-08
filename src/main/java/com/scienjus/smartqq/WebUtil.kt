package com.scienjus.smartqq

import java.io.*
import java.net.*

object WebUtil {

    /**
     * @param httpUrl :请求接口
     * *
     * @return 返回结果
     */
    fun request(httpUrl: String, param: Map<String, String>?, mode: String): String? {
        val reader: BufferedReader
        var result: String? = null
        val sbf = StringBuilder()

        try {
            val url = URL(httpUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = mode
            if (param != null) {
                for (key in param.keys) {
                    connection.addRequestProperty(key, param[key])
                }
            }
            connection.connect()
            reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            var strRead: String? = null
            while ({ strRead = reader.readLine();strRead }() != null) {
                sbf.append(strRead!!)
                sbf.append("\r\n")
            }
            reader.close()
            result = sbf.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

}
