package com.efthemiosprime.pasabayan.features.trips.model

data class PopularRoute(
    val originCity: String,
    val destinationCity: String,
    val packageCount: Int,
    val averagePrice: Double?,
)
