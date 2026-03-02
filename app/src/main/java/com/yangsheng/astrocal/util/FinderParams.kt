package com.yangsheng.astrocal.util

import kotlin.math.roundToInt

data class CutoutSpec(
    val pixels: Int,
    val pixscaleArcsec: Double
)

/**
 * Convert user FOV (arcmin) to fitscut parameters.
 *
 * fitscut: size = pixels, scale = arcsec/pixel
 * FOV_arcsec ≈ pixels * scale
 */
object FinderParams {

    /**
     * Default PS1-like sampling for nice finder charts.
     * You can tune later (0.25" is crisp; 0.5" is lighter).
     */
    private const val DEFAULT_PIXSCALE_ARCSEC = 0.25

    fun fromFovArcmin(
        fovArcmin: Double,
        pixscaleArcsec: Double = DEFAULT_PIXSCALE_ARCSEC,
        minPixels: Int = 256,
        maxPixels: Int = 2048
    ): CutoutSpec {
        val fovArcsec = fovArcmin * 60.0
        var pixels = (fovArcsec / pixscaleArcsec).roundToInt()

        // keep reasonable bounds + prefer even numbers
        pixels = pixels.coerceIn(minPixels, maxPixels)
        if (pixels % 2 != 0) pixels += 1

        return CutoutSpec(pixels = pixels, pixscaleArcsec = pixscaleArcsec)
    }
}