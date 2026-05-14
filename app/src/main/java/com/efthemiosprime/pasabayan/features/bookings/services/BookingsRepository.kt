package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip
import com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload
import com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken
import com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

interface BookingsRepository {

    suspend fun loadMatches(role: String? = null, status: String? = null): Result<List<DeliveryMatch>>

    suspend fun getMatch(matchId: Int): Result<DeliveryMatch>

    suspend fun confirmMatch(matchId: Int): Result<ConfirmMatchResult>

    suspend fun cancelMatch(matchId: Int): Result<CancelMatchResult>

    suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch>

    suspend fun markInTransit(matchId: Int): Result<DeliveryMatch>

    suspend fun markDelivered(matchId: Int): Result<DeliveryMatch>

    suspend fun shipperAcceptCarrierRequest(
        matchId: Int,
        acknowledgeOverage: Boolean? = null,
    ): Result<DeliveryMatch>

    suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch>

    suspend fun carrierAcceptShipperRequest(
        matchId: Int,
        acknowledgeOverage: Boolean? = null,
    ): Result<DeliveryMatch>

    suspend fun carrierDeclineShipperRequest(matchId: Int): Result<DeliveryMatch>

    suspend fun generatePickupCode(matchId: Int): Result<String>

    suspend fun generateDeliveryCode(matchId: Int): Result<String>

    suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch>

    suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch>

    /**
     * Retry the auto-charge for a match whose initial confirm enqueued no
     * charge (e.g. shipper had no default payment method at confirm time).
     * Backed by `POST /payments/matches/{id}/auto-charge`.
     */
    suspend fun retryAutoCharge(matchId: Int): Result<Unit>

    suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
        isCounterOffer: Boolean = false,
        originalMatchId: Int? = null,
        originalPrice: Double? = null,
    ): Result<RequestMatchResult>

    suspend fun carrierRequestPackage(
        tripId: Int,
        packageId: Int,
        proposedPrice: Double,
        message: String?,
        isCounterOffer: Boolean = false,
        originalMatchId: Int? = null,
        originalPrice: Double? = null,
    ): Result<RequestMatchResult>

    /**
     * Fetch the carrier's current location snapshot for live tracking.
     * Backed by `GET /matches/{matchId}/carrier-location`.
     */
    suspend fun getCarrierLocation(matchId: Int): Result<CarrierLocationSnapshot>

    // -- Discovery / compatibility --

    /**
     * Trips compatible with a shipper's package request.
     * Backed by `GET /packages/{packageRequestId}/compatible-trips`.
     *
     * @param carrierId optional client-side filter — when non-null, only trips
     *  owned by the given carrier are returned. Mirrors iOS `getCompatibleTrips`.
     */
    suspend fun getCompatibleTrips(
        packageRequestId: Int,
        carrierId: Int? = null,
    ): Result<List<CompatibleTrip>>

    /**
     * Packages compatible with a carrier's trip.
     * Backed by `GET /trips/{tripId}/compatible-packages`.
     */
    suspend fun getCompatiblePackages(tripId: Int): Result<List<PackageRequest>>

    // -- Receiver access --

    suspend fun getReceiverAccess(matchId: Int): Result<List<ReceiverAccessToken>>

    suspend fun createReceiverAccess(
        matchId: Int,
        generatePin: Boolean = true,
        receiverName: String? = null,
    ): Result<ReceiverAccessToken>

    suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit>

    // -- Direct booking --

    /**
     * Book a trip directly without going through the package-request match flow.
     * Backed by `POST /trips/{tripId}/book`. Mirrors iOS `bookTripDirect`.
     */
    suspend fun bookTripDirect(
        tripId: Int,
        payload: DirectBookingPayload,
    ): Result<DirectBookingData>

    // -- Rating --

    /**
     * Submit a post-delivery rating for a match. Backed by
     * `POST /matches/{matchId}/rate`. Mirrors iOS `RatingViewModel.submitRating`.
     *
     * @param rating integer star value (1..5)
     * @param reviewText optional written review; null/blank is allowed
     */
    suspend fun submitRating(
        matchId: Int,
        rating: Int,
        reviewText: String?,
    ): Result<DeliveryMatch>
}
