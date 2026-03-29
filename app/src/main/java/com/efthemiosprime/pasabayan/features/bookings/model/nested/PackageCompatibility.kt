package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class PackageCompatibility(
    val isCompatible: Boolean,
    val score: Int? = null,
    val routeMatch: String? = null,
    val capacitySufficient: Boolean = false,
    val dateCompatible: Boolean = false,
    val priceCompatible: Boolean = false,
)
