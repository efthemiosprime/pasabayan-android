package com.efthemiosprime.pasabayan.features.bookings.model

data class BookingStats(
    val totalBookings: Int,
    val pendingBookings: Int,
    val confirmedBookings: Int,
    val activeBookings: Int,
    val completedBookings: Int,
    val cancelledBookings: Int,
    val totalEarnings: Double,
    val averageRating: Double?,
)
