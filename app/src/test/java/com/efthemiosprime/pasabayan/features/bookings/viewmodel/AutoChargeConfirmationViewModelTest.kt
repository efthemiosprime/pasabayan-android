package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.AutoChargeInfo
import com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.ConfirmMatchResult
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.services.PaymentMethodsRepository
import com.efthemiosprime.pasabayan.core.network.payments.SetupIntentDataJson
import kotlinx.coroutines.flow.first
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
class AutoChargeConfirmationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bookings: FakeBookingsRepo
    private lateinit var paymentMethods: FakePaymentMethodsRepo
    private lateinit var viewModel: AutoChargeConfirmationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        bookings = FakeBookingsRepo()
        paymentMethods = FakePaymentMethodsRepo()
        viewModel = AutoChargeConfirmationViewModel(bookings, paymentMethods)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- prepareConfirmation --

    @Test
    fun `prepareConfirmation with default PM transitions to ReadyToConfirm`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("expected ReadyToConfirm, got $state", state is AutoChargeConfirmationState.ReadyToConfirm)
        val ready = state as AutoChargeConfirmationState.ReadyToConfirm
        assertEquals(150.0, ready.price, 0.001)
        assertEquals("4242", ready.cardLast4)
    }

    @Test
    fun `prepareConfirmation without default PM transitions to NeedsPaymentMethod`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        assertEquals(AutoChargeConfirmationState.NeedsPaymentMethod(price = 150.0), viewModel.uiState.value)
    }

    @Test
    fun `prepareConfirmation surfaces Error on repo failure`() = runTest {
        paymentMethods.methodsResult = Result.failure(Exception("boom"))

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AutoChargeConfirmationState.Error)
    }

    // -- confirmMatch --

    @Test
    fun `confirmMatch success with queued auto-charge transitions to Success(true)`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )
        bookings.confirmMatchResult = Result.success(
            ConfirmMatchResult(
                match = testMatch(100, MatchStatus.CONFIRMED),
                autoCharge = AutoChargeInfo(queued = true, shipperHasDefaultPaymentMethod = true),
                chatConversationId = 77,
            ),
        )

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.confirmMatch()
        advanceUntilIdle()

        assertEquals(AutoChargeConfirmationState.Success(autoChargeQueued = true), viewModel.uiState.value)
    }

    @Test
    fun `confirmMatch success without default PM transitions to NeedsPaymentMethodAfterConfirm`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )
        bookings.confirmMatchResult = Result.success(
            ConfirmMatchResult(
                match = testMatch(100, MatchStatus.CONFIRMED),
                autoCharge = AutoChargeInfo(queued = false, shipperHasDefaultPaymentMethod = false),
                chatConversationId = null,
            ),
        )

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.confirmMatch()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm)
    }

    @Test
    fun `confirmMatch failure transitions to Error`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )
        bookings.confirmMatchResult = Result.failure(Exception("server is grumpy"))

        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.confirmMatch()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AutoChargeConfirmationState.Error)
    }

    // -- add payment method --

    @Test
    fun `startAddingPaymentMethod transitions to AddingPaymentMethod`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        viewModel.startAddingPaymentMethod()

        assertTrue(viewModel.uiState.value is AutoChargeConfirmationState.AddingPaymentMethod)
    }

    @Test
    fun `startAddingPaymentMethod emits client secret on setup-intent success`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())
        paymentMethods.setupIntentResult = Result.success(SetupIntentDataJson(clientSecret = "seti_secret_abc"))
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        viewModel.startAddingPaymentMethod()
        advanceUntilIdle()

        // Collect once — Channel-backed flow holds the emission until consumed.
        val secret = viewModel.launchPaymentSheet.first()
        assertEquals("seti_secret_abc", secret)
    }

    @Test
    fun `startAddingPaymentMethod transitions to Error when setup-intent fails`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())
        paymentMethods.setupIntentResult = Result.failure(Exception("stripe is down"))
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        viewModel.startAddingPaymentMethod()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AutoChargeConfirmationState.Error)
    }

    @Test
    fun `onPaymentMethodAdded pre-confirm re-checks and transitions to ReadyToConfirm`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.startAddingPaymentMethod()
        // Simulate Stripe success — repo now returns a default card.
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_2", last4 = "9999", isDefault = true)),
        )

        viewModel.onPaymentMethodAdded()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AutoChargeConfirmationState.ReadyToConfirm)
        assertEquals("9999", (state as AutoChargeConfirmationState.ReadyToConfirm).cardLast4)
    }

    @Test
    fun `onPaymentMethodAdded post-confirm transitions to Success when retry queues charge`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )
        bookings.confirmMatchResult = Result.success(
            ConfirmMatchResult(
                match = testMatch(100, MatchStatus.CONFIRMED),
                autoCharge = AutoChargeInfo(queued = false, shipperHasDefaultPaymentMethod = false),
                chatConversationId = null,
            ),
        )
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.confirmMatch()
        advanceUntilIdle()
        viewModel.startAddingPaymentMethod()
        bookings.retryAutoChargeResult = Result.success(Unit)

        viewModel.onPaymentMethodAdded()
        advanceUntilIdle()

        assertEquals(AutoChargeConfirmationState.Success(autoChargeQueued = true), viewModel.uiState.value)
    }

    @Test
    fun `onPaymentMethodCancelled pre-confirm returns to NeedsPaymentMethod`() = runTest {
        paymentMethods.methodsResult = Result.success(emptyList())
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()
        viewModel.startAddingPaymentMethod()

        viewModel.onPaymentMethodCancelled()

        assertEquals(AutoChargeConfirmationState.NeedsPaymentMethod(price = 150.0), viewModel.uiState.value)
    }

    @Test
    fun `dismiss returns to Idle`() = runTest {
        paymentMethods.methodsResult = Result.success(
            listOf(card(id = "pm_1", last4 = "4242", isDefault = true)),
        )
        viewModel.prepareConfirmation(matchId = 100, price = 150.0)
        advanceUntilIdle()

        viewModel.dismiss()

        assertEquals(AutoChargeConfirmationState.Idle, viewModel.uiState.value)
    }

    // -- helpers --

    private fun card(id: String, last4: String, isDefault: Boolean) = PaymentMethodDisplay(
        id = id, brand = "visa", last4 = last4, expMonth = 12, expYear = 2030, isDefault = isDefault,
    )

    private fun testMatch(
        id: Int,
        status: MatchStatus,
    ): DeliveryMatch = DeliveryMatch(
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

private class FakeBookingsRepo : BookingsRepository {
    var confirmMatchResult: Result<ConfirmMatchResult>? = null
    var retryAutoChargeResult: Result<Unit>? = null

    override suspend fun loadMatches(role: String?, status: String?) = Result.success(emptyList<DeliveryMatch>())
    override suspend fun getMatch(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmMatch(matchId: Int): Result<ConfirmMatchResult> =
        confirmMatchResult ?: Result.failure(Exception("Not set"))
    override suspend fun cancelMatch(matchId: Int): Result<CancelMatchResult> = Result.failure(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperAcceptCarrierRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int, acknowledgeOverage: Boolean?) =
        Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int) = Result.failure<String>(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String) = Result.failure<DeliveryMatch>(Exception("Not used"))
    override suspend fun retryAutoCharge(matchId: Int): Result<Unit> =
        retryAutoChargeResult ?: Result.failure(Exception("Not set"))
    override suspend fun shipperRequestTrip(
        packageId: Int, tripId: Int, offeredPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ): Result<RequestMatchResult> = Result.failure(Exception("Not used"))
    override suspend fun carrierRequestPackage(
        tripId: Int, packageId: Int, proposedPrice: Double, message: String?,
        isCounterOffer: Boolean, originalMatchId: Int?, originalPrice: Double?,
    ): Result<RequestMatchResult> = Result.failure(Exception("Not used"))
}

private class FakePaymentMethodsRepo : PaymentMethodsRepository {
    var methodsResult: Result<List<PaymentMethodDisplay>> = Result.success(emptyList())

    var setupIntentResult: Result<SetupIntentDataJson> =
        Result.success(SetupIntentDataJson(clientSecret = "seti_secret_default"))

    override suspend fun loadPaymentMethods(): Result<List<PaymentMethodDisplay>> = methodsResult
    override suspend fun loadDefaultPaymentMethod(): Result<String?> = Result.failure(Exception("Not used"))
    override suspend fun createSetupIntent(): Result<SetupIntentDataJson> = setupIntentResult
    override suspend fun removePaymentMethod(methodId: String): Result<Unit> = Result.failure(Exception("Not used"))
    override suspend fun setDefaultPaymentMethod(methodId: String): Result<Unit> = Result.failure(Exception("Not used"))
}
