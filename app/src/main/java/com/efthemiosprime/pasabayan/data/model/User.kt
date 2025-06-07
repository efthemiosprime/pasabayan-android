package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.efthemiosprime.pasabayan.data.common.ValidationResult
import com.efthemiosprime.pasabayan.data.common.Validation
import com.efthemiosprime.pasabayan.data.common.validate

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
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
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
    
    // MARK: - Functional Update Methods (Immutable)
    
    /**
     * Update user name functionally
     */
    fun updateName(newName: String): User = copy(name = newName)
    
    /**
     * Update user email functionally
     */
    fun updateEmail(newEmail: String): User = copy(email = newEmail)
    
    /**
     * Update phone number functionally
     */
    fun updatePhone(newPhone: String): User = copy(phone = newPhone)
    
    /**
     * Mark phone as verified functionally
     */
    fun markPhoneVerified(): User = copy(phoneVerified = true)
    
    /**
     * Mark profile as completed functionally
     */
    fun markProfileCompleted(): User = copy(profileCompleted = true)
    
    /**
     * Update user types functionally
     */
    fun updateUserTypes(newUserTypes: List<String>): User = copy(userTypes = newUserTypes)
    
    /**
     * Add user type functionally
     */
    fun addUserType(userType: String): User = copy(userTypes = userTypes + userType)
    
    /**
     * Remove user type functionally
     */
    fun removeUserType(userType: String): User = copy(userTypes = userTypes - userType)
    
    /**
     * Update carrier status functionally
     */
    fun updateCarrierStatus(isActive: Boolean): User = copy(isActiveCarrier = isActive)
    
    /**
     * Update shipper status functionally
     */
    fun updateShipperStatus(isActive: Boolean): User = copy(isActiveShipper = isActive)
    
    /**
     * Update rating functionally
     */
    fun updateRating(newRating: Double, newTotalRatings: Int): User = 
        copy(rating = newRating, totalRatings = newTotalRatings)
    
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
            createdAt = null,
            updatedAt = null,
            userTypes = emptyList(),
            isActiveCarrier = false,
            isActiveShipper = false,
            rating = null,
            totalRatings = 0,
            verificationLevel = "unverified"
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
 * User role enumeration
 */
enum class UserRole {
    SHIPPER,
    CARRIER
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