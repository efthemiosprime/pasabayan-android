package com.efthemiosprime.pasabayan.core.network.verification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequestJson(
    val phone: String,
)

@Serializable
data class VerifyOtpRequestJson(
    val phone: String,
    @SerialName("otp_code") val otpCode: String,
)

@Serializable
data class ResendOtpRequestJson(
    val phone: String,
)

@Serializable
data class OtpResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: OtpDataJson? = null,
)

@Serializable
data class OtpDataJson(
    val phone: String,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("verification_id") val verificationId: Int? = null,
)

@Serializable
data class VerifyOtpResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: VerifyOtpDataJson? = null,
)

@Serializable
data class VerifyOtpDataJson(
    val phone: String,
    @SerialName("verified_at") val verifiedAt: String? = null,
    val user: VerifiedUserDataJson? = null,
)

@Serializable
data class VerifiedUserDataJson(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = false,
    @SerialName("verification_level") val verificationLevel: String? = null,
)

@Serializable
data class PhoneStatusResponseJson(
    val success: Boolean,
    val data: PhoneStatusDataJson? = null,
)

@Serializable
data class PhoneStatusDataJson(
    val phone: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = false,
    @SerialName("phone_verified_at") val phoneVerifiedAt: String? = null,
    @SerialName("is_fully_verified") val isFullyVerified: Boolean = false,
    @SerialName("pending_verifications") val pendingVerifications: Int = 0,
)
