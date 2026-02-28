package com.yangsheng.astrocal.util

import kotlin.math.*

object SphericalUtils {
    fun angularSeparationDeg(ra1Deg: Double, dec1Deg: Double, ra2Deg: Double, dec2Deg: Double): Double {
        val ra1 = Math.toRadians(ra1Deg)
        val dec1 = Math.toRadians(dec1Deg)
        val ra2 = Math.toRadians(ra2Deg)
        val dec2 = Math.toRadians(dec2Deg)

        val cosSep = sin(dec1) * sin(dec2) + cos(dec1) * cos(dec2) * cos(ra1 - ra2)
        val sep = acos(cosSep.coerceIn(-1.0, 1.0))
        return Math.toDegrees(sep)
    }
}