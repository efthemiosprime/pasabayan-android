package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class DirectBookingData(
    val bookingId: Int,
    val tripId: Int,
    val packageRequestId: Int? = null,
    val agreedPrice: Double,
    val status: String,
)
