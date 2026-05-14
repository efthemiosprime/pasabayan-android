package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
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
class CompatiblePackagesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeRepo: FakeBookingsRepositoryForCompatible
    private lateinit var viewModel: CompatiblePackagesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        every { context.getString(R.string.bookings_compatible_packages_error_load) } returns
            "Failed to load compatible packages. Please try again."
        fakeRepo = FakeBookingsRepositoryForCompatible()
        viewModel = CompatiblePackagesViewModel(context, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCompatiblePackages queries the trip and exposes results`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.success(
            listOf(pkg(id = 100), pkg(id = 200)),
        )

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.packages.size)
        assertEquals(100, state.packages[0].id)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf(42), fakeRepo.compatiblePackagesCalls)
    }

    @Test
    fun `loadCompatiblePackages surfaces error message on failure`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.failure(RuntimeException("timed out"))

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("timed out", state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded)
        assertTrue(state.packages.isEmpty())
    }

    @Test
    fun `loadCompatiblePackages uses fallback message when error has none`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.failure(RuntimeException(null as String?))

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()

        assertEquals(
            "Failed to load compatible packages. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `loadCompatiblePackages is no-op for same trip when already loaded and not forced`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.success(emptyList())

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()
        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()

        assertEquals(1, fakeRepo.compatiblePackagesCalls.size)
    }

    @Test
    fun `loadCompatiblePackages re-runs when tripId changes`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.success(emptyList())

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()
        viewModel.loadCompatiblePackages(tripId = 99)
        advanceUntilIdle()

        assertEquals(listOf(42, 99), fakeRepo.compatiblePackagesCalls)
    }

    @Test
    fun `refresh re-runs the load for the same trip after a prior load`() = runTest {
        fakeRepo.compatiblePackagesResult = Result.success(emptyList())

        viewModel.loadCompatiblePackages(tripId = 42)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(42, 42), fakeRepo.compatiblePackagesCalls)
    }

    @Test
    fun `refresh is a no-op before any trip has been loaded`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(fakeRepo.compatiblePackagesCalls.isEmpty())
    }

    private fun pkg(id: Int): PackageRequest = PackageRequest(
        id = id,
        shipperId = null,
        pickupAddress = null,
        pickupCity = "Manila",
        pickupCountry = null,
        deliveryAddress = null,
        deliveryCity = "Cebu",
        deliveryCountry = null,
        packageWeightKg = 5.0,
        packageDimensions = null,
        packageType = null,
        fragile = null,
        packageValue = null,
        packageDescription = "Test package $id",
        urgencyLevel = null,
        maxPriceBudget = 100.0,
        pickupDatePreferred = null,
        pickupTimePreferred = null,
        pickupDateFlexible = null,
        deliveryDateNeeded = null,
        deliveryTimeNeeded = null,
        specialHandlingRequirements = null,
        requestStatus = null,
        createdAt = null,
        updatedAt = null,
        compatibleTripsCount = null,
        shipper = null,
        images = null,
        imagesProcessing = null,
        serviceType = null,
        shoppingList = null,
        storeName = null,
        storeAddress = null,
        receiptRequired = null,
    )
}

/**
 * Minimal `BookingsRepository` stub — only `getCompatiblePackages` is exercised
 * by `CompatiblePackagesViewModel`. Hand-rolled because mockk can't mock the
 * inline-value `Result` return type cleanly.
 */
private class FakeBookingsRepositoryForCompatible : BookingsRepository {
    var compatiblePackagesResult: Result<List<PackageRequest>> = Result.success(emptyList())
    val compatiblePackagesCalls: MutableList<Int> = mutableListOf()

    override suspend fun getCompatiblePackages(tripId: Int): Result<List<PackageRequest>> {
        compatiblePackagesCalls += tripId
        return compatiblePackagesResult
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
    override suspend fun shipperDeclineCarrierRequest(matchId: Int, reason: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int, reason: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int) =
        Result.failure<String>(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int) =
        Result.failure<String>(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
    override suspend fun retryAutoCharge(matchId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
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
    override suspend fun getCompatibleTrips(packageRequestId: Int, carrierId: Int?) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip>>(Exception("Not used"))
    override suspend fun getReceiverAccess(matchId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>>(Exception("Not used"))
    override suspend fun createReceiverAccess(matchId: Int, generatePin: Boolean, receiverName: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>(Exception("Not used"))
    override suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
    override suspend fun bookTripDirect(
        tripId: Int,
        payload: com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData>(Exception("Not used"))
    override suspend fun submitRating(matchId: Int, rating: Int, reviewText: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
}
