package com.efthemiosprime.pasabayan.core.network.profile

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * User profile, carrier, consent, and stats for profile tab + onboarding.
 * Base path prefix `api` is in [com.efthemiosprime.pasabayan.core.network.di.NetworkModule].
 */
interface ProfileApi {

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponseJson>

    @PUT("profile")
    suspend fun putProfile(@Body body: UpdateProfileRequestJson): Response<ProfileResponseJson>

    @Multipart
    @POST("profile")
    suspend fun postProfileMultipart(
        @Part profilePicture: okhttp3.MultipartBody.Part?,
        @Part("full_name") fullName: RequestBody?,
        @Part("delivery_address") deliveryAddress: RequestBody?,
        @Part("preferred_contact_method") preferredContactMethod: RequestBody?,
        @Part("additional_info") additionalInfo: RequestBody?,
    ): Response<ProfileResponseJson>

    @DELETE("profile/picture")
    suspend fun deleteProfilePicture(): Response<SimpleApiMessageResponseJson>

    @POST("profile/request-deletion")
    suspend fun requestAccountDeletion(
        @Body body: AccountDeletionRequestJson,
    ): Response<AccountDeletionResponseJson>

    @GET("profile/disclaimer-acknowledgments")
    suspend fun getDisclaimerAcknowledgments(): Response<DisclaimerAcknowledgmentsListResponseJson>

    @POST("profile/disclaimer-acknowledgments")
    suspend fun postDisclaimerAcknowledgment(
        @Body body: DisclaimerAcknowledgmentRequestJson,
    ): Response<DisclaimerAcknowledgmentResponseJson>

    @GET("profile/consent-preferences")
    suspend fun getConsentPreferences(): Response<ConsentPreferencesResponseJson>

    @PUT("profile/consent-preferences")
    suspend fun putConsentPreferences(
        @Body body: ConsentPreferencesUpdateJson,
    ): Response<ConsentPreferencesResponseJson>

    @GET("profile/export-data")
    @Streaming
    suspend fun exportUserData(): Response<ResponseBody>

    @GET("carrier/profile")
    suspend fun getCarrierProfile(): Response<CarrierProfileResponseJson>

    @POST("carrier/profile")
    suspend fun postCarrierProfile(
        @Body body: CreateCarrierProfileRequestJson,
    ): Response<CarrierProfileResponseJson>

    @PUT("carrier/profile")
    suspend fun putCarrierProfile(
        @Body body: CreateCarrierProfileRequestJson,
    ): Response<CarrierProfileResponseJson>

    @GET("carrier/stats")
    suspend fun getCarrierStats(): Response<CarrierStatsResponseJson>

    @POST("carrier/enable")
    suspend fun postCarrierEnable(): Response<CarrierEnableResponseJson>

    @POST("carrier/toggle-status")
    suspend fun postCarrierToggleStatus(): Response<CarrierStatusResponseJson>

    @GET("user/stats")
    suspend fun getUserStats(): Response<UserStatsResponseJson>

    /**
     * Single source-of-truth attention payload driving every badge surface
     * (tab-bar Profile badge, dashboard bell, drawer sections, per-row badges).
     * Server caches per (user, role) for ~30 s — do not add a client cache.
     * Omit [role] for the dual-role union; pass `"carrier"` or `"shipper"` for
     * single-role sub-counts. iOS parity:
     * `Pasabayan/Features/Profile/Services/ProfileAPIService.swift` (getBadgeSummary).
     */
    @GET("me/badge-summary")
    suspend fun getBadgeSummary(@Query("role") role: String?): Response<BadgeSummaryJson>
}
