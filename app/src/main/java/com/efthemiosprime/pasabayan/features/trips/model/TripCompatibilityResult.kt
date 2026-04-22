package com.efthemiosprime.pasabayan.features.trips.model

data class TripCompatibilityResult(
    val routeCompatible: Boolean,
    val capacitySufficient: Boolean,
    val dateCompatible: Boolean,
    val priceCompatible: Boolean,
) {
    val isCompatible: Boolean
        get() = routeCompatible && capacitySufficient && dateCompatible && priceCompatible
}
