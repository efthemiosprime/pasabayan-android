package com.efthemiosprime.pasabayan.features.payments.viewmodel

import com.efthemiosprime.pasabayan.core.network.payments.CreatePaymentResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import com.efthemiosprime.pasabayan.core.network.payments.SetupIntentDataJson
import com.efthemiosprime.pasabayan.core.network.payments.TipResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.model.toDomain
import com.efthemiosprime.pasabayan.features.payments.services.PaymentMethodsRepository
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import com.efthemiosprime.pasabayan.features.payments.services.ReceiptRepository
import com.efthemiosprime.pasabayan.features.payments.services.StripeConfigRepository
import com.efthemiosprime.pasabayan.features.payments.services.StripeConnectRepository
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
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
class PaymentViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePaymentRepo: FakePaymentRepository
    private lateinit var fakeMethodsRepo: FakePaymentMethodsRepository
    private lateinit var fakeStripeConfigRepo: FakeStripeConfigRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePaymentRepo = FakePaymentRepository()
        fakeMethodsRepo = FakePaymentMethodsRepository()
        fakeStripeConfigRepo = FakeStripeConfigRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- PaymentViewModel --

    @Test
    fun `createPayment sets clientSecret on success`() = runTest {
        fakePaymentRepo.createResult = Result.success(
            CreatePaymentResponseJson(
                success = true,
                clientSecret = "pi_secret_123",
                customerId = "cus_abc",
                data = TransactionJson(id = 1, status = "pending"),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.createPayment(100, 150.0)
        advanceUntilIdle()

        assertEquals("pi_secret_123", vm.uiState.value.clientSecret)
        assertTrue(vm.uiState.value.isSheetReady)
        assertEquals(PaymentFlowStatus.SHEET_READY, vm.uiState.value.flowStatus)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `createPayment sets error on failure`() = runTest {
        fakePaymentRepo.createResult = Result.failure(Exception("Payment failed"))
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.createPayment(100, 150.0)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `createPayment sets error when client secret is missing`() = runTest {
        fakePaymentRepo.createResult = Result.success(
            CreatePaymentResponseJson(
                success = true,
                clientSecret = null,
                data = TransactionJson(id = 1, status = "pending", clientSecret = null),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.createPayment(100, 150.0)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
        assertNull(vm.uiState.value.clientSecret)
    }

    @Test
    fun `confirmCapture sets paymentSuccess`() = runTest {
        fakePaymentRepo.confirmCaptureResult = Result.success(
            TransactionJson(id = 1, status = "completed").toDomain(),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.confirmCapture(100)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.paymentSuccess)
        assertEquals(PaymentFlowStatus.PAYMENT_SUCCESS, vm.uiState.value.flowStatus)
    }

    @Test
    fun `isMockClientSecret detects mock`() {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        assertTrue(vm.isMockClientSecret("mock_pi_abc123"))
        assertFalse(vm.isMockClientSecret("pi_real_abc123"))
    }

    @Test
    fun `createPayment marks already paid statuses`() {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)

        vm.createPayment(deliveryMatchId = 100, amount = 150.0, transactionStatus = "completed")

        assertEquals(PaymentFlowStatus.ALREADY_PAID, vm.uiState.value.flowStatus)
        assertFalse(vm.uiState.value.isSheetReady)
    }

    @Test
    fun `createPayment handles mock client secret without sheet`() = runTest {
        fakePaymentRepo.createResult = Result.success(
            CreatePaymentResponseJson(
                success = true,
                clientSecret = "mock_pi_local_test",
                data = TransactionJson(id = 2, status = "pending"),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)

        vm.createPayment(101, 155.0)
        advanceUntilIdle()

        assertEquals(PaymentFlowStatus.PAYMENT_SUCCESS, vm.uiState.value.flowStatus)
        assertFalse(vm.uiState.value.isSheetReady)
    }

    @Test
    fun `sheet canceled updates state`() {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)

        vm.onPaymentSheetCanceled()

        assertEquals(PaymentFlowStatus.PAYMENT_CANCELED, vm.uiState.value.flowStatus)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `sheet failed updates state and clears readiness`() {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)

        vm.onPaymentSheetFailed("stripe failure")

        assertEquals("stripe failure", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSheetReady)
    }

    // -- B2: PaymentSheet config + 3DS + polling --

    @Test
    fun `configurePaymentSheet stores config built from current state`() = runTest {
        fakePaymentRepo.createResult = Result.success(
            CreatePaymentResponseJson(
                success = true,
                clientSecret = "pi_secret_123",
                customerId = "cus_abc",
                ephemeralKey = "ek_xyz",
                data = TransactionJson(id = 1, status = "pending"),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.createPayment(100, 150.0)
        advanceUntilIdle()

        // createPayment with a real secret triggers configurePaymentSheet automatically.
        assertTrue(vm.uiState.value.paymentSheetConfig != null)
    }

    @Test
    fun `mock client secret skips configurePaymentSheet`() = runTest {
        fakePaymentRepo.createResult = Result.success(
            CreatePaymentResponseJson(
                success = true,
                clientSecret = "mock_pi_skip",
                data = TransactionJson(id = 1, status = "pending"),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.createPayment(100, 150.0)
        advanceUntilIdle()

        // Mock path short-circuits — no sheet config built.
        assertNull(vm.uiState.value.paymentSheetConfig)
    }

    @Test
    fun `handle3DSChallenge sets requires authentication and rotates secrets`() = runTest {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        advanceUntilIdle()

        vm.handle3DSChallenge(
            clientSecret = "pi_secret_3ds",
            customerId = "cus_new",
            ephemeralKey = "ek_new",
            publicKey = "pk_test_new",
            message = "Please complete verification",
        )

        val state = vm.uiState.value
        assertTrue(state.requiresAuthentication)
        assertEquals("pi_secret_3ds", state.authenticationClientSecret)
        assertEquals("pi_secret_3ds", state.clientSecret)
        assertEquals("cus_new", state.customerId)
        assertEquals("ek_new", state.ephemeralKey)
        assertEquals("pk_test_new", state.publicKey)
        assertTrue(state.isSheetReady)
        assertEquals(PaymentFlowStatus.SHEET_READY, state.flowStatus)
        assertEquals("Please complete verification", state.statusMessage)
        // Sheet is re-configured.
        assertTrue(state.paymentSheetConfig != null)
    }

    @Test
    fun `pollForConfirmation succeeds on first attempt`() = runTest {
        fakePaymentRepo.confirmCaptureResult = Result.success(
            TransactionJson(id = 1, status = "completed").toDomain(),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.pollForConfirmation(100, attempts = 3, delayMs = 100L)
        advanceUntilIdle()

        assertEquals(1, fakePaymentRepo.confirmCaptureCallCount)
        assertTrue(vm.uiState.value.paymentSuccess)
        assertEquals(PaymentFlowStatus.PAYMENT_SUCCESS, vm.uiState.value.flowStatus)
        assertEquals(0, vm.uiState.value.pollAttempt)
    }

    @Test
    fun `pollForConfirmation retries until success`() = runTest {
        fakePaymentRepo.confirmCaptureQueue.addAll(
            listOf(
                Result.failure(Exception("transient")),
                Result.failure(Exception("transient")),
                Result.success(TransactionJson(id = 1, status = "completed").toDomain()),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.pollForConfirmation(100, attempts = 3, delayMs = 50L)
        advanceUntilIdle()

        assertEquals(3, fakePaymentRepo.confirmCaptureCallCount)
        assertTrue(vm.uiState.value.paymentSuccess)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `pollForConfirmation gives up after N attempts`() = runTest {
        fakePaymentRepo.confirmCaptureQueue.addAll(
            listOf(
                Result.failure(Exception("err 1")),
                Result.failure(Exception("err 2")),
                Result.failure(Exception("err 3 final")),
            ),
        )
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.pollForConfirmation(100, attempts = 3, delayMs = 50L)
        advanceUntilIdle()

        assertEquals(3, fakePaymentRepo.confirmCaptureCallCount)
        assertFalse(vm.uiState.value.paymentSuccess)
        assertEquals("err 3 final", vm.uiState.value.errorMessage)
        assertEquals(0, vm.uiState.value.pollAttempt)
    }

    @Test
    fun `onStripeSDKError surfaces message and clears readiness`() = runTest {
        val vm = PaymentViewModel(fakePaymentRepo, fakeStripeConfigRepo)
        vm.onStripeSDKError("Card declined by issuer")

        assertEquals("Card declined by issuer", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSheetReady)
        assertEquals(PaymentFlowStatus.IDLE, vm.uiState.value.flowStatus)
    }

    // -- PaymentMethodsViewModel --

    @Test
    fun `loadPaymentMethods sets methods on success`() = runTest {
        fakeMethodsRepo.loadResult = Result.success(
            listOf(
                PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, false),
                PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
            ),
        )
        fakeMethodsRepo.defaultResult = Result.success("pm_2")
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.loadPaymentMethods()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.paymentMethods.size)
        assertEquals("pm_2", vm.uiState.value.defaultPaymentMethodId)
        assertEquals("pm_2", vm.uiState.value.paymentMethods.first().id)
        assertTrue(vm.uiState.value.paymentMethods.first().isDefault)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadPaymentMethods sets error on failure`() = runTest {
        fakeMethodsRepo.loadResult = Result.failure(Exception("Network error"))
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.loadPaymentMethods()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
    }

    @Test
    fun `removePaymentMethod removes from list`() = runTest {
        fakeMethodsRepo.loadResult = Result.success(
            listOf(
                PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, true),
                PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
            ),
        )
        fakeMethodsRepo.defaultResult = Result.success("pm_1")
        fakeMethodsRepo.removeResult = Result.success(Unit)
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.loadPaymentMethods()
        advanceUntilIdle()

        vm.removePaymentMethod("pm_1")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.paymentMethods.size)
        assertEquals("pm_2", vm.uiState.value.paymentMethods[0].id)
        assertNull(vm.uiState.value.defaultPaymentMethodId)
    }

    @Test
    fun `setDefaultPaymentMethod updates default`() = runTest {
        fakeMethodsRepo.loadResult = Result.success(
            listOf(
                PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, false),
                PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
            ),
        )
        fakeMethodsRepo.setDefaultResult = Result.success(Unit)
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.loadPaymentMethods()
        advanceUntilIdle()

        vm.setDefaultPaymentMethod("pm_2")
        advanceUntilIdle()

        assertEquals("pm_2", vm.uiState.value.defaultPaymentMethodId)
        assertEquals("pm_2", vm.uiState.value.paymentMethods.first().id)
        assertTrue(vm.uiState.value.paymentMethods.first().isDefault)
    }

    // -- Add-card flow (B1) --

    @Test
    fun `prepareAddPaymentMethod success transitions to Ready and stores secrets`() = runTest {
        fakeMethodsRepo.setupResult = Result.success(
            SetupIntentDataJson(
                clientSecret = "seti_secret_abc",
                customerId = "cus_123",
                ephemeralKey = "ek_456",
            ),
        )
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()

        assertEquals(AddCardFlowState.Ready, vm.uiState.value.addCardFlowState)
        assertEquals("seti_secret_abc", vm.uiState.value.setupIntentClientSecret)
        assertEquals("cus_123", vm.uiState.value.setupIntentCustomerId)
        assertEquals("ek_456", vm.uiState.value.setupIntentEphemeralKey)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `prepareAddPaymentMethod failure sets Failed and surfaces error`() = runTest {
        fakeMethodsRepo.setupResult = Result.failure(Exception("Stripe down"))
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()

        val state = vm.uiState.value.addCardFlowState
        assertTrue("expected Failed got $state", state is AddCardFlowState.Failed)
        assertEquals("Stripe down", (state as AddCardFlowState.Failed).message)
        assertEquals("Stripe down", vm.uiState.value.errorMessage)
    }

    @Test
    fun `prepareAddPaymentMethod empty secret strings are normalised to null`() = runTest {
        fakeMethodsRepo.setupResult = Result.success(
            SetupIntentDataJson(clientSecret = "", customerId = "", ephemeralKey = ""),
        )
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()

        assertNull(vm.uiState.value.setupIntentClientSecret)
        assertNull(vm.uiState.value.setupIntentCustomerId)
        assertNull(vm.uiState.value.setupIntentEphemeralKey)
    }

    @Test
    fun `onAddCardCompleted transitions to Success, closes sheet, clears secrets, reloads`() = runTest {
        fakeMethodsRepo.setupResult = Result.success(
            SetupIntentDataJson(clientSecret = "s", customerId = "c", ephemeralKey = "e"),
        )
        fakeMethodsRepo.loadResult = Result.success(
            listOf(PaymentMethodDisplay("pm_new", "visa", "4242", 12, 2030, false)),
        )
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()
        vm.showAddCardSheet(true)

        vm.onAddCardCompleted()
        advanceUntilIdle()

        assertEquals(AddCardFlowState.Success, vm.uiState.value.addCardFlowState)
        assertFalse(vm.uiState.value.showAddCardSheet)
        assertNull(vm.uiState.value.setupIntentClientSecret)
        assertEquals("Payment method added", vm.uiState.value.successMessage)
        // List reloaded with the newly-added card.
        assertEquals(1, vm.uiState.value.paymentMethods.size)
        assertEquals("pm_new", vm.uiState.value.paymentMethods[0].id)
    }

    @Test
    fun `onAddCardCanceled returns to Idle and clears secrets`() = runTest {
        fakeMethodsRepo.setupResult = Result.success(
            SetupIntentDataJson(clientSecret = "s", customerId = "c", ephemeralKey = "e"),
        )
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()
        vm.showAddCardSheet(true)

        vm.onAddCardCanceled()

        assertEquals(AddCardFlowState.Idle, vm.uiState.value.addCardFlowState)
        assertFalse(vm.uiState.value.showAddCardSheet)
        assertNull(vm.uiState.value.setupIntentClientSecret)
    }

    @Test
    fun `onAddCardSheetPresented transitions Ready to Presenting`() = runTest {
        fakeMethodsRepo.setupResult = Result.success(
            SetupIntentDataJson(clientSecret = "s", customerId = "c", ephemeralKey = "e"),
        )
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.prepareAddPaymentMethod()
        advanceUntilIdle()

        vm.onAddCardSheetPresented()
        assertEquals(AddCardFlowState.Presenting, vm.uiState.value.addCardFlowState)
    }

    @Test
    fun `onAddCardFailed sets Failed with message`() = runTest {
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        vm.onAddCardFailed("Card declined")

        val state = vm.uiState.value.addCardFlowState
        assertTrue(state is AddCardFlowState.Failed)
        assertEquals("Card declined", (state as AddCardFlowState.Failed).message)
        assertEquals("Card declined", vm.uiState.value.errorMessage)
    }

    @Test
    fun `showAddCardSheet toggles flag`() = runTest {
        val vm = PaymentMethodsViewModel(fakeMethodsRepo)
        assertFalse(vm.uiState.value.showAddCardSheet)

        vm.showAddCardSheet(true)
        assertTrue(vm.uiState.value.showAddCardSheet)

        vm.showAddCardSheet(false)
        assertFalse(vm.uiState.value.showAddCardSheet)
    }
}

// -- Fakes --

class FakePaymentRepository : PaymentRepository {
    var createResult: Result<CreatePaymentResponseJson> = Result.failure(Exception("Not set"))
    var listResult: Result<List<Transaction>> = Result.success(emptyList())
    var getResult: Result<Transaction>? = null
    var confirmCaptureResult: Result<Transaction>? = null
    /** When non-empty, each `confirmCapture` call pops the next result. Use for polling tests. */
    val confirmCaptureQueue: ArrayDeque<Result<Transaction>> = ArrayDeque()
    var confirmCaptureCallCount: Int = 0
    var cancelResult: Result<Transaction>? = null
    var tipResult: Result<TipResponseJson> = Result.success(TipResponseJson(success = true))

    override suspend fun createPayment(deliveryMatchId: Int, amount: Double, currency: String) = createResult
    override suspend fun listTransactions(role: String?) = listResult
    override suspend fun getTransaction(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun captureTransaction(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun confirmCapture(deliveryMatchId: Int): Result<Transaction> {
        confirmCaptureCallCount++
        confirmCaptureQueue.removeFirstOrNull()?.let { return it }
        return confirmCaptureResult ?: Result.failure(Exception("Not set"))
    }
    override suspend fun releaseTransaction(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    var refundResult: Result<RefundRequestDataJson> = Result.failure(Exception("Not set"))
    override suspend fun requestRefund(transactionId: Int, amount: Double?, reason: String, description: String?) = refundResult
    override suspend fun getRefundStatus(transactionId: Int) = refundResult
    override suspend fun cancelTransaction(id: Int, reason: String?) = cancelResult ?: Result.failure(Exception("Not set"))
    override suspend fun addTip(transactionId: Int, amount: Double) = tipResult
}

class FakePaymentMethodsRepository : PaymentMethodsRepository {
    var loadResult: Result<List<PaymentMethodDisplay>> = Result.success(emptyList())
    var defaultResult: Result<String?> = Result.success(null)
    var setupResult: Result<SetupIntentDataJson> = Result.failure(Exception("Not set"))
    var removeResult: Result<Unit> = Result.success(Unit)
    var setDefaultResult: Result<Unit> = Result.success(Unit)

    override suspend fun loadPaymentMethods() = loadResult
    override suspend fun loadDefaultPaymentMethod() = defaultResult
    override suspend fun createSetupIntent() = setupResult
    override suspend fun removePaymentMethod(methodId: String) = removeResult
    override suspend fun setDefaultPaymentMethod(methodId: String) = setDefaultResult
}

class FakeStripeConfigRepository : StripeConfigRepository {
    var fetchResult: Result<StripeConfig> = Result.success(
        StripeConfig(
            mode = "sandbox",
            publicKey = "pk_test_123",
            currency = "cad",
            minDeliveryPrice = 5.0,
            senderFeePercentage = 10.0,
            carrierFeePercentage = 5.0,
            platformFeePercentage = 10.0,
        ),
    )

    override suspend fun fetchConfig(): Result<StripeConfig> = fetchResult
}
