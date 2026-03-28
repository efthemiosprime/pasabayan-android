package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
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
class BrowseTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var viewModel: BrowseTripsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTripsRepository()
        viewModel = BrowseTripsViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAvailableTrips sets trips on success`() = runTest {
        fakeRepo.availableTripsResult = Result.success(
            listOf(testTrip(1), testTrip(2)),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.availableTrips.size)
        assertTrue(state.hasLoadedTrips)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadAvailableTrips sets error on failure`() = runTest {
        fakeRepo.availableTripsResult = Result.failure(Exception("No internet"))
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `loadAvailableTrips filters out non-bookable statuses`() = runTest {
        fakeRepo.availableTripsResult = Result.success(
            listOf(
                testTrip(1, TripStatus.ACTIVE),
                testTrip(2, TripStatus.COMPLETED),
                testTrip(3, TripStatus.PLANNING),
                testTrip(4, TripStatus.CANCELLED),
            ),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.availableTrips.size)
        assertTrue(state.availableTrips.all { it.tripStatus in listOf(TripStatus.PLANNING, TripStatus.ACTIVE) })
    }

    @Test
    fun `updateSearchText updates filter`() = runTest {
        viewModel.updateSearchText("Montreal")
        assertEquals("Montreal", viewModel.uiState.value.filter.searchText)
    }

    @Test
    fun `clearFilters resets filter`() = runTest {
        viewModel.updateSearchText("test")
        viewModel.clearFilters()
        assertTrue(viewModel.uiState.value.filter.searchText.isEmpty())
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
