package com.yangsheng.astrocal.util

data class GaiaStar(
    val raDeg: Double,
    val decDeg: Double,
    val gmag: Double? = null,
    val plxMas: Double? = null
)