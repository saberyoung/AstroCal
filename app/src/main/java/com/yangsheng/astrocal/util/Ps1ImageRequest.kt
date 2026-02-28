package com.yangsheng.astrocal.util

data class Ps1ImageRequest(
    val raDeg: Double,
    val decDeg: Double,
    val fovArcmin: Double,
    val filter: String
)