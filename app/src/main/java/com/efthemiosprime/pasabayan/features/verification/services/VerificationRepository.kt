package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PremiumVerificationSubmissionDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson

interface VerificationRepository {

    suspend fun sendOtp(phone: String): Result<OtpDataJson?>

    suspend fun verifyOtp(phone: String, otpCode: String): Result<VerifyOtpDataJson?>

    suspend fun resendOtp(phone: String): Result<OtpDataJson?>

    suspend fun fetchPhoneStatus(): Result<PhoneStatusDataJson>

    /**
     * Multipart submission for premium verification per `10-verification.md` § premium upload.
     * Callers pre-compress images (the existing `ImageCompressor` keeps avatars under the 5 MB
     * backend ceiling and the same constraints apply here).
     */
    suspend fun submitPremiumVerification(
        idType: String,
        idDocumentFront: ByteArray,
        idDocumentBack: ByteArray?,
        selfieWithId: ByteArray,
        idNumber: String? = null,
        birthDate: String? = null,
        mimeType: String = "image/jpeg",
    ): Result<PremiumVerificationSubmissionDataJson?>

    suspend fun fetchPremiumStatus(): Result<PremiumVerificationStatusDataJson>
}
