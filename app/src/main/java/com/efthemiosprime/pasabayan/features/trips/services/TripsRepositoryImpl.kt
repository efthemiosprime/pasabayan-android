package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripsApi
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.model.toDomain
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepositoryImpl @Inject constructor(
    private val tripsApi: TripsApi,
    private val json: Json,
) : TripsRepository {

    override suspend fun loadCarrierTrips(): Result<List<Trip>> {
        return try {
            val res = tripsApi.getTrips()
            if (!res.isSuccessful) {
                // Fallback: if message contains role error, retry with /carrier/trips
                val errorBytes = res.errorBody()?.bytes()
                val errorMsg = errorBytes?.let { String(it) } ?: ""
                if (errorMsg.contains("not registered as both carrier and shipper", ignoreCase = true)) {
                    return loadCarrierTripsFallback()
                }
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), errorBytes, json)),
                )
            }
            val trips = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private suspend fun loadCarrierTripsFallback(): Result<List<Trip>> {
        return try {
            val res = tripsApi.getCarrierTrips()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val trips = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadAvailableTrips(filter: TripFilter): Result<List<Trip>> {
        return try {
            val res = tripsApi.getAvailableTrips(filter.toQueryMap())
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val trips = res.body()?.data?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun getTrip(id: Int): Result<Trip> {
        return try {
            val res = tripsApi.getTrip(id)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val trip = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun createTrip(request: CreateTripRequestJson): Result<Trip> {
        return try {
            val res = tripsApi.createTrip(request)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val trip = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun updateTrip(id: Int, request: TripUpdateRequestJson): Result<Trip> {
        return try {
            val res = tripsApi.updateTrip(id, request)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val trip = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun deleteTrip(id: Int): Result<Unit> {
        return try {
            val res = tripsApi.deleteTrip(id)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }
}
