package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.NegotiationMetadata
import com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeBookingsRepository
    private lateinit var requirePhoneVerification: RequirePhoneVerificationUseCase
    private lateinit var authRepository: AuthRepository
    private lateinit var currentUserFlow: MutableStateFlow<AuthUser?>
    private lateinit var viewModel: MatchingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBookingsRepository()
        requirePhoneVerification = mockk()
        every { requirePhoneVerification.invoke() } returns Result.success(Unit)
        currentUserFlow = MutableStateFlow(null)
        authRepository = mockk()
        every { authRepository.currentUser() } returns currentUserFlow
        viewModel = MatchingViewModel(fakeRepo, requirePhoneVerification, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `currentUserId mirrors AuthRepository currentUser updates`() = runTest {
        // Initial null
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.currentUserId)

        // Emit a signed-in user
        currentUserFlow.value = AuthUser(
            id = 7L,
            name = "Tester",
            email = "t@x",
            avatar = null,
            phone = null,
            phoneVerified = true,
            profileCompleted = true,
            provider = "test",
            userTypes = listOf("shipper"),
            isActiveCarrier = false,
            isActiveShipper = true,
        )
        advanceUntilIdle()
        assertEquals(7L, viewModel.uiState.value.currentUserId)

        // Sign out
        currentUserFlow.value = null
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.currentUserId)
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
    fun `cancelMatch replaces match with cancelled version on success`() = runTest {
        val cancelled = testMatch(1, MatchStatus.CANCELLED)
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.CONFIRMED), testMatch(2)))
        fakeRepo.cancelResult = Result.success(
            CancelMatchResult(match = cancelled, chatConversationId = 77, refund = null),
        )
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.cancelMatch(1)
        advanceUntilIdle()

        val matches = viewModel.uiState.value.matches
        assertEquals(2, matches.size)
        assertEquals(MatchStatus.CANCELLED, matches.first { it.id == 1 }.matchStatus)
    }

    @Test
    fun `cancelMatch surfaces refund result when refund processed`() = runTest {
        val cancelled = testMatch(1, MatchStatus.CANCELLED)
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.CONFIRMED)))
        fakeRepo.cancelResult = Result.success(
            CancelMatchResult(
                match = cancelled,
                chatConversationId = 77,
                refund = RefundResult(processed = true, amount = 150.50, transactionId = 555, error = null),
            ),
        )
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.cancelMatch(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.lastCancelRefund?.processed == true)
        assertEquals(150.50, state.lastCancelRefund?.amount!!, 0.001)
        assertEquals(77, state.lastCancelConversationId)
    }

    @Test
    fun `clearCancelArtifacts wipes refund and conversation`() = runTest {
        val cancelled = testMatch(1, MatchStatus.CANCELLED)
        fakeRepo.loadResult = Result.success(listOf(testMatch(1)))
        fakeRepo.cancelResult = Result.success(
            CancelMatchResult(
                match = cancelled,
                chatConversationId = 77,
                refund = RefundResult(processed = true, amount = 150.50, transactionId = 555, error = null),
            ),
        )
        viewModel.loadMatches("carrier")
        advanceUntilIdle()
        viewModel.cancelMatch(1)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.lastCancelRefund)

        viewModel.clearCancelArtifacts()
        assertNull(viewModel.uiState.value.lastCancelRefund)
        assertNull(viewModel.uiState.value.lastCancelConversationId)
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

    // -- submitCounterOffer (iOS parity 8c9646d) --

    @Test
    fun `submitCounterOffer (shipper) replaces match and surfaces negotiation`() = runTest {
        val original = testMatch(1, MatchStatus.CARRIER_REQUESTED)
        val counterOffered = testMatch(1, MatchStatus.SHIPPER_REQUESTED)
        fakeRepo.loadResult = Result.success(listOf(original))
        fakeRepo.shipperRequestResult = Result.success(
            RequestMatchResult(
                match = counterOffered,
                negotiation = NegotiationMetadata(
                    warnings = emptyList(),
                    negotiationNeeded = false,
                    isCounterOffer = true,
                ),
            ),
        )
        viewModel.loadMatches("shipper")
        advanceUntilIdle()

        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 135.0, message = "Lower?", isShipper = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(MatchStatus.SHIPPER_REQUESTED, state.matches.first { it.id == 1 }.matchStatus)
        assertTrue(state.lastNegotiation?.isCounterOffer == true)
        assertFalse(state.isSubmittingCounterOffer)
    }

    @Test
    fun `submitCounterOffer (carrier) surfaces warnings list`() = runTest {
        val original = testMatch(1, MatchStatus.SHIPPER_REQUESTED)
        val counterOffered = testMatch(1, MatchStatus.CARRIER_REQUESTED)
        fakeRepo.loadResult = Result.success(listOf(original))
        fakeRepo.carrierRequestResult = Result.success(
            RequestMatchResult(
                match = counterOffered,
                negotiation = NegotiationMetadata(
                    warnings = listOf("pickup_address_outside_range"),
                    negotiationNeeded = true,
                    isCounterOffer = true,
                ),
            ),
        )
        viewModel.loadMatches("carrier")
        advanceUntilIdle()

        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 160.0, message = "Above your offer", isShipper = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("pickup_address_outside_range"), state.lastNegotiation?.warnings)
        assertTrue(state.lastNegotiation?.negotiationNeeded == true)
    }

    @Test
    fun `submitCounterOffer sets error when match is unknown`() = runTest {
        fakeRepo.loadResult = Result.success(emptyList())
        viewModel.loadMatches("shipper")
        advanceUntilIdle()

        viewModel.submitCounterOffer(matchId = 999, proposedPrice = 100.0, message = null, isShipper = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `submitCounterOffer sets error on repository failure`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1)))
        fakeRepo.shipperRequestResult = Result.failure(Exception("Negotiation closed"))
        viewModel.loadMatches("shipper")
        advanceUntilIdle()

        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 100.0, message = null, isShipper = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.errorMessage != null)
        assertFalse(state.isSubmittingCounterOffer)
    }

    // -- phone verification gate --

    @Test
    fun `submitCounterOffer (shipper) blocked when phone not verified emits BookTrip gate`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.CARRIER_REQUESTED)))
        viewModel.loadMatches("shipper")
        advanceUntilIdle()
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 100.0, message = null, isShipper = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.BookTrip, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingCounterOffer)
    }

    @Test
    fun `submitCounterOffer (carrier) blocked when phone not verified emits RequestToCarry gate`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1, MatchStatus.SHIPPER_REQUESTED)))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 100.0, message = null, isShipper = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.RequestToCarry, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingCounterOffer)
    }

    @Test
    fun `requestPackageAsCarrier blocked when phone not verified does not call repo`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.requestPackageAsCarrier(
            tripId = 7,
            packageId = 9,
            proposedPrice = 50.0,
            message = "Please",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.RequestToCarry, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingCounterOffer)
    }

    @Test
    fun `requestPackageAsCarrier success appends match and surfaces negotiation`() = runTest {
        val newMatch = testMatch(99, MatchStatus.CARRIER_REQUESTED)
        fakeRepo.carrierRequestResult = Result.success(
            RequestMatchResult(
                match = newMatch,
                negotiation = NegotiationMetadata(
                    warnings = emptyList(),
                    negotiationNeeded = false,
                    isCounterOffer = false,
                ),
            ),
        )

        viewModel.requestPackageAsCarrier(
            tripId = 7,
            packageId = 9,
            proposedPrice = 50.0,
            message = "Please",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.matches.any { it.id == 99 })
        assertNotNull(state.lastNegotiation)
        assertFalse(state.isSubmittingCounterOffer)
    }

    @Test
    fun `consumeRequiresPhoneVerification clears the gate flag`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)
        viewModel.requestPackageAsCarrier(tripId = 1, packageId = 2, proposedPrice = 10.0, message = null)
        advanceUntilIdle()
        assertEquals(VerifyPhoneReason.RequestToCarry, viewModel.uiState.value.requiresPhoneVerification)

        viewModel.consumeRequiresPhoneVerification()

        assertNull(viewModel.uiState.value.requiresPhoneVerification)
    }

    @Test
    fun `clearNegotiationArtifacts wipes lastNegotiation`() = runTest {
        val original = testMatch(1, MatchStatus.CARRIER_REQUESTED)
        fakeRepo.loadResult = Result.success(listOf(original))
        fakeRepo.shipperRequestResult = Result.success(
            RequestMatchResult(
                match = testMatch(1, MatchStatus.SHIPPER_REQUESTED),
                negotiation = NegotiationMetadata(
                    warnings = listOf("over_capacity"),
                    negotiationNeeded = false,
                    isCounterOffer = true,
                ),
            ),
        )
        viewModel.loadMatches("shipper")
        advanceUntilIdle()
        viewModel.submitCounterOffer(matchId = 1, proposedPrice = 100.0, message = null, isShipper = true)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.lastNegotiation)

        viewModel.clearNegotiationArtifacts()
        assertNull(viewModel.uiState.value.lastNegotiation)
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

    // -- IncomingRequestSnackbar review state --

    @Test
    fun `initial state has empty reviewedIncomingRequestIds`() {
        assertTrue(viewModel.uiState.value.reviewedIncomingRequestIds.isEmpty())
    }

    @Test
    fun `markIncomingRequestReviewed adds matchId to the set`() {
        viewModel.markIncomingRequestReviewed(matchId = 7)

        assertEquals(setOf(7), viewModel.uiState.value.reviewedIncomingRequestIds)
    }

    @Test
    fun `markIncomingRequestReviewed is idempotent`() {
        viewModel.markIncomingRequestReviewed(matchId = 7)
        viewModel.markIncomingRequestReviewed(matchId = 7)

        assertEquals(setOf(7), viewModel.uiState.value.reviewedIncomingRequestIds)
    }

    @Test
    fun `markIncomingRequestReviewed accumulates ids across calls`() {
        viewModel.markIncomingRequestReviewed(matchId = 7)
        viewModel.markIncomingRequestReviewed(matchId = 12)
        viewModel.markIncomingRequestReviewed(matchId = 99)

        assertEquals(setOf(7, 12, 99), viewModel.uiState.value.reviewedIncomingRequestIds)
    }

    @Test
    fun `markIncomingRequestReviewed preserves other state fields`() {
        fakeRepo.loadResult = Result.success(listOf(testMatch(1)))
        viewModel.loadMatches("carrier")
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.markIncomingRequestReviewed(matchId = 1)

        val after = viewModel.uiState.value
        assertEquals(before.matches, after.matches)
        assertEquals(before.isLoading, after.isLoading)
        assertEquals(before.errorMessage, after.errorMessage)
        assertEquals(before.statusFilter, after.statusFilter)
        assertEquals(setOf(1), after.reviewedIncomingRequestIds)
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
    var cancelResult: Result<CancelMatchResult> = Result.failure(Exception("Not set"))
    var shipperAcceptResult: Result<DeliveryMatch>? = null
    var shipperDeclineResult: Result<DeliveryMatch>? = null
    var carrierAcceptResult: Result<DeliveryMatch>? = null
    var carrierDeclineResult: Result<DeliveryMatch>? = null
    var shipperRequestResult: Result<RequestMatchResult> = Result.failure(Exception("Not set"))
    var carrierRequestResult: Result<RequestMatchResult> = Result.failure(Exception("Not set"))

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
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ) = shipperRequestResult

    override suspend fun carrierRequestPackage(
        tripId: Int,
        packageId: Int,
        proposedPrice: Double,
        message: String?,
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ) = carrierRequestResult
}
