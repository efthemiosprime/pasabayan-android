package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.efthemiosprime.pasabayan.data.common.ValidationResult
import com.efthemiosprime.pasabayan.data.common.Validation
import com.efthemiosprime.pasabayan.data.common.validate

/**
 * User model representing the authenticated user
 * Exactly matching iOS User model structure and behavior
 * Follows functional programming principles with immutable data and pure functions
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
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
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
    val verificationLevel: String = "basic"
) {
    
    // MARK: - Pure Computed Properties (exactly matching iOS)
    
    /**
     * Display name derived from name or email (iOS: displayName)
     */
    val displayName: String
        get() = if (name.isEmpty()) extractNameFromEmail(email) else name
    
    /**
     * Get user initials for avatar display (iOS: initials)
     */
    val initials: String
        get() = computeInitials(name)
    
    /**
     * Phone verification status (iOS: isPhoneVerified)
     */
    val isPhoneVerified: Boolean
        get() = phoneVerified && phone != null && phone.isNotEmpty()
    
    /**
     * Profile completion status (iOS: isProfileComplete)
     */
    val isProfileComplete: Boolean
        get() = profileCompleted
    
    /**
     * Available user roles (iOS: availableRoles)
     */
    val availableRoles: List<UserRole>
        get() = userTypes.mapNotNull { UserRole.fromString(it) }
    
    /**
     * Carrier role status (iOS: isCarrier)
     */
    val isCarrier: Boolean
        get() = userTypes.contains("carrier")
    
    /**
     * Shipper role status (iOS: isShipper)
     */
    val isShipper: Boolean
        get() = userTypes.contains("shipper")
    
    /**
     * Multi-role capability (iOS: hasBothRoles)
     */
    val hasBothRoles: Boolean
        get() = isCarrier && isShipper
    
    /**
     * Formatted rating display (iOS: displayRating)
     */
    val displayRating: String
        get() = formatRating(rating, totalRatings)
    
    /**
     * User verification level enum (iOS: verificationLevelEnum)
     */
    val verificationLevelEnum: VerificationLevel
        get() = VerificationLevel.fromString(verificationLevel)
    
    /**
     * Account age in days (iOS: accountAgeInDays)
     */
    val accountAgeInDays: Int
        get() = calculateAccountAge(createdAt)
    
    // MARK: - Functional Update Methods (exactly matching iOS)
    
    /**
     * Create a new User instance with updated name (iOS: updatingName)
     */
    fun updatingName(newName: String): User = copy(
        name = newName,
        updatedAt = getCurrentTimestamp()
    )
    
    /**
     * Create a new User instance with updated phone verification (iOS: updatingPhoneVerification)
     */
    fun updatingPhoneVerification(phone: String, verified: Boolean): User = copy(
        phone = phone,
        phoneVerified = verified,
        updatedAt = getCurrentTimestamp()
    )
    
    /**
     * Create a new User instance with updated profile completion (iOS: updatingProfileCompletion)
     */
    fun updatingProfileCompletion(isCompleted: Boolean): User = copy(
        profileCompleted = isCompleted,
        updatedAt = getCurrentTimestamp()
    )
    
    /**
     * Create a new User instance with updated roles (iOS: updatingRoles)
     */
    fun updatingRoles(carrier: Boolean, shipper: Boolean): User {
        val newUserTypes = mutableListOf<String>()
        if (shipper) newUserTypes.add("shipper")
        if (carrier) newUserTypes.add("carrier")
        
        return copy(
            userTypes = newUserTypes,
            isActiveCarrier = carrier,
            isActiveShipper = shipper,
            updatedAt = getCurrentTimestamp()
        )
    }
    
    /**
     * Create a new User instance with updated rating (iOS: updatingRating)
     */
    fun updatingRating(newRating: Double, newTotalRatings: Int): User = copy(
        rating = newRating,
        totalRatings = newTotalRatings,
        updatedAt = getCurrentTimestamp()
    )
    
    // MARK: - Validation Methods (Pure Functions)
    
    /**
     * Validate user data
     */
    fun validate(): ValidationResult = validate {
        validate(Validation.validateName(name))
        validate(Validation.validateEmail(email))
        phone?.let { validate(Validation.validatePhoneNumber(it)) }
    }
    
    /**
     * Validate for profile completion
     */
    fun validateForProfileCompletion(): ValidationResult = validate {
        validate(Validation.validateName(name))
        validate(Validation.validateEmail(email))
        validate(Validation.validateRequired(phone ?: "", "Phone number"))
        phone?.let { validate(Validation.validatePhoneNumber(it)) }
    }
    
    companion object {
        /**
         * Create empty User for initial state
         */
        fun empty(): User = User(
            id = 0,
            name = "",
            email = "",
            avatar = null,
            phone = null,
            phoneVerified = false,
            profileCompleted = false,
            provider = null,
            providerId = null,
            emailVerifiedAt = null,
            createdAt = "",
            updatedAt = "",
            userTypes = emptyList(),
            isActiveCarrier = false,
            isActiveShipper = false,
            rating = null,
            totalRatings = 0,
            verificationLevel = "basic"
        )
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
) {
    // MARK: - Computed Properties
    
    val isComplete: Boolean
        get() = address != null && 
                dateOfBirth != null && 
                gender != null && 
                emergencyContactName != null && 
                emergencyContactPhone != null
                
    // MARK: - Functional Update Methods
    
    fun updateProfilePicture(newPicture: String?): UserProfile = copy(profilePicture = newPicture)
    
    fun updateAddress(newAddress: String?): UserProfile = copy(address = newAddress)
    
    fun updateDateOfBirth(newDateOfBirth: String?): UserProfile = copy(dateOfBirth = newDateOfBirth)
    
    fun updateGender(newGender: String?): UserProfile = copy(gender = newGender)
    
    fun updateEmergencyContact(name: String?, phone: String?): UserProfile = 
        copy(emergencyContactName = name, emergencyContactPhone = phone)
        
    fun updatePreferredLanguage(newLanguage: String?): UserProfile = copy(preferredLanguage = newLanguage)
    
    fun updateNotificationSettings(newSettings: NotificationSettings?): UserProfile = 
        copy(notificationSettings = newSettings)
    
    // MARK: - Validation
    
    fun validate(): ValidationResult = validate {
        address?.let { validate(Validation.validateRequired(it, "Address")) }
        emergencyContactName?.let { validate(Validation.validateName(it)) }
        emergencyContactPhone?.let { validate(Validation.validatePhoneNumber(it)) }
        dateOfBirth?.let { validate(Validation.validateISODate(it, "Date of Birth")) }
    }
}

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
) {
    // MARK: - Functional Update Methods
    
    fun toggleEmailNotifications(): NotificationSettings = copy(emailNotifications = !emailNotifications)
    
    fun togglePushNotifications(): NotificationSettings = copy(pushNotifications = !pushNotifications)
    
    fun toggleSmsNotifications(): NotificationSettings = copy(smsNotifications = !smsNotifications)
    
    fun toggleMarketingEmails(): NotificationSettings = copy(marketingEmails = !marketingEmails)
    
    fun updateEmailNotifications(enabled: Boolean): NotificationSettings = copy(emailNotifications = enabled)
    
    fun updatePushNotifications(enabled: Boolean): NotificationSettings = copy(pushNotifications = enabled)
    
    fun updateSmsNotifications(enabled: Boolean): NotificationSettings = copy(smsNotifications = enabled)
    
    fun updateMarketingEmails(enabled: Boolean): NotificationSettings = copy(marketingEmails = enabled)
    
    companion object {
        fun allEnabled(): NotificationSettings = NotificationSettings(
            emailNotifications = true,
            pushNotifications = true,
            smsNotifications = true,
            marketingEmails = true
        )
        
        fun allDisabled(): NotificationSettings = NotificationSettings(
            emailNotifications = false,
            pushNotifications = false,
            smsNotifications = false,
            marketingEmails = false
        )
        
        fun defaultSettings(): NotificationSettings = NotificationSettings()
    }
}

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
 * OTP verification data
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
 * User role enumeration - exactly matching iOS UserRole enum
 */
enum class UserRole(val rawValue: String) {
    SHIPPER("shipper"),
    CARRIER("carrier");
    
    val displayName: String
        get() = when (this) {
            SHIPPER -> "Shipper"
            CARRIER -> "Carrier"
        }
    
    val icon: String
        get() = when (this) {
            SHIPPER -> "shop"
            CARRIER -> "courier"
        }
    
    val description: String
        get() = when (this) {
            SHIPPER -> "Send packages with Carriers"
            CARRIER -> "Deliver packages while traveling"
        }
    
    val capabilities: List<String>
        get() = when (this) {
            SHIPPER -> listOf("Create package requests", "Track deliveries", "Rate carriers")
            CARRIER -> listOf("Create trips", "Accept packages", "Earn money")
        }
    
    companion object {
        fun fromString(value: String): UserRole? {
            return values().find { it.rawValue == value }
        }
    }
}

/**
 * Verification Level Enum - exactly matching iOS VerificationLevel enum
 */
enum class VerificationLevel(val rawValue: String) {
    BASIC("basic"),
    VERIFIED("verified"),
    PREMIUM("premium");
    
    val displayName: String
        get() = when (this) {
            BASIC -> "Basic"
            VERIFIED -> "Verified"
            PREMIUM -> "Premium"
        }
    
    val trustScore: Int
        get() = when (this) {
            BASIC -> 1
            VERIFIED -> 3
            PREMIUM -> 5
        }
    
    companion object {
        fun fromString(value: String): VerificationLevel {
            return values().find { it.rawValue == value } ?: BASIC
        }
    }
}

/**
 * User data response for getCurrentUser API
 * This API only returns user data, no token
 */
@Serializable
data class UserDataResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: UserData
)

/**
 * User data wrapper for getCurrentUser response
 */
@Serializable
data class UserData(
    val user: User
)

// MARK: - Pure Helper Functions (exactly matching iOS)

/**
 * Extract name from email address
 */
private fun extractNameFromEmail(email: String): String {
    return email.substringBefore("@").ifEmpty { "User" }
}

/**
 * Compute user initials from full name
 */
private fun computeInitials(name: String): String {
    return name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}

/**
 * Format rating for display
 */
private fun formatRating(rating: Double?, totalRatings: Int): String {
    return if (rating != null && totalRatings > 0) {
        String.format("%.1f ⭐ (%d reviews)", rating, totalRatings)
    } else {
        "No ratings yet"
    }
}

/**
 * Calculate account age in days
 */
private fun calculateAccountAge(dateString: String): Int {
    if (dateString.isEmpty()) return 0
    
    return try {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
        val createdDate = formatter.parse(dateString) ?: return 0
        val diffInMs = System.currentTimeMillis() - createdDate.time
        (diffInMs / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}

/**
 * Get current timestamp in ISO format
 */
private fun getCurrentTimestamp(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return formatter.format(java.util.Date())
} 