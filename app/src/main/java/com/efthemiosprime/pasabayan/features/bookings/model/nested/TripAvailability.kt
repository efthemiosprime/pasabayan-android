package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class TripAvailability(
    val isAvailable: Boolean,
    val reason: String? = null,
)
