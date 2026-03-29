package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class CurrentLocationData(
    val latitude: Double,
    val longitude: Double,
    val lastUpdatedAt: String? = null,
    val isStale: Boolean = false,
)
