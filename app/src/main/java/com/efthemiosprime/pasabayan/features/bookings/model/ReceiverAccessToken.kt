package com.efthemiosprime.pasabayan.features.bookings.model

data class ReceiverAccessToken(
    val id: Int,
    val shortCode: String,
    val shortUrl: String,
    val hasPin: Boolean,
    val pin: String?,
    val pinNotice: String?,
    val isActive: Boolean,
    val accessCount: Int,
    val trackingUrl: String,
    val firstAccessedAt: String?,
    val lastAccessedAt: String?,
    val createdAt: String,
)
