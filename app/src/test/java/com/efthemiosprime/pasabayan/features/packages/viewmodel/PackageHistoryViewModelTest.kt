package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
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
class PackageHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeBookingsRepository: FakeBookingsRepositoryForHistory
    private lateinit var viewModel: PackageHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        every { context.getString(R.string.packages_history_error_load) } returns "Failed to load package history. Please try again."
        fakeBookingsRepository = FakeBookingsRepositoryForHistory()
        viewModel = PackageHistoryViewModel(context, fakeBookingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHistory queries shipper-delivered matches and exposes them`() = runTest {
        fakeBookingsRepository.loadMatchesResult = Result.success(
            listOf(
                match(id = 1, status = MatchStatus.DELIVERED, agreedPrice = 75.0),
                match(id = 2, status = MatchStatus.DELIVERED, agreedPrice = 120.5),
            ),
        )

        viewModel.loadHistory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.matches.size)
        assertEquals(195.5, state.totalSpent, 0.001)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf("shipper" to "delivered"), fakeBookingsRepository.loadMatchesCalls)
    }

    @Test
    fun `loadHistory filters out non-delivered statuses defensively`() = runTest {
        fakeBookingsRepository.loadMatchesResult = Result.success(
            listOf(
                match(id = 1, status = MatchStatus.DELIVERED, agreedPrice = 50.0),
                match(id = 2, status = MatchStatus.PICKED_UP, agreedPrice = 60.0),
            ),
        )

        viewModel.loadHistory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.matches.size)
        assertEquals(MatchStatus.DELIVERED, state.matches.first().matchStatus)
        assertEquals(50.0, state.totalSpent, 0.001)
    }

    @Test
    fun `loadHistory surfaces error message on failure`() = runTest {
        fakeBookingsRepository.loadMatchesResult = Result.failure(RuntimeException("network down"))

        viewModel.loadHistory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("network down", state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded)
    }

    @Test
    fun `loadHistory is no-op when already loaded and not forced`() = runTest {
        fakeBookingsRepository.loadMatchesResult = Result.success(emptyList())

        viewModel.loadHistory()
        advanceUntilIdle()
        viewModel.loadHistory()
        advanceUntilIdle()

        assertEquals(1, fakeBookingsRepository.loadMatchesCalls.size)
    }

    @Test
    fun `refresh re-runs the load even after a prior load`() = runTest {
        fakeBookingsRepository.loadMatchesResult = Result.success(emptyList())

        viewModel.loadHistory()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, fakeBookingsRepository.loadMatchesCalls.size)
    }

    private fun match(
        id: Int,
        status: MatchStatus,
        agreedPrice: Double,
    ): DeliveryMatch = DeliveryMatch(
        id = id,
        tripId = 100 + id,
        packageRequestId = 200 + id,
        matchStatus = status,
        agreedPrice = agreedPrice,
        initiatedBy = InitiatedBy.SHIPPER,
        isCounterOffer = false,
        originalPrice = null,
        canCounterOffer = false,
        remainingCounterOffers = 0,
        counterOfferRound = null,
        carrierMessage = null,
        shipperMessage = null,
        carrier = null,
        shipper = null,
        chatConversationId = null,
        confirmedAt = null,
        pickedUpAt = null,
        deliveredAt = "2026-04-10T12:00:00Z",
        createdAt = null,
        updatedAt = null,
        platformFeePercent = null,
        transactionStatus = "completed",
        receiptPhoto = null,
        autoCancelAfterDays = null,
        pickupConfirmationCode = null,
        codeExpiresAt = null,
        deliveryVerificationCode = null,
        deliveryCodeExpiresAt = null,
    )
}

/**
 * Minimal `BookingsRepository` stub — only `loadMatches` is exercised by the
 * history VM. Mockk can't mock the inline-value `Result` return type
 * cleanly, so we hand-roll the test fake instead.
 */
private class FakeBookingsRepositoryForHistory : BookingsRepository {
    var loadMatchesResult: Result<List<DeliveryMatch>> = Result.success(emptyList())
    val loadMatchesCalls: MutableList<Pair<String?, String?>> = mutableListOf()

    override suspend fun loadMatches(role: String?, status: String?): Result<List<DeliveryMatch>> {
        loadMatchesCalls += role to status
        return loadMatchesResult
    }

    override suspend fun getMatch(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun confirmMatch(matchId: Int):
        Result<com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult> =
        Result.failure(Exception("Not used"))
    override suspend fun cancelMatch(matchId: Int):
        Result<com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult> =
        Result.failure(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun shipperAcceptCarrierRequest(
        matchId: Int,
        acknowledgeOverage: Boolean?,
    ): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(
        matchId: Int,
        acknowledgeOverage: Boolean?,
    ): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int): Result<String> =
        Result.failure(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int): Result<String> =
        Result.failure(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun retryAutoCharge(matchId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
    override suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult> =
        Result.failure(Exception("Not used"))
    override suspend fun carrierRequestPackage(
        tripId: Int,
        packageId: Int,
        proposedPrice: Double,
        message: String?,
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult> =
        Result.failure(Exception("Not used"))
    override suspend fun getCarrierLocation(matchId: Int):
        Result<com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot> =
        Result.failure(Exception("Not used"))
    override suspend fun getCompatibleTrips(
        packageRequestId: Int,
        carrierId: Int?,
    ): Result<List<com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip>> =
        Result.failure(Exception("Not used"))
    override suspend fun getCompatiblePackages(tripId: Int):
        Result<List<com.efthemiosprime.pasabayan.features.packages.model.PackageRequest>> =
        Result.failure(Exception("Not used"))
    override suspend fun getReceiverAccess(matchId: Int):
        Result<List<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken>> =
        Result.failure(Exception("Not used"))
    override suspend fun createReceiverAccess(
        matchId: Int,
        generatePin: Boolean,
        receiverName: String?,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken> =
        Result.failure(Exception("Not used"))
    override suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit> =
        Result.failure(Exception("Not used"))
    override suspend fun bookTripDirect(
        tripId: Int,
        payload: com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData> =
        Result.failure(Exception("Not used"))
}
