package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import io.mockk.every
import io.mockk.mockk
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
class CompatibleTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeRepo: FakeBookingsRepositoryForCompatibleTrips
    private lateinit var viewModel: CompatibleTripsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        every { context.getString(R.string.bookings_compatible_trips_error_load) } returns
            "Failed to load compatible trips. Please try again."
        fakeRepo = FakeBookingsRepositoryForCompatibleTrips()
        viewModel = CompatibleTripsViewModel(context, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCompatibleTrips queries the package and exposes results`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(listOf(trip(id = 1), trip(id = 2)))

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.trips.size)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf(42 to null), fakeRepo.compatibleTripsCalls)
    }

    @Test
    fun `loadCompatibleTrips forwards carrierId filter to repo`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(listOf(trip(id = 1, carrierId = 7)))

        viewModel.loadCompatibleTrips(packageRequestId = 42, carrierId = 7)
        advanceUntilIdle()

        assertEquals(listOf(42 to 7), fakeRepo.compatibleTripsCalls)
        assertEquals(1, viewModel.uiState.value.trips.size)
    }

    @Test
    fun `loadCompatibleTrips surfaces error message on failure`() = runTest {
        fakeRepo.compatibleTripsResult = Result.failure(RuntimeException("timed out"))

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("timed out", state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded)
        assertTrue(state.trips.isEmpty())
    }

    @Test
    fun `loadCompatibleTrips uses fallback message when error has none`() = runTest {
        fakeRepo.compatibleTripsResult = Result.failure(RuntimeException(null as String?))

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()

        assertEquals(
            "Failed to load compatible trips. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `loadCompatibleTrips is no-op for same package when already loaded and not forced`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(emptyList())

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()
        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()

        assertEquals(1, fakeRepo.compatibleTripsCalls.size)
    }

    @Test
    fun `loadCompatibleTrips re-runs when packageRequestId changes`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(emptyList())

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()
        viewModel.loadCompatibleTrips(packageRequestId = 99)
        advanceUntilIdle()

        assertEquals(listOf(42 to null, 99 to null), fakeRepo.compatibleTripsCalls)
    }

    @Test
    fun `loadCompatibleTrips re-runs when carrierId changes for same package`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(emptyList())

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()
        viewModel.loadCompatibleTrips(packageRequestId = 42, carrierId = 7)
        advanceUntilIdle()

        assertEquals(listOf(42 to null, 42 to 7), fakeRepo.compatibleTripsCalls)
    }

    @Test
    fun `refresh re-runs the load for the same package after a prior load`() = runTest {
        fakeRepo.compatibleTripsResult = Result.success(emptyList())

        viewModel.loadCompatibleTrips(packageRequestId = 42)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(42 to null, 42 to null), fakeRepo.compatibleTripsCalls)
    }

    @Test
    fun `refresh is a no-op before any package has been loaded`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(fakeRepo.compatibleTripsCalls.isEmpty())
    }

    private fun trip(id: Int, carrierId: Int = 1): CompatibleTrip = CompatibleTrip(
        id = id, carrierId = carrierId,
        originCity = "Manila", originCountry = "PH",
        destinationCity = "Cebu", destinationCountry = "PH",
        departureDate = null, arrivalDate = null,
        pickupDate = null, deliveryDate = null,
        availableWeightKg = "20.0", availableSpaceLiters = "100.0",
        pricePerKg = "5.50", flatTripPrice = null, calculatedPrice = null,
        pricingType = "per_kg", pricingMethod = null,
        tripStatus = "active", transportationMethod = "flight",
        specialNotes = null, createdAt = null, updatedAt = null,
        carrier = null, shipperRequestStatus = null, canRequest = null,
        requestMessage = null, requestedAt = null, distanceKm = null,
    )
}

private class FakeBookingsRepositoryForCompatibleTrips : BookingsRepository {
    var compatibleTripsResult: Result<List<CompatibleTrip>> = Result.success(emptyList())
    val compatibleTripsCalls: MutableList<Pair<Int, Int?>> = mutableListOf()

    override suspend fun getCompatibleTrips(
        packageRequestId: Int,
        carrierId: Int?,
    ): Result<List<CompatibleTrip>> {
        compatibleTripsCalls += packageRequestId to carrierId
        return compatibleTripsResult
    }

    override suspend fun loadMatches(role: String?, status: String?) =
        Result.success(emptyList<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>())
    override suspend fun getMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult>(Exception("Not used"))
    override suspend fun cancelMatch(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult>(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperAcceptCarrierRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun retryAutoCharge(matchId: Int): Result<Unit> = Result.failure(Exception("Not used"))
    override suspend fun shipperRequestTrip(
        packageId: Int, tripId: Int, offeredPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult>(Exception("Not used"))
    override suspend fun carrierRequestPackage(
        tripId: Int, packageId: Int, proposedPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult>(Exception("Not used"))
    override suspend fun getCarrierLocation(matchId: Int) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot>(Exception("Not used"))
    override suspend fun getCompatiblePackages(tripId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.packages.model.PackageRequest>>(Exception("Not used"))
    override suspend fun getReceiverAccess(matchId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>>(Exception("Not used"))
    override suspend fun createReceiverAccess(matchId: Int, generatePin: Boolean, receiverName: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>(Exception("Not used"))
    override suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
}
