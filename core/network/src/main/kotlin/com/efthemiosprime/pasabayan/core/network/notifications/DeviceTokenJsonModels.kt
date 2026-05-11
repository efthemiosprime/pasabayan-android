package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for `POST /device-tokens`. */
@Serializable
data class DeviceTokenRequestJson(
    val token: String,
    val platform: String = "android",
    @SerialName("app_version") val appVersion: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("device_name") val deviceName: String? = null,
)

@Serializable
data class DeviceTokenResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: DeviceTokenDataJson? = null,
    /** Present on 403 consent errors. */
    @SerialName("consent_required") val consentRequired: String? = null,
)

@Serializable
data class DeviceTokenDataJson(
    @SerialName("device_token") val deviceToken: DeviceTokenInfoJson? = null,
)

@Serializable
data class DeviceTokenInfoJson(
    val id: Int = 0,
    val platform: String = "",
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)

/** Generic success envelope used by DELETE /device-tokens and POST /device-tokens/test. */
@Serializable
data class DeviceTokenSimpleResponseJson(
    val success: Boolean = false,
    val message: String? = null,
)
