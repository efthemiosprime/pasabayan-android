package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Receiver-access DTOs — `/matches/{id}/receiver-access` family.
 * Mirrors iOS `ReceiverAccessModels.swift`.
 */
@Serializable
data class ReceiverAccessTokenJson(
    val id: Int,
    @SerialName("short_code") val shortCode: String,
    @SerialName("short_url") val shortUrl: String,
    @SerialName("has_pin") val hasPin: Boolean = false,
    val pin: String? = null,
    @SerialName("pin_notice") val pinNotice: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("access_count") val accessCount: Int? = null,
    @SerialName("tracking_url") val trackingUrl: String? = null,
    @SerialName("first_accessed_at") val firstAccessedAt: String? = null,
    @SerialName("last_accessed_at") val lastAccessedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CreateReceiverAccessRequestJson(
    @SerialName("generate_pin") val generatePin: Boolean = true,
    @SerialName("receiver_name") val receiverName: String? = null,
)

@Serializable
data class ReceiverAccessListResponseJson(
    val success: Boolean = false,
    val data: List<ReceiverAccessTokenJson> = emptyList(),
)

@Serializable
data class CreateReceiverAccessResponseJson(
    val success: Boolean = false,
    val data: ReceiverAccessTokenJson? = null,
)

@Serializable
data class RevokeReceiverAccessResponseJson(
    val success: Boolean = false,
    val message: String = "",
)
