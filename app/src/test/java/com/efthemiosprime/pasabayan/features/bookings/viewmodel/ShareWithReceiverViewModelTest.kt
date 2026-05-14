package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.ReceiverAccessToken
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareWithReceiverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeRepo: FakeBookingsRepositoryForShareWithReceiver
    private lateinit var viewModel: ShareWithReceiverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        every { context.getString(R.string.bookings_share_with_receiver_error_generic) } returns
            "Failed to share. Try again."
        fakeRepo = FakeBookingsRepositoryForShareWithReceiver()
        viewModel = ShareWithReceiverViewModel(context, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start with no existing tokens creates fresh token`() = runTest {
        fakeRepo.receiverAccessResult = Result.success(emptyList())
        fakeRepo.createReceiverAccessResult = Result.success(token(id = 10, pin = "1234"))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShareWithReceiverState.Success)
        assertEquals(10, (state as ShareWithReceiverState.Success).token.id)
        assertEquals(listOf(7), fakeRepo.receiverAccessCalls)
        assertEquals(listOf(7 to true), fakeRepo.createCalls.map { it.first to it.second })
        assertTrue(fakeRepo.revokeCalls.isEmpty())
    }

    @Test
    fun `start revokes all active existing tokens before creating new one`() = runTest {
        fakeRepo.receiverAccessResult = Result.success(
            listOf(
                token(id = 1, isActive = true),
                token(id = 2, isActive = false),
                token(id = 3, isActive = true),
            ),
        )
        fakeRepo.revokeResult = Result.success(Unit)
        fakeRepo.createReceiverAccessResult = Result.success(token(id = 99))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ShareWithReceiverState.Success)
        // Only active tokens are revoked.
        assertEquals(setOf(7 to 1, 7 to 3), fakeRepo.revokeCalls.toSet())
        assertEquals(1, fakeRepo.createCalls.size)
    }

    @Test
    fun `start surfaces error when initial list fetch fails`() = runTest {
        fakeRepo.receiverAccessResult = Result.failure(RuntimeException("offline"))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShareWithReceiverState.Error)
        assertEquals("offline", (state as ShareWithReceiverState.Error).message)
        assertTrue(fakeRepo.createCalls.isEmpty())
        assertTrue(fakeRepo.revokeCalls.isEmpty())
    }

    @Test
    fun `start surfaces fallback message when error has no message`() = runTest {
        fakeRepo.receiverAccessResult = Result.failure(RuntimeException(null as String?))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            "Failed to share. Try again.",
            (state as ShareWithReceiverState.Error).message,
        )
    }

    @Test
    fun `start surfaces error when create fails`() = runTest {
        fakeRepo.receiverAccessResult = Result.success(emptyList())
        fakeRepo.createReceiverAccessResult = Result.failure(RuntimeException("rate limited"))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShareWithReceiverState.Error)
        assertEquals("rate limited", (state as ShareWithReceiverState.Error).message)
    }

    @Test
    fun `start swallows revoke failures and still creates new token`() = runTest {
        fakeRepo.receiverAccessResult = Result.success(listOf(token(id = 1, isActive = true)))
        fakeRepo.revokeResult = Result.failure(RuntimeException("stale token"))
        fakeRepo.createReceiverAccessResult = Result.success(token(id = 99))

        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(
            "Revoke failures should not block create — iOS catches them silently",
            state is ShareWithReceiverState.Success,
        )
        assertEquals(1, fakeRepo.createCalls.size)
    }

    @Test
    fun `start is idempotent while a request is already in flight`() = runTest {
        fakeRepo.receiverAccessResult = Result.success(emptyList())
        fakeRepo.createReceiverAccessResult = Result.success(token(id = 1))

        viewModel.start(matchId = 7)
        viewModel.start(matchId = 7) // second call while in flight
        advanceUntilIdle()

        // Only one chain ran end-to-end.
        assertEquals(1, fakeRepo.receiverAccessCalls.size)
        assertEquals(1, fakeRepo.createCalls.size)
    }

    @Test
    fun `start can be retried after an error settles`() = runTest {
        // First attempt fails.
        fakeRepo.receiverAccessResult = Result.failure(RuntimeException("timeout"))
        viewModel.start(matchId = 7)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ShareWithReceiverState.Error)

        // Configure success, retry.
        fakeRepo.receiverAccessResult = Result.success(emptyList())
        fakeRepo.createReceiverAccessResult = Result.success(token(id = 42))
        viewModel.start(matchId = 7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShareWithReceiverState.Success)
        assertEquals(42, (state as ShareWithReceiverState.Success).token.id)
    }

    private fun token(
        id: Int,
        pin: String? = "1234",
        isActive: Boolean = true,
    ): ReceiverAccessToken = ReceiverAccessToken(
        id = id,
        shortCode = "CODE$id",
        shortUrl = "https://psb.to/CODE$id",
        hasPin = pin != null,
        pin = pin,
        pinNotice = null,
        isActive = isActive,
        accessCount = 0,
        trackingUrl = "https://app.pasabayan.com/track/CODE$id",
        firstAccessedAt = null,
        lastAccessedAt = null,
        createdAt = "2026-05-14T10:00:00Z",
    )
}

private class FakeBookingsRepositoryForShareWithReceiver : BookingsRepository {
    var receiverAccessResult: Result<List<ReceiverAccessToken>> = Result.success(emptyList())
    var revokeResult: Result<Unit> = Result.success(Unit)
    var createReceiverAccessResult: Result<ReceiverAccessToken> =
        Result.failure(Exception("Not set"))

    val receiverAccessCalls: MutableList<Int> = mutableListOf()
    val revokeCalls: MutableList<Pair<Int, Int>> = mutableListOf()
    /** matchId, generatePin, receiverName */
    val createCalls: MutableList<Triple<Int, Boolean, String?>> = mutableListOf()

    override suspend fun getReceiverAccess(matchId: Int): Result<List<ReceiverAccessToken>> {
        receiverAccessCalls += matchId
        return receiverAccessResult
    }

    override suspend fun revokeReceiverAccess(matchId: Int, tokenId: Int): Result<Unit> {
        revokeCalls += matchId to tokenId
        return revokeResult
    }

    override suspend fun createReceiverAccess(
        matchId: Int,
        generatePin: Boolean,
        receiverName: String?,
    ): Result<ReceiverAccessToken> {
        createCalls += Triple(matchId, generatePin, receiverName)
        return createReceiverAccessResult
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
    override suspend fun getCompatibleTrips(packageRequestId: Int, carrierId: Int?) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip>>(Exception("Not used"))
    override suspend fun getCompatiblePackages(tripId: Int) =
        Result.failure<List<com.efthemiosprime.pasabayan.features.packages.model.PackageRequest>>(Exception("Not used"))
    override suspend fun bookTripDirect(
        tripId: Int,
        payload: com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload,
    ) = Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData>(Exception("Not used"))
    override suspend fun submitRating(matchId: Int, rating: Int, reviewText: String?) =
        Result.failure<com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch>(Exception("Not used"))
}
