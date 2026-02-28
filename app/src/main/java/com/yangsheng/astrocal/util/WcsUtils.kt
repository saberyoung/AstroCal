package com.yangsheng.astrocal.util

import kotlin.math.abs

object WcsUtils {

    fun parseFitsHeader(fitsBytes: ByteArray): WcsHeader {
        val text = fitsBytes.decodeToString()
        val cards = mutableListOf<String>()
        var i = 0
        while (i + 80 <= text.length) {
            val card = text.substring(i, i + 80)
            cards.add(card)
            i += 80
            if (card.startsWith("END")) break
        }

        fun getDouble(key: String): Double? {
            val card = cards.firstOrNull { it.startsWith(key.padEnd(8, ' ')) } ?: return null
            val eq = card.indexOf('=')
            if (eq < 0) return null
            val raw = card.substring(eq + 1).split('/')[0].trim()
            return raw.toDoubleOrNull()
        }

        fun getInt(key: String): Int = (getDouble(key) ?: error("Missing $key")).toInt()

        val naxis1 = getInt("NAXIS1")
        val naxis2 = getInt("NAXIS2")
        val crpix1 = getDouble("CRPIX1") ?: error("Missing CRPIX1")
        val crpix2 = getDouble("CRPIX2") ?: error("Missing CRPIX2")
        val crval1 = getDouble("CRVAL1") ?: error("Missing CRVAL1")
        val crval2 = getDouble("CRVAL2") ?: error("Missing CRVAL2")

        val cd11 = getDouble("CD1_1")
        val cd12 = getDouble("CD1_2")
        val cd21 = getDouble("CD2_1")
        val cd22 = getDouble("CD2_2")

        if (cd11 != null && cd12 != null && cd21 != null && cd22 != null) {
            return WcsHeader(naxis1, naxis2, crpix1, crpix2, crval1, crval2, cd11, cd12, cd21, cd22)
        }

        val cdelt1 = getDouble("CDELT1") ?: error("Missing CD matrix and CDELT1")
        val cdelt2 = getDouble("CDELT2") ?: error("Missing CD matrix and CDELT2")
        val pc11 = getDouble("PC1_1") ?: 1.0
        val pc12 = getDouble("PC1_2") ?: 0.0
        val pc21 = getDouble("PC2_1") ?: 0.0
        val pc22 = getDouble("PC2_2") ?: 1.0

        return WcsHeader(
            naxis1, naxis2,
            crpix1, crpix2,
            crval1, crval2,
            pc11 * cdelt1, pc12 * cdelt1,
            pc21 * cdelt2, pc22 * cdelt2
        )
    }

    /**
     * world (deg) -> pixel (0-based), linear CD around CRPIX/CRVAL.
     * 对 PS1 cutout 足够用于 overlay/debug。
     */
    fun worldToPixel(w: WcsHeader, raDeg: Double, decDeg: Double): Pair<Double, Double> {
        var dra = raDeg - w.crval1
        if (dra > 180.0) dra -= 360.0
        if (dra < -180.0) dra += 360.0
        val ddec = decDeg - w.crval2

        val det = w.cd11 * w.cd22 - w.cd12 * w.cd21
        if (abs(det) < 1e-18) error("Singular CD matrix")

        val inv11 = w.cd22 / det
        val inv12 = -w.cd12 / det
        val inv21 = -w.cd21 / det
        val inv22 = w.cd11 / det

        val dx = inv11 * dra + inv12 * ddec
        val dy = inv21 * dra + inv22 * ddec

        val x = (w.crpix1 - 1.0) + dx
        val y = (w.crpix2 - 1.0) + dy
        return x to y
    }

    fun isInside(w: WcsHeader, x: Double, y: Double): Boolean {
        return x >= 0 && y >= 0 &&
                x <= (w.naxis1 - 1).toDouble() &&
                y <= (w.naxis2 - 1).toDouble()
    }
}