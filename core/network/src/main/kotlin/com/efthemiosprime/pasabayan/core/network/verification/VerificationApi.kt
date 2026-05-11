package com.efthemiosprime.pasabayan.core.network.verification

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Verification endpoints per `10-verification.md`. Phone OTP endpoints surface here; premium ID
 * upload (multipart) lands in slice B.
 */
interface VerificationApi {

    @POST("phone/send-otp")
    suspend fun sendOtp(@Body body: SendOtpRequestJson): Response<OtpResponseJson>

    @POST("phone/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestJson): Response<VerifyOtpResponseJson>

    @POST("phone/resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequestJson): Response<OtpResponseJson>

    @GET("phone/status")
    suspend fun phoneStatus(): Response<PhoneStatusResponseJson>
}
