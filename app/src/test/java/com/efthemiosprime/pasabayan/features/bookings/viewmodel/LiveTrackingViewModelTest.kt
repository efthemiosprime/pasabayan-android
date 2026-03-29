package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
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
    }

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
