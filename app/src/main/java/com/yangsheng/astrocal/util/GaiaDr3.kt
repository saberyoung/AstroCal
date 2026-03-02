package com.yangsheng.astrocal.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import kotlin.math.*

data class GaiaSource(
    val sourceId: Long,
    val raDeg: Double,
    val decDeg: Double,
    val gMag: Double?
)

/**
 * Gaia DR3 cone search via ESA TAP sync.
 * 够用、稳定、没 broker 依赖。
 */
object GaiaDr3 {

    private val client = OkHttpClient()

    private fun adqlCone(raDeg: Double, decDeg: Double, radiusDeg: Double): String {
        val ra = String.format(Locale.US, "%.10f", raDeg)
        val dec = String.format(Locale.US, "%.10f", decDeg)
        val rad = String.format(Locale.US, "%.10f", radiusDeg)

        return """
            SELECT TOP 2000
              source_id, ra, dec, phot_g_mean_mag
            FROM gaiadr3.gaia_source
            WHERE 1=CONTAINS(
              POINT('ICRS', ra, dec),
              CIRCLE('ICRS', $ra, $dec, $rad)
            )
            ORDER BY phot_g_mean_mag ASC
        """.trimIndent()
    }

    suspend fun coneSearch(
        raDeg: Double,
        decDeg: Double,
        radiusArcmin: Double
    ): List<GaiaSource> = withContext(Dispatchers.IO) {

        val radiusDeg = radiusArcmin / 60.0
        val query = adqlCone(raDeg, decDeg, radiusDeg)

        val body = FormBody.Builder()
            .add("REQUEST", "doQuery")
            .add("LANG", "ADQL")
            .add("FORMAT", "csv")
            .add("QUERY", query)
            .build()

        val req = Request.Builder()
            .url("https://gea.esac.esa.int/tap-server/tap/sync")
            .post(body)
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("Gaia TAP HTTP ${resp.code}")
            val csv = resp.body?.string() ?: return@use emptyList()

            val lines = csv.lines().filter { it.isNotBlank() }
            if (lines.size <= 1) return@use emptyList()

            // header: source_id,ra,dec,phot_g_mean_mag
            val out = ArrayList<GaiaSource>(lines.size - 1)
            for (i in 1 until lines.size) {
                val cols = lines[i].split(',')
                if (cols.size < 3) continue
                val sid = cols[0].trim().toLongOrNull() ?: continue
                val ra = cols[1].trim().toDoubleOrNull() ?: continue
                val dec = cols[2].trim().toDoubleOrNull() ?: continue
                val g = cols.getOrNull(3)?.trim()?.toDoubleOrNull()
                out.add(GaiaSource(sid, ra, dec, g))
            }
            out
        }
    }
}