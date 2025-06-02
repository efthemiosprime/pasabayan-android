package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Authentication response model from the backend API
 * Mirrors iOS AuthResponse structure
 */
@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

/**
 * Authentication data nested inside the response
 */
@Serializable
data class AuthData(
    val token: String,
    val user: User,
    @SerialName("token_type")
    val tokenType: String = "Bearer"
)

/**
 * Login request model for backend authentication
 */
@Serializable
data class LoginRequest(
    val provider: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("device_info")
    val deviceInfo: DeviceInfo? = null
)

/**
 * Device information for login requests
 */
@Serializable
data class DeviceInfo(
    val platform: String = "android",
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("app_version")
    val appVersion: String? = null,
    @SerialName("os_version")
    val osVersion: String? = null
)

/**
 * API Error model for error responses
 */
@Serializable
data class APIError(
    override val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null
) : Exception(message) 