package com.efthemiosprime.pasabayan.features.trips.model

data class TripEarningsBreakdown(
    val deliveredAmount: Double,
    val deliveredCurrency: String,
    /** Number of packages already delivered (iOS-parity, `trip_earnings_breakdown.delivered_count`). */
    val deliveredCount: Int = 0,
    val pendingAmount: Double,
    val pendingCurrency: String,
    /** Number of packages awaiting completion. */
    val pendingCount: Int = 0,
)
