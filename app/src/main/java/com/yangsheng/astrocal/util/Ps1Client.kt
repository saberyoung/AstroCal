package com.yangsheng.astrocal.util

import kotlin.math.roundToInt

/**
 * Professional PS1 finder client (single source of truth).
 *
 * - PNG:
 *    - Color (g/r/i): blue=g, green=r, red=i
 *    - Single band: one filter (fed into "red")
 * - FITS:
 *    - single-band FITS (contains WCS) for Gaia overlay
 */
object Ps1Client {

    private const val FILENAMES_URL = "https://ps1images.stsci.edu/cgi-bin/ps1filenames.py"
    private const val FITSCUT_URL = "https://ps1images.stsci.edu/cgi-bin/fitscut.cgi"

    enum class Band(val code: String) { G("g"), R("r"), I("i"), Z("z"), Y("y") }

    sealed class PngMode {
        data object ColorGRI : PngMode() // blue=g, green=r, red=i
        data class Single(val band: Band) : PngMode()
    }

    fun downloadPng(
        raDeg: Double,
        decDeg: Double,
        fovArcmin: Double,
        pixscaleArcsecPerPx: Double? = null,
        mode: PngMode = PngMode.ColorGRI,
        timeoutMs: Int = 25000
    ): ByteArray {
        val sizePx = computeSizePx(fovArcmin, pixscaleArcsecPerPx)

        val url = when (mode) {
            is PngMode.ColorGRI -> {
                val fnG = fetchFilename(raDeg, decDeg, Band.G)
                val fnR = fetchFilename(raDeg, decDeg, Band.R)
                val fnI = fetchFilename(raDeg, decDeg, Band.I)
                buildString {
                    append(FITSCUT_URL)
                    append("?ra=$raDeg&dec=$decDeg")
                    append("&size=$sizePx")
                    append("&format=png")
                    append("&red=${NetUtils.urlEncode(fnI)}")
                    append("&green=${NetUtils.urlEncode(fnR)}")
                    append("&blue=${NetUtils.urlEncode(fnG)}")
                }
            }

            is PngMode.Single -> {
                val fn = fetchFilename(raDeg, decDeg, mode.band)
                buildString {
                    append(FITSCUT_URL)
                    append("?ra=$raDeg&dec=$decDeg")
                    append("&size=$sizePx")
                    append("&format=png")
                    append("&red=${NetUtils.urlEncode(fn)}")
                }
            }
        }

        return NetUtils.httpGetBytes(url, timeoutMs)
    }

    fun downloadFits(
        raDeg: Double,
        decDeg: Double,
        fovArcmin: Double,
        pixscaleArcsecPerPx: Double? = null,
        band: Band = Band.R,
        timeoutMs: Int = 30000
    ): ByteArray {
        val sizePx = computeSizePx(fovArcmin, pixscaleArcsecPerPx)
        val fn = fetchFilename(raDeg, decDeg, band)
        val url = buildString {
            append(FITSCUT_URL)
            append("?ra=$raDeg&dec=$decDeg")
            append("&size=$sizePx")
            append("&format=fits")
            append("&red=${NetUtils.urlEncode(fn)}")
        }
        return NetUtils.httpGetBytes(url, timeoutMs)
    }

    fun fetchFilename(raDeg: Double, decDeg: Double, band: Band, timeoutMs: Int = 20000): String {
        val url = buildString {
            append(FILENAMES_URL)
            append("?ra=$raDeg&dec=$decDeg")
            append("&filters=${NetUtils.urlEncode(band.code)}")
        }
        val text = NetUtils.httpGetText(url, timeoutMs)

        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            .toList()

        if (lines.size < 2) throw RuntimeException("PS1 filenames: empty/no data response")

        val header = lines.first().split(Regex("\\s+"))
        val filenameIdx = header.indexOf("filename").takeIf { it >= 0 } ?: 0
        val filterIdx = header.indexOf("filter").takeIf { it >= 0 }

        val data = lines.drop(1)
        val chosen = data.firstOrNull { row ->
            val cols = row.split(Regex("\\s+"))
            if (cols.size <= filenameIdx) return@firstOrNull false
            if (filterIdx != null && cols.size > filterIdx) cols[filterIdx].lowercase() == band.code else true
        } ?: data.first()

        val cols = chosen.split(Regex("\\s+"))
        if (cols.size <= filenameIdx) throw RuntimeException("PS1 filenames: bad row: $chosen")
        return cols[filenameIdx]
    }

    private fun computeSizePx(fovArcmin: Double, pixscaleArcsecPerPx: Double?): Int {
        val fovArcsec = fovArcmin * 60.0
        val ps = (pixscaleArcsecPerPx ?: 0.25).coerceIn(0.05, 2.0)
        val raw = (fovArcsec / ps).roundToInt()
        return raw.coerceIn(128, 4096)
    }
}
