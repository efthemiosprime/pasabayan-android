package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
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

    // iOS parity (`TripUpdateTimeoutCoordinator`): TripStatusUpdateSheet calls the suspend
    // variant and drives its own isUpdating spinner + 15s fallback timeout.
    @Test
    fun `suspendUpdateTripStatus returns success and mirrors trip into state`() = runTest {
        val existing = testTrip(1, TripStatus.ACTIVE)
        val updated = existing.copy(tripStatus = TripStatus.IN_TRANSIT)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.updateResult = Result.success(updated)
        viewModel.loadTrips()
        advanceUntilIdle()

        val result = viewModel.suspendUpdateTripStatus(1, TripStatus.IN_TRANSIT)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(TripStatus.IN_TRANSIT, result.getOrNull()!!.tripStatus)
        assertEquals(TripStatus.IN_TRANSIT, viewModel.uiState.value.trips.first().tripStatus)
        assertEquals("in_transit", fakeRepo.lastUpdateStatus)
    }

    @Test
    fun `suspendUpdateTripStatus returns failure for invalid transition`() = runTest {
        val existing = testTrip(1, TripStatus.COMPLETED)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        viewModel.loadTrips()
        advanceUntilIdle()

        val result = viewModel.suspendUpdateTripStatus(1, TripStatus.ACTIVE)

        assertTrue(result.isFailure)
        assertNull(fakeRepo.lastUpdateStatus)
    }

    @Test
    fun `suspendUpdateTripStatus returns failure when trip is unknown`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(emptyList())
        viewModel.loadTrips()
        advanceUntilIdle()

        val result = viewModel.suspendUpdateTripStatus(999, TripStatus.ACTIVE)

        assertTrue(result.isFailure)
        assertNull(fakeRepo.lastUpdateStatus)
    }

    @Test
    fun `suspendUpdateTripStatus propagates repository failure`() = runTest {
        val existing = testTrip(1, TripStatus.ACTIVE)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.updateResult = Result.failure(Exception("Network down"))
        viewModel.loadTrips()
        advanceUntilIdle()

        val result = viewModel.suspendUpdateTripStatus(1, TripStatus.IN_TRANSIT)

        assertTrue(result.isFailure)
        assertEquals("Network down", result.exceptionOrNull()?.message)
        // Local state should not have mutated when the repo failed.
        assertEquals(TripStatus.ACTIVE, viewModel.uiState.value.trips.first().tripStatus)
    }

    @Test
    fun `cancelTrip marks active trip as cancelled when no blocking matches`() = runTest {
        val existing = testTrip(1, TripStatus.ACTIVE)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                testTripMatch(id = 10, status = MatchStatus.PENDING),
            ),
        )
        fakeRepo.deleteResult = Result.success(Unit)
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.cancelTrip(1)
        advanceUntilIdle()

        assertEquals(MatchStatus.PENDING, fakeRepo.tripMatchesResult.getOrThrow().first().matchStatus)
        assertEquals(listOf(1), fakeRepo.loadedTripMatchIds)
        assertEquals(listOf(1), fakeRepo.deletedTripIds)
        assertEquals(TripStatus.CANCELLED, viewModel.uiState.value.trips.first().tripStatus)
    }

    @Test
    fun `cancelTrip is blocked when matches are confirmed picked up or in transit`() = runTest {
        val existing = testTrip(1, TripStatus.ACTIVE)
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                testTripMatch(id = 11, status = MatchStatus.CONFIRMED),
                testTripMatch(id = 12, status = MatchStatus.PICKED_UP),
                testTripMatch(id = 13, status = MatchStatus.IN_TRANSIT),
            ),
        )
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.cancelTrip(1)
        advanceUntilIdle()

        assertEquals(0, fakeRepo.deletedTripIds.size)
        assertEquals(TripStatus.ACTIVE, viewModel.uiState.value.trips.first().tripStatus)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `cancelTrip is blocked for completed trip before loading matches`() = runTest {
        fakeRepo.carrierTripsResult = Result.success(listOf(testTrip(1, TripStatus.COMPLETED)))
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.cancelTrip(1)
        advanceUntilIdle()

        assertEquals(0, fakeRepo.loadedTripMatchIds.size)
        assertEquals(0, fakeRepo.deletedTripIds.size)
    }

    @Test
    fun `updateTripDetails sends weight and notes and updates state`() = runTest {
        val existing = testTrip(7, TripStatus.ACTIVE).copy(availableWeightKg = 20.0, specialNotes = "Old")
        val updated = existing.copy(availableWeightKg = 12.5, specialNotes = "New")
        fakeRepo.carrierTripsResult = Result.success(listOf(existing))
        fakeRepo.updateResult = Result.success(updated)
        viewModel.loadTrips()
        advanceUntilIdle()

        viewModel.updateTripDetails(tripId = 7, availableWeightKg = 12.5, specialNotes = "New")
        advanceUntilIdle()

        assertEquals(12.5, requireNotNull(viewModel.uiState.value.trips.first().availableWeightKg), 0.001)
        assertEquals("New", viewModel.uiState.value.trips.first().specialNotes)
        assertEquals(12.5, requireNotNull(fakeRepo.lastUpdateWeight), 0.001)
        assertEquals("New", fakeRepo.lastUpdateNotes)
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

    private fun testTripMatch(
        id: Int,
        status: MatchStatus,
    ) = TripMatchPackage(
        id = id,
        matchStatus = status,
        agreedPrice = null,
        packageDescription = null,
        packageWeightKg = null,
        packageId = null,
        shipper = null,
        chatConversationId = null,
        confirmedAt = null,
        pickedUpAt = null,
        deliveredAt = null,
        createdAt = null,
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
    var lastTripTemplatePackageId: Int? = null
    var deletedTripIds: MutableList<Int> = mutableListOf()
    var loadedTripMatchIds: MutableList<Int> = mutableListOf()
    var lastUpdateStatus: String? = null
    var lastUpdateWeight: Double? = null
    var lastUpdateNotes: String? = null
    var lastAvailableTripsFilter: TripFilter? = null

    override suspend fun loadCarrierTrips() = carrierTripsResult
    override suspend fun loadAvailableTrips(filter: TripFilter): Result<List<Trip>> {
        lastAvailableTripsFilter = filter
        return availableTripsResult
    }
    override suspend fun loadPopularPackageRoutes() = popularRoutesResult
    override suspend fun loadRouteActivitySummary() = routeActivitySummaryResult
    override suspend fun loadTripMatches(tripId: Int): Result<List<TripMatchPackage>> {
        loadedTripMatchIds.add(tripId)
        return tripMatchesResult
    }
    override suspend fun loadTripTemplate(packageId: Int): Result<TripTemplateData> {
        lastTripTemplatePackageId = packageId
        return tripTemplateResult
    }
    override suspend fun getTrip(id: Int) = getTripResult ?: Result.failure(Exception("Not set"))
    override suspend fun createTrip(request: CreateTripRequestJson) = createResult ?: Result.failure(Exception("Not set"))
    override suspend fun createTripFromPackage(request: CreateTripFromPackageRequest): Result<Trip> =
        createResult ?: Result.failure(Exception("Not set"))
    override suspend fun updateTrip(id: Int, request: TripUpdateRequestJson): Result<Trip> {
        lastUpdateStatus = request.tripStatus
        lastUpdateWeight = request.availableWeightKg
        lastUpdateNotes = request.specialNotes
        return updateResult ?: Result.failure(Exception("Not set"))
    }
    override suspend fun deleteTrip(id: Int): Result<Unit> {
        deletedTripIds.add(id)
        return deleteResult
    }
}
