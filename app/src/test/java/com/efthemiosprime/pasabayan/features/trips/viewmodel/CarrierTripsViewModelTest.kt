package com.efthemiosprime.pasabayan.features.trips.viewmodel

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
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarrierTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var viewModel: CarrierTripsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTripsRepository()
        viewModel = CarrierTripsViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTrips sets trips on success`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(listOf(testTrip(1), testTrip(2)))
        viewModel.loadTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.trips.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadTrips sets error on failure`() = runTest {
        fakeRepo.carrierTripsResult = Result.failure(Exception("Network error"))
        viewModel.loadTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.trips.isEmpty())
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `setStatusFilter filters trips by status`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(
            listOf(
                testTrip(1, TripStatus.ACTIVE),
                testTrip(2, TripStatus.PLANNING),
                testTrip(3, TripStatus.CANCELLED),
            ),
        )
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.setStatusFilter(TripStatus.ACTIVE)
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredTrips.size)
        assertEquals(TripStatus.ACTIVE, state.filteredTrips[0].tripStatus)
    }

    @Test
    fun `setStatusFilter null shows all except cancelled by default`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(
            listOf(
                testTrip(1, TripStatus.ACTIVE),
                testTrip(2, TripStatus.CANCELLED),
                testTrip(3, TripStatus.PLANNING),
            ),
        )
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.setStatusFilter(null)
        val state = viewModel.uiState.value
        assertEquals(2, state.filteredTrips.size)
        assertTrue(state.filteredTrips.none { it.tripStatus == TripStatus.CANCELLED })
    }

    @Test
    fun `statusCounts computed correctly`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(
            listOf(
                testTrip(1, TripStatus.ACTIVE),
                testTrip(2, TripStatus.ACTIVE),
                testTrip(3, TripStatus.PLANNING),
                testTrip(4, TripStatus.COMPLETED),
            ),
        )
        viewModel.loadTrips()
        advanceUntilIdle()

        val counts = viewModel.uiState.value.statusCounts
        assertEquals(2, counts[TripStatus.ACTIVE])
        assertEquals(1, counts[TripStatus.PLANNING])
        assertEquals(1, counts[TripStatus.COMPLETED])
    }

    @Test
    fun `deleteTrip removes trip from list on success`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(listOf(testTrip(1), testTrip(2)))
        fakeRepo.deleteResult = Result.success(Unit)
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.deleteTrip(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.trips.size)
        assertEquals(2, viewModel.uiState.value.trips[0].id)
    }

    @Test
    fun `deleteTrip is blocked for in transit trips`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(listOf(testTrip(1, TripStatus.IN_TRANSIT)))
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.deleteTrip(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.trips.size)
        assertEquals(0, fakeRepo.deletedTripIds.size)
    }

    @Test
    fun `updateTripStatus enforces transition rules`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(listOf(testTrip(1, TripStatus.COMPLETED)))
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.updateTripStatus(1, TripStatus.ACTIVE)
        advanceUntilIdle()

        assertNull(fakeRepo.lastUpdateStatus)
    }

    @Test
    fun `updateTripStatus updates trip on success`() = runTest {
        val existing = testTrip(1, TripStatus.ACTIVE)
        val updated = existing.copy(tripStatus = TripStatus.IN_TRANSIT)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.updateResult = Result.success(updated)
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.updateTripStatus(1, TripStatus.IN_TRANSIT)
        advanceUntilIdle()

        assertEquals(TripStatus.IN_TRANSIT, viewModel.uiState.value.trips.first().tripStatus)
        assertEquals("in_transit", fakeRepo.lastUpdateStatus)
    }

    private fun testTrip(
        id: Int,
        status: TripStatus = TripStatus.ACTIVE,
    ) = Trip(
        id = id, carrierId = 1,
        originCity = "Toronto", originCountry = "Canada",
        originLat = null, originLng = null,
        destinationCity = "Vancouver", destinationCountry = "Canada",
        destinationLat = null, destinationLng = null,
        departureDate = "2026-04-01T08:00:00Z",
        arrivalDate = "2026-04-01T14:00:00Z",
        availableWeightKg = 25.0, availableSpaceLiters = 50.0,
        pricePerKg = 15.0, tripStatus = status,
        transportationMethod = TransportationMethod.FLIGHT,
        specialNotes = null, carrier = null,
        createdAt = null, updatedAt = null,
        pricingType = null, pricingMethod = null,
        flatTripPrice = null, basePrice = null, calculatedPrice = null,
        pickupAddress = null, pickupLandmark = null,
        dropoffAddress = null, dropoffLandmark = null,
        tripEarningsTotal = null, tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null, pendingRequestCount = null,
        pendingRequests = null, distanceKm = null,
    )
}

/** Fake repository for ViewModel tests. */
class FakeTripsRepository : TripsRepository {
    var carrierTripsResult: Result<List<Trip>> = Result.success(emptyList())
    var availableTripsResult: Result<List<Trip>> = Result.success(emptyList())
    var getTripResult: Result<Trip>? = null
    var createResult: Result<Trip>? = null
    var updateResult: Result<Trip>? = null
    var deleteResult: Result<Unit> = Result.success(Unit)
    var popularRoutesResult: Result<List<PopularRoute>> = Result.success(emptyList())
    var routeActivitySummaryResult: Result<RouteActivitySummary> = Result.success(
        RouteActivitySummary(0, 0, 0, null, null),
    )
    var tripMatchesResult: Result<List<TripMatchPackage>> = Result.success(emptyList())
    var tripTemplateResult: Result<TripTemplateData> = Result.failure(Exception("Not set"))
    var deletedTripIds: MutableList<Int> = mutableListOf()
    var lastUpdateStatus: String? = null

    override suspend fun loadCarrierTrips() = carrierTripsResult
    override suspend fun loadAvailableTrips(filter: TripFilter) = availableTripsResult
    override suspend fun loadPopularPackageRoutes() = popularRoutesResult
    override suspend fun loadRouteActivitySummary() = routeActivitySummaryResult
    override suspend fun loadTripMatches(tripId: Int) = tripMatchesResult
    override suspend fun loadTripTemplate(packageId: Int) = tripTemplateResult
    override suspend fun getTrip(id: Int) = getTripResult ?: Result.failure(Exception("Not set"))
    override suspend fun createTrip(request: CreateTripRequestJson) = createResult ?: Result.failure(Exception("Not set"))
    override suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip> =
        createResult ?: Result.failure(Exception("Not set"))
    override suspend fun updateTrip(id: Int, request: TripUpdateRequestJson): Result<Trip> {
        lastUpdateStatus = request.tripStatus
        return updateResult ?: Result.failure(Exception("Not set"))
    }
    override suspend fun deleteTrip(id: Int): Result<Unit> {
        deletedTripIds.add(id)
        return deleteResult
    }
}
