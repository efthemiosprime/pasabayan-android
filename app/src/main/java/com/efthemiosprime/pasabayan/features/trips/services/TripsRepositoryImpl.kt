package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripsApi
import com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.model.toDomain
import com.efthemiosprime.pasabayan.features.trips.model.toDomainOrZero
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepositoryImpl @Inject constructor(
    private val tripsApi: TripsApi,
    private val json: Json,
) : TripsRepository {

    private companion object {
        const val CREATE_TRIP_TIMEOUT_MS = 8_000L
    }

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

    override suspend fun loadAvailableTripsPage(
        filter: TripFilter,
        page: Int,
        perPage: Int,
    ): Result<AvailableTripsPage> {
        // Build a query map that honours the filter's own serialization but forces
        // the page / per_page we want. The filter struct also carries `page`; the
        // call-site argument wins.
        val params = buildMap {
            putAll(filter.copy(page = page).toQueryMap())
            put("per_page", perPage.toString())
            // copy() emits "page" only when > 1; ensure page=1 is also explicit.
            put("page", page.toString())
        }
        return try {
            val res = tripsApi.getAvailableTrips(params)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val body = res.body()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            val envelope = body.data
                ?: return Result.success(
                    AvailableTripsPage(
                        trips = emptyList(),
                        currentPage = page,
                        lastPage = page,
                        total = 0,
                        perPage = perPage,
                    ),
                )
            Result.success(
                AvailableTripsPage(
                    trips = envelope.data.map { it.toDomain() },
                    currentPage = envelope.currentPage,
                    lastPage = envelope.lastPage,
                    total = envelope.total,
                    perPage = envelope.perPage,
                ),
            )
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadPopularPackageRoutes(): Result<List<PopularRoute>> {
        return try {
            val res = tripsApi.getPopularPackageRoutes()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val routes = res.body()?.data?.map { it.toDomain() } ?: emptyList()
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadRouteActivitySummary(): Result<RouteActivitySummary> {
        return try {
            val res = tripsApi.getRouteActivitySummary()
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val summary = res.body()?.data.toDomainOrZero()
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>> {
        return try {
            val res = tripsApi.getTripMatches(tripId)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            Result.success(res.body()?.matches?.map { it.toDomain() } ?: emptyList())
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    override suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData> {
        return try {
            val res = tripsApi.getTripTemplate(packageId)
            if (!res.isSuccessful) {
                return Result.failure(
                    DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json)),
                )
            }
            val template = res.body()?.data
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(template.toDomain(packageId))
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
            val res = withTimeout(CREATE_TRIP_TIMEOUT_MS) {
                tripsApi.createTrip(request)
            }
            if (!res.isSuccessful) {
                val errorBytes = res.errorBody()?.bytes()
                return Result.failure(
                    DomainErrorMapperException(mapCreateTripError(res.code(), errorBytes)),
                )
            }
            val trip = res.body()?.data?.toDomain()
                ?: return Result.failure(DomainErrorMapperException(DomainError.InvalidResponse))
            Result.success(trip)
        } catch (_: TimeoutCancellationException) {
            val timeout = SocketTimeoutException("Create trip request timed out after ${CREATE_TRIP_TIMEOUT_MS / 1000}s")
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(timeout)))
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }
    }

    private fun mapCreateTripError(statusCode: Int, body: ByteArray?): DomainError {
        val message = body?.decodeToString().orEmpty()
        val normalized = message.lowercase()
        return when {
            statusCode == 400 && normalized.contains("cargo-only or passenger-only, not both") ->
                DomainError.MixedTransportTypes
            statusCode == 400 &&
                normalized.contains("must specify either cargo transport or passenger transport") ->
                DomainError.NoTransportTypeSpecified
            statusCode == 401 && normalized.contains("unauthenticated") ->
                DomainError.Unauthenticated
            statusCode == 403 && normalized.contains("not registered as a carrier") ->
                DomainError.UserNotCarrier
            else -> ApiErrorMapper.map(statusCode, body, json)
        }
    }

    override suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip> {
        val payload = CreateTripRequestJson(
            originCity = request.originCity,
            originCountry = request.originCountry,
            destinationCity = request.destinationCity,
            destinationCountry = request.destinationCountry,
            departureDate = request.departureDate,
            arrivalDate = request.arrivalDate,
            availableWeightKg = request.availableWeightKg,
            availableSpaceLiters = request.availableSpaceLiters,
            pricePerKg = request.pricePerKg,
            transportationMethod = request.transportationMethod,
            specialNotes = request.specialNotes,
            pricingMethod = null,
            flatTripPrice = request.flatTripPrice,
            basePrice = null,
            originCityId = null,
            destinationCityId = null,
            pickupAddress = request.pickupAddress,
            dropoffAddress = request.dropoffAddress,
            autoRequestPackageId = request.packageId,
            proposedPrice = request.proposedPrice,
            requestMessage = request.requestMessage,
        )
        return createTrip(payload)
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

    override suspend fun activateTrip(id: Int): Result<Trip> {
        return try {
            val res = tripsApi.activateTrip(id)
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
