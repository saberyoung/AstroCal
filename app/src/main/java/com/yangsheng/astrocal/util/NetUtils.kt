package com.yangsheng.astrocal.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object NetUtils {

    fun httpGetBytes(url: String, timeoutMs: Int = 15000): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            val stream: InputStream = if (code in 200..299) conn.inputStream else conn.errorStream
            val bytes = stream.readBytes()
            if (code !in 200..299) {
                throw RuntimeException("HTTP $code: ${bytes.decodeToString()}")
            }
            return bytes
        } finally {
            conn.disconnect()
        }
    }

    fun httpGetText(url: String, timeoutMs: Int = 15000): String {
        val bytes = httpGetBytes(url, timeoutMs)
        return bytes.decodeToString()
    }

    fun urlEncode(s: String): String =
        java.net.URLEncoder.encode(s, Charsets.UTF_8.name())
}