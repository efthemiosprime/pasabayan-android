package com.efthemiosprime.pasabayan.core.network.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderLoginRequestJson(
    @SerialName("access_token") val accessToken: String,
)

@Serializable
data class BackendAuthResponseJson(
    val success: Boolean,
    val message: String = "",
    val data: BackendAuthDataJson? = null,
)

@Serializable
data class BackendAuthDataJson(
    val user: BackendUserJson,
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
)

@Serializable
data class BackendUserJson(
    val id: Long,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val phone: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean? = null,
    @SerialName("profile_completed") val profileCompleted: Boolean? = null,
    val provider: String? = null,
    @SerialName("user_types") val userTypes: List<String>? = null,
    @SerialName("is_active_carrier") val isActiveCarrier: Boolean? = null,
    @SerialName("is_active_shipper") val isActiveShipper: Boolean? = null,
)

@Serializable
data class UserResponseJson(
    val success: Boolean,
    val data: UserDataJson,
)

@Serializable
data class UserDataJson(
    val user: BackendUserJson,
)

@Serializable
data class LogoutResponseJson(
    val success: Boolean? = null,
    val message: String? = null,
)
