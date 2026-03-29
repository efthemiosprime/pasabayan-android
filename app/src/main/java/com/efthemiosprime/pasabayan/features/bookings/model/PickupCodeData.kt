package com.efthemiosprime.pasabayan.features.bookings.model

data class PickupCodeData(
    val confirmationCode: String,
    val expiresAt: String,
    val matchId: Int,
    val shipperId: Int,
    val carrierId: Int,
    val sentToChat: Boolean = false,
)
