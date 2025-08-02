package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * iOS API Models - Missing models to match iOS exactly
 * These models are required for complete iOS compatibility
 */

/**
 * Book Trip Request - iOS: BookTripRequest
 */
@Serializable
data class BookTripRequest(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("package_id")
    val packageId: Int,
    @SerialName("requested_pickup_date")
    val requestedPickupDate: String,
    val notes: String? = null
)

/**
 * Compatibility Response - iOS: CompatibilityResponse
 */
@Serializable
data class CompatibilityResponse(
    val success: Boolean,
    val message: String,
    val data: CompatibilityResult
)

/**
 * Compatibility Result - iOS: CompatibilityResult
 */
@Serializable
data class CompatibilityResult(
    @SerialName("is_compatible")
    val isCompatible: Boolean,
    @SerialName("estimated_price")
    val estimatedPrice: Double,
    @SerialName("available_capacity")
    val availableCapacity: TripCapacity,
    val reasons: List<String>? = null
)

/**
 * Trip Capacity - iOS: TripCapacity
 */
@Serializable
data class TripCapacity(
    @SerialName("weight_kg")
    val weightKg: Double,
    @SerialName("space_liters")
    val spaceLiters: Double
)

/**
 * Health Response - iOS: HealthResponse
 */
@Serializable
data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: String
)

/**
 * Backend Auth Response - iOS: BackendAuthResponse
 */
@Serializable
data class BackendAuthResponse(
    val success: Boolean,
    val message: String,
    val data: BackendAuthData? = null
)

/**
 * Backend Auth Data - iOS: BackendAuthData
 */
@Serializable
data class BackendAuthData(
    val user: BackendUser,
    val token: String,
    @SerialName("token_type")
    val tokenType: String
)

/**
 * Backend User - iOS: BackendUser
 */
@Serializable
data class BackendUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val phone: String? = null,
    @SerialName("phone_verified")
    val phoneVerified: Boolean,
    @SerialName("profile_completed")
    val profileCompleted: Boolean,
    val provider: String,
    val profile: BackendUserProfile? = null,
    @SerialName("is_fully_verified")
    val isFullyVerified: Boolean? = null,
    @SerialName("has_completed_profile")
    val hasCompletedProfile: Boolean? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    // Role-related properties
    @SerialName("user_types")
    val userTypes: List<String>? = null,
    @SerialName("is_active_carrier")
    val isActiveCarrier: Boolean? = null,
    @SerialName("is_active_shipper")
    val isActiveShipper: Boolean? = null,
    val rating: Double? = null,
    @SerialName("total_ratings")
    val totalRatings: Int? = null,
    @SerialName("verification_level")
    val verificationLevel: String? = null
) {
    /**
     * Convert to User model - matching iOS toUser() function
     */
    fun toUser(): User = User(
        id = id,
        name = name,
        email = email,
        avatar = avatar,
        phone = phone,
        phoneVerified = phoneVerified,
        profileCompleted = profileCompleted,
        provider = provider,
        providerId = null,
        emailVerifiedAt = null,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: "",
        userTypes = userTypes ?: listOf("shipper"),
        isActiveCarrier = isActiveCarrier ?: false,
        isActiveShipper = isActiveShipper ?: true,
        rating = rating,
        totalRatings = totalRatings ?: 0,
        verificationLevel = verificationLevel ?: "basic"
    )
}

/**
 * Backend User Profile - iOS: BackendUserProfile
 */
@Serializable
data class BackendUserProfile(
    val id: Int? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("delivery_address")
    val deliveryAddress: String? = null,
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    @SerialName("preferred_contact_method")
    val preferredContactMethod: String? = null,
    @SerialName("additional_info")
    val additionalInfo: Map<String, String>? = null
)

/**
 * Request to Carry Request - iOS: RequestToCarryRequest
 */
@Serializable
data class RequestToCarryRequest(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("proposed_price")
    val proposedPrice: Double,
    val message: String? = null
)

/**
 * Package Accept Request - iOS: PackageAcceptRequest (from APIModels.swift)
 */
@Serializable
data class PackageAcceptRequestIOS(
    @SerialName("agreed_price")
    val agreedPrice: Double
)

/**
 * Package Reject Request - iOS: PackageRejectRequest
 */
@Serializable
data class PackageRejectRequest(
    val reason: String? = null
)

/**
 * Package Accept Response - iOS: PackageAcceptResponse
 */
@Serializable
data class PackageAcceptResponse(
    val message: String,
    val data: DeliveryMatch
)

/**
 * Package Reject Response - iOS: PackageRejectResponse
 */
@Serializable
data class PackageRejectResponse(
    val message: String,
    val data: PackageRejectionResponseData
)

/**
 * Package Rejection Response Data - iOS: PackageRejectionResponse (nested)
 */
@Serializable
data class PackageRejectionResponseData(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("package_id")
    val packageId: Int,
    val reason: String? = null,
    @SerialName("rejected_at")
    val rejectedAt: String
) 