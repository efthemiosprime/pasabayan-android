package com.efthemiosprime.pasabayan.core.network.payments

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Payment methods endpoints. */
interface PaymentMethodsApi {

    @GET("stripe/payment-methods")
    suspend fun getPaymentMethods(): Response<PaymentMethodsResponseJson>

    @POST("stripe/setup-intent")
    suspend fun createSetupIntent(): Response<SetupIntentResponseJson>

    @GET("stripe/default-payment-method")
    suspend fun getDefaultPaymentMethod(): Response<DefaultPaymentMethodResponseJson>

    @PUT("stripe/default-payment-method")
    suspend fun setDefaultPaymentMethod(
        @Body body: Map<String, String>,
    ): Response<SetDefaultPaymentMethodResponseJson>

    @DELETE("stripe/payment-methods/{methodId}")
    suspend fun deletePaymentMethod(
        @Path("methodId") methodId: String,
    ): Response<DeletePaymentMethodResponseJson>
}
