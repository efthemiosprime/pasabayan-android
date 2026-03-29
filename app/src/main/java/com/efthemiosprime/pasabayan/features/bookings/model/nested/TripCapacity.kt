package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class TripCapacity(
    val availableWeightKg: Double,
    val availableSpaceLiters: Double? = null,
    val totalWeightKg: Double? = null,
)
