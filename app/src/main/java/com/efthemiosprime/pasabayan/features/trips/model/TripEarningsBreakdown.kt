package com.efthemiosprime.pasabayan.features.trips.model

data class TripEarningsBreakdown(
    val deliveredAmount: Double,
    val deliveredCurrency: String,
    val pendingAmount: Double,
    val pendingCurrency: String,
)
