package com.yangsheng.astrocal.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.max

object Ps1Service {
    private const val FILENAMES_URL = "https://ps1images.stsci.edu/cgi-bin/ps1filenames.py"
    private const val FITSCUT_URL = "https://ps1images.stsci.edu/cgi-bin/fitscut.cgi"

    fun downloadPng(req: Ps1ImageRequest): ByteArray {
        val filter = req.filter.lowercase()
        val filename = fetchPs1Filename(req.raDeg, req.decDeg, filter)
        val pix = max((req.fovArcmin * 60.0 * 4.0).toInt(), 256)
        val url = buildString {
            append(FITSCUT_URL)
            append("?ra=${req.raDeg}")
            append("&dec=${req.decDeg}")
            append("&size=$pix")
            append("&format=png")
            append("&red=${urlEncode(filename)}")
        }
        return httpGetBytes(url, timeoutMs = 20000)
    }

    fun downloadFitsHeader(req: Ps1ImageRequest): ByteArray {
        val filter = req.filter.lowercase()
        val filename = fetchPs1Filename(req.raDeg, req.decDeg, filter)
        val pix = max((req.fovArcmin * 60.0 * 4.0).toInt(), 256)
        val url = buildString {
            append(FITSCUT_URL)
            append("?ra=${req.raDeg}")
            append("&dec=${req.decDeg}")
            append("&size=$pix")
            append("&format=fits")
            append("&red=${urlEncode(filename)}")
        }
        return httpGetBytes(url, timeoutMs = 20000)
    }

    fun fetchPs1Filename(raDeg: Double, decDeg: Double, filter: String): String {
        val url = buildString {
            append(FILENAMES_URL)
            append("?ra=$raDeg")
            append("&dec=$decDeg")
            append("&filters=${urlEncode(filter)}")
        }
        val text = httpGetText(url, timeoutMs = 20000)

        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            .toList()
        if (lines.isEmpty()) throw RuntimeException("PS1 filenames: empty response")

        val header = lines.first().split(Regex("\\s+"))
        val dataLines = lines.drop(1)
        if (dataLines.isEmpty()) throw RuntimeException("PS1 filenames: no data rows")

        val filenameIdx = header.indexOf("filename").takeIf { it >= 0 } ?: 0
        val filterIdx = header.indexOf("filter").takeIf { it >= 0 }

        val chosen = dataLines.firstOrNull { row ->
            val cols = row.split(Regex("\\s+"))
            if (cols.size <= filenameIdx) return@firstOrNull false
            if (filterIdx != null && cols.size > filterIdx) cols[filterIdx].lowercase() == filter.lowercase() else true
        } ?: dataLines.first()

        val cols = chosen.split(Regex("\\s+"))
        if (cols.size <= filenameIdx) throw RuntimeException("PS1 filenames: bad row: $chosen")
        return cols[filenameIdx]
    }

    private fun urlEncode(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun httpGetBytes(url: String, timeoutMs: Int): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            requestMethod = "GET"
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.readBytes()?.decodeToString()
                throw RuntimeException("HTTP $code: $err")
            }
            return conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun httpGetText(url: String, timeoutMs: Int): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            requestMethod = "GET"
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.readBytes()?.decodeToString()
                throw RuntimeException("HTTP $code: $err")
            }
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                while (true) {
                    val line = br.readLine() ?: break
                    sb.append(line).append('\n')
                }
            }
            return sb.toString()
        } finally {
            conn.disconnect()
        }
    }
}