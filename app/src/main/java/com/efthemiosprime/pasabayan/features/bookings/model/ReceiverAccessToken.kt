package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Receiver access token — domain model for the shipper "Share with receiver" flow.
 * Mirrors iOS `ReceiverAccessToken` (ReceiverAccessModels.swift). `accessCount` and
 * `trackingUrl` are nullable because newly created tokens may omit them until the
 * receiver opens the link.
 */
data class ReceiverAccessToken(
    val id: Int,
    val shortCode: String,
    val shortUrl: String,
    val hasPin: Boolean,
    val pin: String?,
    val pinNotice: String?,
    val isActive: Boolean,
    val accessCount: Int?,
    val trackingUrl: String?,
    val firstAccessedAt: String?,
    val lastAccessedAt: String?,
    val createdAt: String,
)
