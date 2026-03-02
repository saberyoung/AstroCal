package com.yangsheng.astrocal.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

object OacApiClient {
    private const val BASE = "https://api.astrocats.space"

    private fun enc(s: String): String =
        URLEncoder.encode(s, StandardCharsets.UTF_8.toString())

    private fun openPossiblyGzippedStream(conn: HttpURLConnection, code: Int): InputStream {
        val raw = if (code in 200..299) conn.inputStream else conn.errorStream
        val encoding = conn.contentEncoding?.lowercase()?.trim()
        return if (encoding == "gzip") GZIPInputStream(raw) else raw
    }

    private suspend fun httpGetText(url: String): String = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        conn.requestMethod = "GET"
        conn.connectTimeout = 15_000
        conn.readTimeout = 25_000

        // 关键：允许 gzip，并且我们自己解压（否则你看到的是乱码）
        conn.setRequestProperty("Accept-Encoding", "gzip")
        conn.setRequestProperty("Accept", "*/*")

        try {
            val code = conn.responseCode
            val stream = openPossiblyGzippedStream(conn, code)
            val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (code !in 200..299) {
                throw RuntimeException("HTTP $code: ${text.take(800)}")
            }
            text
        } finally {
            conn.disconnect()
        }
    }

    suspend fun getText(path: String, query: Map<String, String?> = emptyMap()): String {
        val q = query
            .filterValues { !it.isNullOrBlank() }
            .entries
            .joinToString("&") { "${enc(it.key)}=${enc(it.value!!)}" }

        val url = if (q.isBlank()) "$BASE/$path" else "$BASE/$path?$q"
        return httpGetText(url)
    }

    /** Query full object JSON by name, e.g. "2020faa" or "SN2014J". */
    suspend fun queryByName(name: String): String {
        val safe = name.trim()
        require(safe.isNotEmpty())
        return getText(path = enc(safe))
    }

    /** Cone search in SN catalog. ra/dec can be sexagesimal or degrees. radius in arcsec. */
    suspend fun coneSearchSupernovaeTsv(ra: String, dec: String, radiusArcsec: String): String {
        return getText(
            path = "catalog/sne",
            query = mapOf(
                "ra" to ra.trim(),
                "dec" to dec.trim(),
                "radius" to radiusArcsec.trim(),
                "format" to "tsv"
            )
        )
    }

    /** Filter by claimed type in SN catalog, supports regex. Return TSV for lightweight parsing. */
    suspend fun filterByClaimedTypeTsv(type: String): String {
        return getText(
            path = "catalog/sne/claimedtype",
            query = mapOf(
                "claimedtype" to type.trim(),
                "format" to "tsv"
            )
        )
    }
}