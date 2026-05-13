package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class LiveTrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeBookingsRepository
    private lateinit var viewModel: LiveTrackingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBookingsRepository()
        viewModel = LiveTrackingViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generatePickupCode sets code on success`() = runTest {
        viewModel.generatePickupCode(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("123456", state.pickupCode)
        assertFalse(state.isGeneratingCode)
    }

    @Test
    fun `generateDeliveryCode sets code on success`() = runTest {
        viewModel.generateDeliveryCode(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("654321", state.deliveryCode)
    }

    @Test
    fun `confirmPickupWithCode returns match on success`() = runTest {
        val confirmed = testMatch(100, MatchStatus.PICKED_UP)
        fakeRepo.confirmResult = Result.success(confirmed)

        viewModel.confirmPickupWithCode(100, "123456")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.codeVerified)
    }

    @Test
    fun `confirmPickupWithCode sets error on failure`() = runTest {
        fakeRepo.confirmResult = Result.failure(Exception("Invalid code"))

        viewModel.confirmPickupWithCode(100, "wrong")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)
        assertFalse(viewModel.uiState.value.codeVerified)
    }

    @Test
    fun `clearState resets all fields`() = runTest {
        viewModel.generatePickupCode(100)
        advanceUntilIdle()

        viewModel.clearState()
        val state = viewModel.uiState.value
        assertNull(state.pickupCode)
        assertNull(state.deliveryCode)
        assertNull(state.errorMessage)
        assertFalse(state.codeVerified)
        assertEquals(0.0, state.remainingDistanceKm, 0.001)
        assertEquals(0, state.estimatedMinutes)
        assertFalse(state.isStale)
    }

    // -- refreshLocation --

    @Test
    fun `refreshLocation populates carrier coords, distance, and ETA`() = runTest {
        viewModel.overrideClock(fixedClock("2026-05-13T10:30:00Z"))
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = 14.5995, carrierLng = 120.9842,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                lastUpdatedAt = "2026-05-13T10:25:00Z",
                isStale = false,
            ),
        )

        viewModel.refreshLocation(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingLocation)
        assertEquals(14.5995, state.carrierLat!!, 0.0001)
        assertEquals(120.9842, state.carrierLng!!, 0.0001)
        assertEquals(14.6760, state.deliveryLat!!, 0.0001)
        assertEquals(121.0437, state.deliveryLng!!, 0.0001)
        assertTrue("remaining should be 10–14 km, got ${state.remainingDistanceKm}",
            state.remainingDistanceKm in 10.0..14.0)
        assertTrue("ETA should be at least 1 min, got ${state.estimatedMinutes}",
            state.estimatedMinutes >= 1)
        assertFalse(state.isStale)
    }

    @Test
    fun `refreshLocation honors server stale flag over fresh timestamp`() = runTest {
        viewModel.overrideClock(fixedClock("2026-05-13T10:30:00Z"))
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = 14.5995, carrierLng = 120.9842,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                lastUpdatedAt = "2026-05-13T10:29:00Z",
                isStale = true,
            ),
        )

        viewModel.refreshLocation(100)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isStale)
    }

    @Test
    fun `refreshLocation uses local 10-minute window when server flag is null`() = runTest {
        viewModel.overrideClock(fixedClock("2026-05-13T10:30:00Z"))
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = 14.5995, carrierLng = 120.9842,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                // Older than 10 minutes — should be flagged stale.
                lastUpdatedAt = "2026-05-13T10:19:00Z",
                isStale = null,
            ),
        )

        viewModel.refreshLocation(100)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isStale)
    }

    @Test
    fun `refreshLocation with no carrier coords leaves distance and ETA zero`() = runTest {
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = null, carrierLng = null,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                lastUpdatedAt = null, isStale = null,
            ),
        )

        viewModel.refreshLocation(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.carrierLat)
        assertNull(state.carrierLng)
        assertEquals(0.0, state.remainingDistanceKm, 0.001)
        assertEquals(0, state.estimatedMinutes)
    }

    @Test
    fun `refreshLocation surfaces errorMessage on repo failure`() = runTest {
        fakeRepo.carrierLocationResult = Result.failure(Exception("Network down"))

        viewModel.refreshLocation(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingLocation)
        assertEquals("Network down", state.errorMessage)
    }

    @Test
    fun `refreshLocation keeps prior totalDistance once captured`() = runTest {
        viewModel.overrideClock(fixedClock("2026-05-13T10:30:00Z"))
        // First refresh — total distance is captured (max(remaining, 5.0))
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = 14.5995, carrierLng = 120.9842,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                lastUpdatedAt = "2026-05-13T10:29:00Z",
                isStale = false,
            ),
        )
        viewModel.refreshLocation(100)
        advanceUntilIdle()
        val initialTotal = viewModel.uiState.value.totalDistanceKm

        // Second refresh — carrier closer to delivery; total should NOT shrink
        fakeRepo.carrierLocationResult = Result.success(
            snapshot(
                carrierLat = 14.6500, carrierLng = 121.0400,
                deliveryLat = 14.6760, deliveryLng = 121.0437,
                lastUpdatedAt = "2026-05-13T10:30:00Z",
                isStale = false,
            ),
        )
        viewModel.refreshLocation(100)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(initialTotal, state.totalDistanceKm, 0.001)
        assertTrue("progress should be > 0 after carrier moves closer",
            state.deliveryProgress > 0.0)
    }

    private fun snapshot(
        carrierLat: Double?,
        carrierLng: Double?,
        deliveryLat: Double?,
        deliveryLng: Double?,
        lastUpdatedAt: String?,
        isStale: Boolean?,
    ) = CarrierLocationSnapshot(
        matchId = 100,
        carrier = null,
        carrierLat = carrierLat,
        carrierLng = carrierLng,
        lastUpdatedAt = lastUpdatedAt,
        isStale = isStale,
        deliveryAddress = null,
        deliveryCity = null,
        deliveryLat = deliveryLat,
        deliveryLng = deliveryLng,
        matchStatus = MatchStatus.IN_TRANSIT,
    )

    private fun fixedClock(iso: String): Clock =
        Clock.fixed(Instant.parse(iso), ZoneOffset.UTC)

    private fun testMatch(id: Int, status: MatchStatus) = DeliveryMatch(
        id = id, tripId = 1, packageRequestId = 10,
        matchStatus = status, agreedPrice = 150.0,
        initiatedBy = InitiatedBy.CARRIER,
        isCounterOffer = false, originalPrice = null,
        canCounterOffer = false, remainingCounterOffers = 0, counterOfferRound = 0,
        carrierMessage = null, shipperMessage = null,
        carrier = null, shipper = null, chatConversationId = null,
        confirmedAt = null, pickedUpAt = null, deliveredAt = null,
        createdAt = null, updatedAt = null,
        platformFeePercent = null, transactionStatus = null,
        receiptPhoto = null, autoCancelAfterDays = 10,
        pickupConfirmationCode = null, codeExpiresAt = null,
        deliveryVerificationCode = null, deliveryCodeExpiresAt = null,
    )
}
