package com.efthemiosprime.pasabayan.features.bookings.model

data class DeliveryCodeData(
    val verificationCode: String,
    val expiresAt: String,
    val matchId: Int,
    val shipperId: Int,
    val carrierId: Int,
    val sentToChat: Boolean = false,
    val packageOrService: String? = null,
)
