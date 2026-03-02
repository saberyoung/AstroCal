package com.yangsheng.astrocal.util

import kotlin.math.*

object WcsUtils {

    /**
     * 兼容你 FinderScreen 里正在调用的名字
     */
    fun parseFitsHeaderFromFitsFile(fitsBytes: ByteArray): WcsHeader? = parseFitsHeader(fitsBytes)

    fun parseFitsHeader(fitsBytes: ByteArray): WcsHeader? {
        return try {
            val headerBytes = extractPrimaryHeaderBlock(fitsBytes)
            val cards = parseCards(headerBytes)
            toWcs(cards)
        } catch (_: Throwable) {
            null
        }
    }

    private fun extractPrimaryHeaderBlock(fits: ByteArray): ByteArray {
        var pos = 0
        while (pos + 80 <= fits.size) {
            val card = String(fits, pos, 80, Charsets.US_ASCII)
            val key = card.substring(0, 8).trim()
            if (key == "END") {
                val endPos = pos + 80
                val paddedEnd = ((endPos + 2879) / 2880) * 2880
                return fits.copyOfRange(0, min(paddedEnd, fits.size))
            }
            pos += 80
        }
        return fits.copyOfRange(0, min(2880, fits.size))
    }

    private fun parseCards(headerBytes: ByteArray): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val nCards = headerBytes.size / 80

        for (i in 0 until nCards) {
            val card = String(headerBytes, i * 80, 80, Charsets.US_ASCII)
            val key = card.substring(0, 8).trim()
            if (key.isEmpty() || key == "END") continue

            val eq = card.indexOf('=')
            if (eq < 0) continue

            val raw = card.substring(eq + 1).trim()
            val slash = raw.indexOf('/')
            val value = if (slash >= 0) raw.substring(0, slash).trim() else raw.trim()

            map[key] = value.trim().trim('\'')
        }

        return map
    }

    private fun toWcs(cards: Map<String, String>): WcsHeader {
        fun i(key: String) = cards[key]?.toIntOrNull()
        fun d(key: String) = cards[key]?.replace('D','E')?.toDoubleOrNull()

        val naxis1 = i("NAXIS1") ?: error("Missing NAXIS1")
        val naxis2 = i("NAXIS2") ?: error("Missing NAXIS2")
        val crpix1 = d("CRPIX1") ?: error("Missing CRPIX1")
        val crpix2 = d("CRPIX2") ?: error("Missing CRPIX2")
        val crval1 = d("CRVAL1") ?: error("Missing CRVAL1")
        val crval2 = d("CRVAL2") ?: error("Missing CRVAL2")

        val cd11 = d("CD1_1")
        val cd12 = d("CD1_2")
        val cd21 = d("CD2_1")
        val cd22 = d("CD2_2")

        if (cd11 != null && cd12 != null && cd21 != null && cd22 != null) {
            return WcsHeader(
                naxis1, naxis2,
                crpix1, crpix2,
                crval1, crval2,
                cd11, cd12, cd21, cd22
            )
        }

        // fallback: PC + CDELT
        val cdelt1 = d("CDELT1") ?: error("Missing CD and CDELT1")
        val cdelt2 = d("CDELT2") ?: error("Missing CD and CDELT2")

        val pc11 = d("PC1_1") ?: 1.0
        val pc12 = d("PC1_2") ?: 0.0
        val pc21 = d("PC2_1") ?: 0.0
        val pc22 = d("PC2_2") ?: 1.0

        val _cd11 = pc11 * cdelt1
        val _cd12 = pc12 * cdelt2
        val _cd21 = pc21 * cdelt1
        val _cd22 = pc22 * cdelt2

        return WcsHeader(
            naxis1, naxis2,
            crpix1, crpix2,
            crval1, crval2,
            _cd11, _cd12, _cd21, _cd22
        )
    }

    /**
     * 简化的 world->pixel（小视场足够用）
     * - 处理 RA wrap 到 [-180,180]，避免跨 0° 时跳变
     * - 线性 CD 近似（你现在 size 720px，大概 3 arcmin 级别，够了）
     *
     * 返回的是 FITS convention 的 pixel（CRPIX 同系），也就是通常接近 1-based。
     */
    fun worldToPixel(header: WcsHeader, raDeg: Double, decDeg: Double): Pair<Double, Double> {
        var dra = raDeg - header.crval1
        if (dra > 180.0) dra -= 360.0
        if (dra < -180.0) dra += 360.0
        val ddec = decDeg - header.crval2

        val det = header.cd11 * header.cd22 - header.cd12 * header.cd21
        if (abs(det) < 1e-30) return Pair(Double.NaN, Double.NaN)

        val inv11 = header.cd22 / det
        val inv12 = -header.cd12 / det
        val inv21 = -header.cd21 / det
        val inv22 = header.cd11 / det

        val dx = inv11 * dra + inv12 * ddec
        val dy = inv21 * dra + inv22 * ddec

        val x = header.crpix1 + dx
        val y = header.crpix2 + dy
        return Pair(x, y)
    }
}