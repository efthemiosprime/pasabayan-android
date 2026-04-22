package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
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
class MatchingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeBookingsRepository
    private lateinit var viewModel: MatchingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBookingsRepository()
        viewModel = MatchingViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMatches sets matches on success`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1), testMatch(2)))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.matches.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadMatches sets error on failure`() = runTest {
        fakeRepo.loadResult = Result.failure(Exception("Network error"))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `filterByStatus filters matches`() = runTest {
        fakeRepo.loadResult = Result.success(
            listOf(
                testMatch(1, MatchStatus.CONFIRMED),
                testMatch(2, MatchStatus.PENDING),
                testMatch(3, MatchStatus.DELIVERED),
            ),
        )
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.filterByStatus(MatchStatus.CONFIRMED)
        assertEquals(1, viewModel.uiState.value.filteredMatches.size)
        assertEquals(MatchStatus.CONFIRMED, viewModel.uiState.value.filteredMatches[0].matchStatus)
    }

    @Test
    fun `filterByStatus null shows all`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1), testMatch(2), testMatch(3)))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.filterByStatus(null)
        assertEquals(3, viewModel.uiState.value.filteredMatches.size)
    }

    @Test
    fun `acceptMatch calls shipperAccept and updates list`() = runTest {
        val accepted = testMatch(1, MatchStatus.CONFIRMED)
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.CARRIER_REQUESTED)))
        fakeRepo.shipperAcceptResult = Result.success(accepted)
        viewModel.loadMatches("shipper")
        advanceUntilIdle()

        viewModel.acceptMatch(1, isCarrier = false)
        advanceUntilIdle()

        assertEquals(MatchStatus.CONFIRMED, viewModel.uiState.value.matches[0].matchStatus)
    }

    @Test
    fun `cancelMatch removes from list on success`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1), testMatch(2)))
        fakeRepo.cancelResult = Result.success(Unit)
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.cancelMatch(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.matches.size)
        assertEquals(2, viewModel.uiState.value.matches[0].id)
    }

    @Test
    fun `cancelMatch sets error on failure`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1)))
        fakeRepo.cancelResult = Result.failure(Exception("Cannot cancel"))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.cancelMatch(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.matches.size)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `confirmMatch updates match status`() = runTest {
        val confirmed = testMatch(1, MatchStatus.CONFIRMED)
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.PENDING)))
        fakeRepo.confirmResult = Result.success(confirmed)
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.confirmMatch(1)
        advanceUntilIdle()

        assertEquals(MatchStatus.CONFIRMED, viewModel.uiState.value.matches[0].matchStatus)
    }

    @Test
    fun `clearError clears errorMessage`() = runTest {
        fakeRepo.loadResult = Result.failure(Exception("fail"))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private fun testMatch(
        id: Int,
        status: MatchStatus = MatchStatus.CONFIRMED,
    ) = DeliveryMatch(
        id = id, tripId = 1, packageRequestId = 10,
        matchStatus = status, agreedPrice = 150.0,
        initiatedBy = InitiatedBy.CARRIER,
        isCounterOffer = false, originalPrice = null,
        canCounterOffer = false, remainingCounterOffers = 0,
        counterOfferRound = 0,
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

class FakeBookingsRepository : BookingsRepository {
    var loadResult: Result<List<DeliveryMatch>> = Result.success(emptyList())
    var getResult: Result<DeliveryMatch>? = null
    var confirmResult: Result<DeliveryMatch>? = null
    var cancelResult: Result<Unit> = Result.success(Unit)
    var shipperAcceptResult: Result<DeliveryMatch>? = null
    var shipperDeclineResult: Result<DeliveryMatch>? = null
    var carrierAcceptResult: Result<DeliveryMatch>? = null
    var carrierDeclineResult: Result<DeliveryMatch>? = null

    override suspend fun loadMatches(role: String?, status: String?) = loadResult
    override suspend fun getMatch(matchId: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun confirmMatch(matchId: Int) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun cancelMatch(matchId: Int) = cancelResult
    override suspend fun markPickedUp(matchId: Int) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun markInTransit(matchId: Int) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun markDelivered(matchId: Int) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun shipperAccept(matchId: Int) = shipperAcceptResult ?: Result.failure(Exception("Not set"))
    override suspend fun shipperDecline(matchId: Int) = shipperDeclineResult ?: Result.failure(Exception("Not set"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int) = carrierAcceptResult ?: Result.failure(Exception("Not set"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int) = carrierDeclineResult ?: Result.failure(Exception("Not set"))
    override suspend fun generatePickupCode(matchId: Int) = Result.success("123456")
    override suspend fun generateDeliveryCode(matchId: Int) = Result.success("654321")
    override suspend fun confirmPickupWithCode(matchId: Int, code: String) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String) = confirmResult ?: Result.failure(Exception("Not set"))
    override suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
    ) = Result.failure<DeliveryMatch>(Exception("Not set"))
}
