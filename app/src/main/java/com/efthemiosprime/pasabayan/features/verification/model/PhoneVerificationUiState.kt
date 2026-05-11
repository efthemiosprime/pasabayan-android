package com.efthemiosprime.pasabayan.features.verification.model

data class PhoneVerificationUiState(
    val phoneNumber: String = "",
    val countryCode: String = "+1",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isVerified: Boolean = false,
    val canResend: Boolean = false,
    val remainingResendSeconds: Int = RESEND_COOLDOWN_SECONDS,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    /** Backend regex: `^\+?[1-9]\d{1,14}$`. Only 10-digit phones formatted E.164 are accepted. */
    val isPhoneValid: Boolean
        get() = phoneNumber.filter { it.isDigit() }.length == 10

    val isOtpValid: Boolean
        get() = otpCode.length == OTP_LENGTH && otpCode.all { it.isDigit() }

    val formattedE164Phone: String
        get() {
            val digits = phoneNumber.filter { it.isDigit() }
            if (digits.isEmpty()) return ""
            val cleanedCountry = countryCode.removePrefix("+")
            if (digits.startsWith(cleanedCountry) && digits.length >= 11) {
                return "+$digits"
            }
            return "+$cleanedCountry$digits"
        }

    companion object {
        const val OTP_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}
