package com.yangsheng.astrocal.util

data class WcsHeader(
    val naxis1: Int,
    val naxis2: Int,
    val crpix1: Double,
    val crpix2: Double,
    val crval1: Double,
    val crval2: Double,
    val cd11: Double,
    val cd12: Double,
    val cd21: Double,
    val cd22: Double
)