package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Carrier status data from toggle-status API endpoint
 * Mirrors iOS CarrierStatusData structure
 */
@Serializable
data class CarrierStatusData(
    @SerialName("is_active_carrier")
    val isActiveCarrier: Boolean,
    @SerialName("carrier_status")
    val carrierStatus: String,
    @SerialName("has_carrier_profile")
    val hasCarrierProfile: Boolean = false,
    @SerialName("profile_recommended")
    val profileRecommended: Boolean = false,
    @SerialName("user_types")
    val userTypes: List<String> = emptyList()
)

/**
 * Carrier status response wrapper
 * Mirrors iOS CarrierStatusResponse structure
 */
@Serializable
data class CarrierStatusResponse(
    val success: Boolean = true,
    val message: String,
    val data: CarrierStatusData
)

/**
 * Carrier profile response for checking carrier status
 * Returns 200 if user is carrier, 403 if not
 */
@Serializable
data class CarrierProfileResponse(
    val success: Boolean = true,
    val message: String,
    val data: CarrierProfileData? = null
)

/**
 * Carrier profile data 
 */
@Serializable
data class CarrierProfileData(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: Int? = null,
    @SerialName("business_name")
    val businessName: String? = null,
    @SerialName("business_type")
    val businessType: String? = null,
    @SerialName("vehicle_type")
    val vehicleType: String? = null,
    @SerialName("vehicle_capacity")
    val vehicleCapacity: String? = null,
    @SerialName("license_number")
    val licenseNumber: String? = null,
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    @SerialName("is_active")
    val isActive: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Carrier status enumeration for type safety
 */
enum class CarrierStatus(val value: String) {
    ACTIVE("active"),
    INACTIVE("inactive");
    
    companion object {
        fun fromString(value: String): CarrierStatus {
            return when (value.lowercase()) {
                "active" -> ACTIVE
                "inactive" -> INACTIVE
                else -> INACTIVE
            }
        }
    }
    
    val displayName: String
        get() = when (this) {
            ACTIVE -> "Active"
            INACTIVE -> "Inactive"
        }
} 