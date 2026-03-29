package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PickupCodeResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: PickupCodeDataJson? = null,
)

@Serializable
data class PickupCodeDataJson(
    val code: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("generated_at") val generatedAt: String? = null,
)

@Serializable
data class DeliveryCodeResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryCodeDataJson? = null,
)

@Serializable
data class DeliveryCodeDataJson(
    val code: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("generated_at") val generatedAt: String? = null,
)

@Serializable
data class PickupConfirmationResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)

@Serializable
data class DeliveryConfirmationResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)
