package com.efthemiosprime.pasabayan.features.verification.services

import com.efthemiosprime.pasabayan.core.network.verification.OtpDataJson
import com.efthemiosprime.pasabayan.core.network.verification.PhoneStatusDataJson
import com.efthemiosprime.pasabayan.core.network.verification.VerifyOtpDataJson

interface VerificationRepository {

    suspend fun sendOtp(phone: String): Result<OtpDataJson?>

    suspend fun verifyOtp(phone: String, otpCode: String): Result<VerifyOtpDataJson?>

    suspend fun resendOtp(phone: String): Result<OtpDataJson?>

    suspend fun fetchPhoneStatus(): Result<PhoneStatusDataJson>
}
