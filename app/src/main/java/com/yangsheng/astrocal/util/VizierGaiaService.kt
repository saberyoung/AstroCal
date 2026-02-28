package com.yangsheng.astrocal.util

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object VizierGaiaService {

    fun coneSearchGaiaDr3(
        raDeg: Double,
        decDeg: Double,
        radiusArcmin: Double,
        maxRows: Int = 500
    ): List<GaiaStar> {
        val endpoint = "https://tapvizier.cds.unistra.fr/TAPVizieR/tap/sync"
        val radDeg = radiusArcmin / 60.0

        val adql = """
            SELECT TOP $maxRows
                "RA_ICRS" as ra,
                "DE_ICRS" as dec,
                "Gmag" as gmag,
                "Plx" as plx
            FROM "I/355/gaiadr3"
            WHERE 1=CONTAINS(
                POINT('ICRS', "RA_ICRS", "DE_ICRS"),
                CIRCLE('ICRS', $raDeg, $decDeg, $radDeg)
            )
            ORDER BY gmag ASC
        """.trimIndent()

        val params = mapOf(
            "request" to "doQuery",
            "lang" to "ADQL",
            "format" to "csv",
            "query" to adql
        )
        val postBody = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20000
            readTimeout = 20000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }

        return try {
            conn.outputStream.use { it.write(postBody.toByteArray()) }
            val code = conn.responseCode
            val bytes = (if (code in 200..299) conn.inputStream else conn.errorStream)?.readBytes()
                ?: return emptyList()
            parseCsv(bytes.decodeToString())
        } catch (_: Throwable) {
            emptyList()
        } finally {
            conn.disconnect()
        }
    }

    private fun parseCsv(csv: String): List<GaiaStar> {
        val lines = csv.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (lines.size <= 1) return emptyList()

        val header = lines.first().split(",").map { it.trim().trim('"') }
        fun idx(name: String) = header.indexOf(name)

        val ira = idx("ra")
        val idec = idx("dec")
        val ig = idx("gmag")
        val ip = idx("plx")
        if (ira < 0 || idec < 0) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val cols = line.split(",")
            val ra = cols.getOrNull(ira)?.trim()?.toDoubleOrNull() ?: return@mapNotNull null
            val dec = cols.getOrNull(idec)?.trim()?.toDoubleOrNull() ?: return@mapNotNull null
            val g = cols.getOrNull(ig)?.trim()?.toDoubleOrNull()
            val plx = cols.getOrNull(ip)?.trim()?.toDoubleOrNull()
            GaiaStar(ra, dec, g, plx)
        }
    }
}