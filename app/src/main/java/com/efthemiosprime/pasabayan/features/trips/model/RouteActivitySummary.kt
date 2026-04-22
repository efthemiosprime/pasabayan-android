package com.efthemiosprime.pasabayan.features.trips.model

data class RouteActivitySummary(
    val totalTrips: Int,
    val activeTrips: Int,
    val completedTrips: Int,
    val totalEarnings: Double?,
    val currency: String?,
)
