package com.efthemiosprime.pasabayan.core.network.payments

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

/** Stripe Connect endpoints. */
interface StripeConnectApi {

    @POST("stripe/connect/onboard")
    suspend fun startOnboarding(): Response<StripeOnboardingResponseJson>

    @GET("stripe/connect/status")
    suspend fun getStatus(): Response<StripeStatusResponseJson>

    @GET("stripe/connect/dashboard")
    suspend fun getDashboard(): Response<StripeDashboardResponseJson>
}
