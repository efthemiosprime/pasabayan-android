package com.efthemiosprime.pasabayan.core.network.verification

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Verification endpoints per `10-verification.md`.
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

    @Multipart
    @POST("verification/request-premium")
    suspend fun requestPremiumVerification(
        @Part idDocument: MultipartBody.Part,
        @Part idDocumentBack: MultipartBody.Part?,
        @Part selfieWithId: MultipartBody.Part,
        @Part("id_type") idType: RequestBody,
        @Part("id_number") idNumber: RequestBody?,
        @Part("birth_date") birthDate: RequestBody?,
    ): Response<PremiumVerificationResponseJson>

    @GET("verification/premium-status")
    suspend fun premiumStatus(): Response<PremiumVerificationStatusResponseJson>

    @GET("premium-verification/application")
    suspend fun premiumApplication(): Response<PremiumVerificationApplicationResponseJson>
}
