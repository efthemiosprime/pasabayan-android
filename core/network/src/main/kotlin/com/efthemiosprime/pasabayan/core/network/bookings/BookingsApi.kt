package com.efthemiosprime.pasabayan.core.network.bookings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Bookings & matches endpoints per `android-spec/05-bookings-matches.md`. */
interface BookingsApi {

    // -- Match listing --

    @GET("matches")
    suspend fun getMatches(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
    ): Response<MatchListResponseJson>

    @GET("matches/pending-requests")
    suspend fun getPendingRequests(): Response<MatchListResponseJson>

    @GET("matches/{matchId}")
    suspend fun getMatch(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    // -- Match creation & status transitions --

    @POST("matches")
    suspend fun createMatch(@Body body: MatchCreationRequestJson): Response<MatchResponseJson>

    @PUT("matches/{matchId}/confirm")
    suspend fun confirmMatch(@Path("matchId") matchId: Int): Response<MatchConfirmResponseJson>

    @PUT("matches/{matchId}/pickup")
    suspend fun markPickedUp(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    @PUT("matches/{matchId}/transit")
    suspend fun markInTransit(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    @PUT("matches/{matchId}/deliver")
    suspend fun markDelivered(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    @DELETE("matches/{matchId}")
    suspend fun cancelMatch(@Path("matchId") matchId: Int): Response<CancelMatchResponseJson>

    // -- Code confirmation --

    @PUT("matches/{matchId}/confirm-pickup/{code}")
    suspend fun confirmPickupWithCode(
        @Path("matchId") matchId: Int,
        @Path("code") code: String,
    ): Response<PickupConfirmationResponseJson>

    @PUT("matches/{matchId}/delivery/{code}")
    suspend fun confirmDeliveryWithCode(
        @Path("matchId") matchId: Int,
        @Path("code") code: String,
    ): Response<DeliveryConfirmationResponseJson>

    // -- Accept/decline flows --

    @PUT("matches/{matchId}/accept-carrier-request")
    suspend fun shipperAcceptCarrierRequest(
        @Path("matchId") matchId: Int,
        @Body body: AcceptMatchRequestJson = AcceptMatchRequestJson(),
    ): Response<MatchResponseJson>

    @POST("matches/{matchId}/decline")
    suspend fun shipperDecline(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    @PUT("matches/{matchId}/accept-shipper-request")
    suspend fun carrierAcceptShipperRequest(
        @Path("matchId") matchId: Int,
        @Body body: AcceptMatchRequestJson = AcceptMatchRequestJson(),
    ): Response<MatchResponseJson>

    @PUT("matches/{matchId}/decline-shipper-request")
    suspend fun carrierDeclineShipperRequest(@Path("matchId") matchId: Int): Response<MatchResponseJson>

    // -- Code generation --

    @POST("matches/{matchId}/generate-pickup-code")
    suspend fun generatePickupCode(@Path("matchId") matchId: Int): Response<PickupCodeResponseJson>

    @POST("matches/{matchId}/generate-delivery-code")
    suspend fun generateDeliveryCode(@Path("matchId") matchId: Int): Response<DeliveryCodeResponseJson>

    // -- Carrier ↔ Package requests --

    @POST("trips/{tripId}/packages/{packageId}/request")
    suspend fun carrierRequestPackage(
        @Path("tripId") tripId: Int,
        @Path("packageId") packageId: Int,
        @Body body: CarrierCounterOfferRequestJson,
    ): Response<CarrierRequestResponseJson>

    @POST("packages/{packageId}/request-trip/{tripId}")
    suspend fun shipperRequestTrip(
        @Path("packageId") packageId: Int,
        @Path("tripId") tripId: Int,
        @Body body: ShipperCounterOfferRequestJson,
    ): Response<ShipperRequestResponseJson>

    // -- Rating --

    @POST("matches/{matchId}/rate")
    suspend fun rateMatch(@Path("matchId") matchId: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<MatchResponseJson>

    // -- Auto-charge retry --

    @POST("payments/matches/{matchId}/auto-charge")
    suspend fun retryAutoCharge(@Path("matchId") matchId: Int): Response<MatchResponseJson>
}
