package com.efthemiosprime.pasabayan.core.network.bookings

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Service-match receipt endpoints. Mirrors iOS `BookingsAPIService.uploadReceipt`
 * and `getReceipt`. Distinct from `ReceiptApi` (payments hub `/receipts` —
 * transaction-level history).
 *
 * Upload pre-condition: caller is the carrier on a service match.
 * Read pre-condition: caller is the shipper (iOS gates the in-thread
 * `ShipperServiceReceiptCard` to shipper role).
 */
interface MatchReceiptApi {

    @Multipart
    @POST("services/matches/{matchId}/receipt")
    suspend fun uploadReceipt(
        @Path("matchId") matchId: Int,
        @Part receiptPhoto: MultipartBody.Part,
    ): Response<MatchReceiptUploadResponseJson>

    @GET("services/matches/{matchId}/receipt")
    suspend fun getReceipt(
        @Path("matchId") matchId: Int,
    ): Response<MatchReceiptResponseJson>
}
