package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.bookings.BookingsApi
import com.efthemiosprime.pasabayan.core.network.bookings.MatchResponseJson
import com.efthemiosprime.pasabayan.core.network.bookings.ShipperCounterOfferRequestJson
import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.toDomain
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingsRepositoryImpl @Inject constructor(
    private val bookingsApi: BookingsApi,
    private val json: Json,
) : BookingsRepository {

    override suspend fun loadMatches(role: String?, status: String?): Result<List<DeliveryMatch>> {
        return try {
            val res = bookingsApi.getMatches(role = role, status = status)
            if (!res.isSuccessful) {
                return Result.failure(mapError(res))
            }
            val matches = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getMatch(matchId: Int): Result<DeliveryMatch> {
        return try {
            val res = bookingsApi.getMatch(matchId)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val match = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun confirmMatch(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.confirmMatch(matchId) }

    override suspend fun cancelMatch(matchId: Int): Result<CancelMatchResult> {
        return try {
            val res = bookingsApi.cancelMatch(matchId)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val result = res.body()?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.markPickedUp(matchId) }

    override suspend fun markInTransit(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.markInTransit(matchId) }

    override suspend fun markDelivered(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.markDelivered(matchId) }

    override suspend fun shipperAccept(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.shipperAccept(matchId) }

    override suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.shipperDecline(matchId) }

    override suspend fun carrierAcceptShipperRequest(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.carrierAcceptShipperRequest(matchId) }

    override suspend fun carrierDeclineShipperRequest(matchId: Int): Result<DeliveryMatch> =
        matchAction { bookingsApi.carrierDeclineShipperRequest(matchId) }

    override suspend fun generatePickupCode(matchId: Int): Result<String> {
        return try {
            val res = bookingsApi.generatePickupCode(matchId)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val code = res.body()?.data?.code
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun generateDeliveryCode(matchId: Int): Result<String> {
        return try {
            val res = bookingsApi.generateDeliveryCode(matchId)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val code = res.body()?.data?.code
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch> {
        return try {
            val res = bookingsApi.confirmPickupWithCode(matchId, code)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val match = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch> {
        return try {
            val res = bookingsApi.confirmDeliveryWithCode(matchId, code)
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val match = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
    ): Result<DeliveryMatch> {
        return try {
            val res = bookingsApi.shipperRequestTrip(
                packageId = packageId,
                tripId = tripId,
                body = ShipperCounterOfferRequestJson(
                    proposedPrice = offeredPrice,
                    message = message,
                    isCounterOffer = false,
                ),
            )
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val match = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private suspend fun matchAction(call: suspend () -> Response<MatchResponseJson>): Result<DeliveryMatch> {
        return try {
            val res = call()
            if (!res.isSuccessful) return Result.failure(mapError(res))
            val match = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))
}
