package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val phone: String? = null,
    val provider: String? = null,
    @SerialName("provider_id") val providerId: String? = null,
    @SerialName("email_verified_at") val emailVerifiedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class UserProfile(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("emergency_contact") val emergencyContact: String? = null,
    @SerialName("emergency_phone") val emergencyPhone: String? = null,
    @SerialName("vehicle_type") val vehicleType: String? = null,
    @SerialName("vehicle_model") val vehicleModel: String? = null,
    @SerialName("license_plate") val licensePlate: String? = null,
    @SerialName("is_driver") val isDriver: Boolean = false,
    @SerialName("driver_status") val driverStatus: String? = null,
    val rating: Double? = null,
    @SerialName("total_deliveries") val totalDeliveries: Int = 0,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
    val profile: UserProfile? = null
)

@Serializable
data class BackendAuthResponse(
    val success: Boolean,
    val message: String,
    val data: BackendAuthData? = null
)

@Serializable
data class BackendAuthData(
    val token: String,
    @SerialName("token_type") val tokenType: String,
    val user: BackendUser
)

@Serializable
data class BackendUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = false,
    @SerialName("profile_completed") val profileCompleted: Boolean = false,
    val provider: String? = null
) {
    fun toUser(): User = User(
        id = id,
        name = name,
        email = email,
        avatar = avatar,
        phone = null,
        provider = provider,
        providerId = null,
        emailVerifiedAt = null,
        createdAt = "",
        updatedAt = ""
    )
}

@Serializable
data class APIResponse(
    val message: String
) 