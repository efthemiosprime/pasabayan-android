package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch

interface BookingsRepository {

    suspend fun loadMatches(role: String? = null, status: String? = null): Result<List<DeliveryMatch>>

    suspend fun getMatch(matchId: Int): Result<DeliveryMatch>

    suspend fun confirmMatch(matchId: Int): Result<DeliveryMatch>

    suspend fun cancelMatch(matchId: Int): Result<CancelMatchResult>

    suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch>

    suspend fun markInTransit(matchId: Int): Result<DeliveryMatch>

    suspend fun markDelivered(matchId: Int): Result<DeliveryMatch>

    suspend fun shipperAccept(matchId: Int): Result<DeliveryMatch>

    suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch>

    suspend fun carrierAcceptShipperRequest(matchId: Int): Result<DeliveryMatch>

    suspend fun carrierDeclineShipperRequest(matchId: Int): Result<DeliveryMatch>

    suspend fun generatePickupCode(matchId: Int): Result<String>

    suspend fun generateDeliveryCode(matchId: Int): Result<String>

    suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch>

    suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch>

    suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
    ): Result<DeliveryMatch>
}
