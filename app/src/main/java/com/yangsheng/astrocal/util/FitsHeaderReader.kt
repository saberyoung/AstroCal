package com.yangsheng.astrocal.util

import kotlin.math.min

object FitsHeaderReader {

    /** Extract only the ASCII header (cards) from FITS bytes (primary HDU). */
    fun extractPrimaryHeaderBytes(fits: ByteArray): ByteArray {
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

    /** Parse cards (KEY = VALUE / comment) into map. */
    fun parseCards(headerBytes: ByteArray): Map<String, String> {
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
            val value = (if (slash >= 0) raw.substring(0, slash) else raw).trim()
            map[key] = value.trim('\'')
        }
        return map
    }

    /** Convert to WcsHeader (supports CD matrix or PC+CDELT). */
    fun toWcsHeader(cards: Map<String, String>): WcsHeader {
        fun i(key: String) = cards[key]?.toInt()
        fun d(key: String) = cards[key]?.toDouble()

        val naxis1 = i("NAXIS1") ?: error("Missing NAXIS1")
        val naxis2 = i("NAXIS2") ?: error("Missing NAXIS2")
        val crpix1 = d("CRPIX1") ?: error("Missing CRPIX1")
        val crpix2 = d("CRPIX2") ?: error("Missing CRPIX2")
        val crval1 = d("CRVAL1") ?: error("Missing CRVAL1")
        val crval2 = d("CRVAL2") ?: error("Missing CRVAL2")

        // Prefer CD if present
        val cd11 = d("CD1_1")
        val cd12 = d("CD1_2")
        val cd21 = d("CD2_1")
        val cd22 = d("CD2_2")
        if (cd11 != null && cd12 != null && cd21 != null && cd22 != null) {
            return WcsHeader(naxis1, naxis2, crpix1, crpix2, crval1, crval2, cd11, cd12, cd21, cd22)
        }

        // Else PC + CDELT
        val cdelt1 = d("CDELT1") ?: error("Missing CD matrix and missing CDELT1")
        val cdelt2 = d("CDELT2") ?: error("Missing CD matrix and missing CDELT2")

        val pc11 = d("PC1_1") ?: 1.0
        val pc12 = d("PC1_2") ?: 0.0
        val pc21 = d("PC2_1") ?: 0.0
        val pc22 = d("PC2_2") ?: 1.0

        val _cd11 = pc11 * cdelt1
        val _cd12 = pc12 * cdelt2
        val _cd21 = pc21 * cdelt1
        val _cd22 = pc22 * cdelt2

        return WcsHeader(naxis1, naxis2, crpix1, crpix2, crval1, crval2, _cd11, _cd12, _cd21, _cd22)
    }
}