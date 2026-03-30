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
}

// -- Fakes --

class FakePaymentRepository : PaymentRepository {
    var createResult: Result<CreatePaymentResponseJson> = Result.failure(Exception("Not set"))
    var listResult: Result<List<Transaction>> = Result.success(emptyList())
    var getResult: Result<Transaction>? = null
    var confirmCaptureResult: Result<Transaction>? = null
    var cancelResult: Result<Transaction>? = null
    var tipResult: Result<TipResponseJson> = Result.success(TipResponseJson(success = true))

    override suspend fun createPayment(deliveryMatchId: Int, amount: Double, currency: String) = createResult
    override suspend fun listTransactions(role: String?) = listResult
    override suspend fun getTransaction(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun captureTransaction(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun confirmCapture(deliveryMatchId: Int) = confirmCaptureResult ?: Result.failure(Exception("Not set"))
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
