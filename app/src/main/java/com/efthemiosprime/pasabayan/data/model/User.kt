package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User model representing the authenticated user
 * Mirrors iOS User model structure
 */
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val phone: String? = null,
    @SerialName("phone_verified")
    val phoneVerified: Boolean = false,
    @SerialName("profile_completed")
    val profileCompleted: Boolean = false,
    val provider: String? = null,
    @SerialName("provider_id")
    val providerId: String? = null,
    @SerialName("email_verified_at")
    val emailVerifiedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("user_types")
    val userTypes: List<String> = emptyList(),
    @SerialName("is_active_carrier")
    val isActiveCarrier: Boolean = false,
    @SerialName("is_active_shipper")
    val isActiveShipper: Boolean = false,
    val rating: Double? = null,
    @SerialName("total_ratings")
    val totalRatings: Int = 0,
    @SerialName("verification_level")
    val verificationLevel: String = "unverified"
) {
    
    /**
     * Get user initials for avatar display
     */
    val initials: String
        get() = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    
    /**
     * Check if phone is verified
     */
    val isPhoneVerified: Boolean
        get() = phoneVerified
    
    /**
     * Check if profile is complete
     */
    val isProfileComplete: Boolean
        get() = profileCompleted
    
    /**
     * Check if user has carrier role
     */
    val isCarrier: Boolean
        get() = userTypes.contains("carrier")
    
    /**
     * Check if user has shipper role
     */
    val isShipper: Boolean
        get() = userTypes.contains("shipper")
    
    /**
     * Check if user has both roles
     */
    val hasBothRoles: Boolean
        get() = isCarrier && isShipper
    
    /**
     * Get available user roles
     */
    val availableRoles: List<UserRole>
        get() = buildList {
            if (isShipper) add(UserRole.SHIPPER)
            if (isCarrier) add(UserRole.CARRIER)
        }
    
    /**
     * Get formatted rating display string
     */
    val displayRating: String
        get() = if (rating != null && totalRatings > 0) {
            "%.1f ⭐ (%d reviews)".format(rating, totalRatings)
        } else {
            "0"
        }
}

/**
 * User profile model for extended user information
 */
@Serializable
data class UserProfile(
    val id: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    val address: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("emergency_contact_name")
    val emergencyContactName: String? = null,
    @SerialName("emergency_contact_phone")
    val emergencyContactPhone: String? = null,
    @SerialName("preferred_language")
    val preferredLanguage: String? = null,
    @SerialName("notification_settings")
    val notificationSettings: NotificationSettings? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

/**
 * Notification settings for the user
 */
@Serializable
data class NotificationSettings(
    @SerialName("email_notifications")
    val emailNotifications: Boolean = true,
    @SerialName("push_notifications")
    val pushNotifications: Boolean = true,
    @SerialName("sms_notifications")
    val smsNotifications: Boolean = false,
    @SerialName("marketing_emails")
    val marketingEmails: Boolean = false
)

/**
 * Request model for updating user profile
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    val address: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("emergency_contact_name")
    val emergencyContactName: String? = null,
    @SerialName("emergency_contact_phone")
    val emergencyContactPhone: String? = null,
    @SerialName("preferred_language")
    val preferredLanguage: String? = null,
    @SerialName("notification_settings")
    val notificationSettings: NotificationSettings? = null
)

/**
 * Phone verification request model
 */
@Serializable
data class PhoneVerificationRequest(
    @SerialName("phone_number")
    val phoneNumber: String
)

/**
 * Phone verification response model
 */
@Serializable
data class PhoneVerificationResponse(
    val success: Boolean,
    val message: String,
    val data: PhoneVerificationData? = null
)

/**
 * Phone verification data
 */
@Serializable
data class PhoneVerificationData(
    @SerialName("verification_id")
    val verificationId: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null
)

/**
 * OTP verification request model
 */
@Serializable
data class OTPVerificationRequest(
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("otp_code")
    val otpCode: String
)

/**
 * Phone verification status model
 */
@Serializable
data class PhoneVerificationStatus(
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("verified_at")
    val verifiedAt: String? = null
)

/**
 * User role enumeration
 */
enum class UserRole {
    SHIPPER,
    CARRIER
} 