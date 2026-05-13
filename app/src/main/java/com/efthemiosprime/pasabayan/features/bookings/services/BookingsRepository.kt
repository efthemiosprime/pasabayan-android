package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult

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
}
