package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTripFromPackageUseCaseTest {
    @Test
    fun `createTrip updates local state on success when userId is provided`() = runTest {
        val repository = FakeTripsRepository()
        val updater = FakeTripsLocalStateUpdater()
        val useCase = CreateTripFromPackageUseCase(repository, updater)
        val request = request()
        repository.createTripResult = Result.success(testTrip(id = 101))

        val result = useCase.createTrip(request, userId = 5L)

        assertTrue(result.isSuccess)
        assertEquals(5L, updater.lastCreatedUserId)
        assertEquals(request, updater.lastCreateRequest)
    }

    @Test
    fun `createTrip skips local state update when userId is null`() = runTest {
        val repository = FakeTripsRepository()
        val updater = FakeTripsLocalStateUpdater()
        val useCase = CreateTripFromPackageUseCase(repository, updater)
        val request = request()
        repository.createTripResult = Result.success(testTrip(id = 102))

        val result = useCase.createTrip(request, userId = null)

        assertTrue(result.isSuccess)
        assertEquals(null, updater.lastCreatedUserId)
        assertEquals(null, updater.lastCreateRequest)
    }

    private fun request(): CreateTripFromPackageRequest = CreateTripFromPackageRequest(
        packageId = 10,
        originCity = "Toronto",
        originCountry = "CA",
        destinationCity = "Montreal",
        destinationCountry = "CA",
        departureDate = "2026-06-01T08:00:00Z",
        arrivalDate = "2026-06-01T12:00:00Z",
        availableWeightKg = 10.0,
        availableSpaceLiters = 20.0,
        transportationMethod = "car",
        pricePerKg = null,
        flatTripPrice = 25.0,
        specialNotes = null,
        pickupAddress = null,
        dropoffAddress = null,
        proposedPrice = null,
        requestMessage = null,
    )

    private fun testTrip(id: Int): Trip = Trip(
        id = id,
        carrierId = 1,
        originCity = "Toronto",
        originCountry = "CA",
        originLat = null,
        originLng = null,
        destinationCity = "Montreal",
        destinationCountry = "CA",
        destinationLat = null,
        destinationLng = null,
        departureDate = "2026-06-01T08:00:00Z",
        arrivalDate = "2026-06-01T12:00:00Z",
        availableWeightKg = 10.0,
        availableSpaceLiters = 20.0,
        pricePerKg = null,
        tripStatus = TripStatus.PLANNING,
        transportationMethod = TransportationMethod.CAR,
        specialNotes = null,
        carrier = null,
        createdAt = null,
        updatedAt = null,
        pricingType = "flat",
        pricingMethod = null,
        flatTripPrice = 25.0,
        basePrice = null,
        calculatedPrice = null,
        pickupAddress = null,
        pickupLandmark = null,
        dropoffAddress = null,
        dropoffLandmark = null,
        tripEarningsTotal = null,
        tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null,
        pendingRequestCount = null,
        pendingRequests = null,
        distanceKm = null,
    )

    private class FakeTripsRepository : TripsRepository {
        var createTripResult: Result<Trip> = Result.failure(IllegalStateException("unset"))

        override suspend fun loadCarrierTrips(): Result<List<Trip>> = Result.success(emptyList())

        override suspend fun loadAvailableTrips(filter: TripFilter): Result<List<Trip>> = Result.success(emptyList())

        override suspend fun loadAvailableTripsPage(
            filter: TripFilter,
            page: Int,
            perPage: Int,
        ): Result<com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage> = Result.success(
            com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage(
                trips = emptyList(),
                currentPage = page,
                lastPage = page,
                total = 0,
                perPage = perPage,
            ),
        )

        override suspend fun loadPopularPackageRoutes(): Result<List<PopularRoute>> = Result.success(emptyList())

        override suspend fun loadRouteActivitySummary(): Result<RouteActivitySummary> =
            Result.success(RouteActivitySummary(0, 0, 0, null, null))

        override suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>> =
            Result.success(emptyList())

        override suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getTrip(id: Int): Result<Trip> = Result.failure(UnsupportedOperationException())

        override suspend fun createTrip(request: CreateTripRequestJson): Result<Trip> =
            Result.failure(UnsupportedOperationException())

        override suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip> =
            createTripResult

        override suspend fun updateTrip(id: Int, request: TripUpdateRequestJson): Result<Trip> =
            Result.failure(UnsupportedOperationException())

        override suspend fun activateTrip(id: Int): Result<Trip> = Result.failure(UnsupportedOperationException())

        override suspend fun deleteTrip(id: Int): Result<Unit> = Result.failure(UnsupportedOperationException())
    }

    private class FakeTripsLocalStateUpdater : TripsLocalStateUpdater {
        var lastCreatedUserId: Long? = null
        var lastCreateRequest: CreateTripFromPackageRequest? = null

        override fun onTripCreationSucceeded(userId: Long, request: CreateTripFromPackageRequest) {
            lastCreatedUserId = userId
            lastCreateRequest = request
        }

        override fun onCarrierDisclaimerAcknowledgeAttempt(userId: Long, syncSucceeded: Boolean) = Unit

        override suspend fun retryPendingCarrierDisclaimerSync(
            userId: Long,
            syncAction: suspend () -> Result<Unit>,
        ): Boolean = false
    }
}
