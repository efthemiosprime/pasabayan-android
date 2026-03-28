package com.efthemiosprime.pasabayan.features.trips.model

data class PendingTripRequest(
    val id: Int,
    val shipperId: Int,
    val shipperName: String?,
    val shipperAvatar: String?,
    val packageId: Int?,
    val packageDescription: String?,
    val proposedPrice: Double?,
    val message: String?,
    val createdAt: String?,
)
