package com.efthemiosprime.pasabayan.core.network.payments

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Receipt endpoints. */
interface ReceiptApi {

    @GET("receipts")
    suspend fun getReceipts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): Response<PaymentReceiptListResponseJson>

    @GET("receipts/{transactionId}")
    suspend fun getReceipt(
        @Path("transactionId") transactionId: Int,
    ): Response<SinglePaymentReceiptResponseJson>
}
