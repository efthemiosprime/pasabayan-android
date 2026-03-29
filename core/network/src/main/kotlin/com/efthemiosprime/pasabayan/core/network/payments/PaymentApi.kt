package com.efthemiosprime.pasabayan.core.network.payments

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Payment endpoints per `android-spec/06-payments-stripe.md`. */
interface PaymentApi {

    @POST("payments")
    suspend fun createPayment(@Body body: CreatePaymentRequestJson): Response<CreatePaymentResponseJson>

    @GET("payments")
    suspend fun listTransactions(@Query("role") role: String? = null): Response<TransactionListResponseJson>

    @GET("payments/{id}")
    suspend fun getTransaction(@Path("id") id: Int): Response<TransactionResponseJson>

    @POST("payments/{id}/capture")
    suspend fun captureTransaction(@Path("id") id: Int): Response<PaymentActionResponseJson>

    @POST("payments/confirm-capture")
    suspend fun confirmCapture(@Body body: ConfirmCaptureRequestJson): Response<PaymentActionResponseJson>

    @POST("payments/{id}/release")
    suspend fun releaseTransaction(@Path("id") id: Int): Response<PaymentActionResponseJson>

    @POST("payments/{transactionId}/refund")
    suspend fun requestRefund(
        @Path("transactionId") transactionId: Int,
        @Body body: RefundRequestBodyJson,
    ): Response<RefundStatusResponseJson>

    @GET("payments/{transactionId}/refund-status")
    suspend fun getRefundStatus(@Path("transactionId") transactionId: Int): Response<RefundStatusResponseJson>

    @POST("payments/{id}/cancel")
    suspend fun cancelTransaction(
        @Path("id") id: Int,
        @Body body: CancelRequestJson = CancelRequestJson(),
    ): Response<PaymentActionResponseJson>

    @POST("payments/{transactionId}/tip")
    suspend fun addTip(
        @Path("transactionId") transactionId: Int,
        @Body body: TipRequestJson,
    ): Response<TipResponseJson>
}

/** Stripe config endpoint. */
interface StripeConfigApi {

    @GET("payments/config")
    suspend fun getConfig(): Response<StripeConfigResponseJson>
}
