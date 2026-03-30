package com.efthemiosprime.pasabayan.features.payments.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.services.ReceiptRepository
import com.efthemiosprime.pasabayan.features.payments.services.StripeConnectRepository
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
class RemainingViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePaymentRepo: FakePaymentRepository
    private lateinit var fakeConnectRepo: FakeStripeConnectRepository
    private lateinit var fakeReceiptRepo: FakeReceiptRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePaymentRepo = FakePaymentRepository()
        fakeConnectRepo = FakeStripeConnectRepository()
        fakeReceiptRepo = FakeReceiptRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- TransactionHistoryViewModel --

    @Test
    fun `loadTransactions sets list on success`() = runTest {
        fakePaymentRepo.listResult = Result.success(listOf(testTransaction(1), testTransaction(2)))
        val vm = TransactionHistoryViewModel(fakePaymentRepo)
        vm.loadTransactions()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.transactions.size)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadTransactions sets error on failure`() = runTest {
        fakePaymentRepo.listResult = Result.failure(Exception("Network error"))
        val vm = TransactionHistoryViewModel(fakePaymentRepo)
        vm.loadTransactions()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
    }

    // -- TransactionDetailViewModel --

    @Test
    fun `loadTransaction sets transaction on success`() = runTest {
        fakePaymentRepo.getResult = Result.success(testTransaction(500))
        val vm = TransactionDetailViewModel(fakePaymentRepo)
        vm.loadTransaction(500)
        advanceUntilIdle()

        assertEquals(500, vm.uiState.value.transaction!!.id)
    }

    @Test
    fun `cancelTransaction sets success`() = runTest {
        fakePaymentRepo.cancelResult = Result.success(testTransaction(500, "cancelled"))
        val vm = TransactionDetailViewModel(fakePaymentRepo)
        vm.cancelTransaction(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.cancelSuccess)
    }

    // -- StripeConnectViewModel --

    @Test
    fun `loadStatus sets status on success`() = runTest {
        fakeConnectRepo.statusResult = Result.success(
            StripeConnectStatusJson(
                hasStripeAccount = true,
                onboardingComplete = true,
                chargesEnabled = true,
                payoutsEnabled = true,
                canReceivePayouts = true,
            ),
        )
        val vm = StripeConnectViewModel(fakeConnectRepo)
        vm.loadStatus()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.status!!.onboardingComplete)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `startOnboarding sets url on success`() = runTest {
        fakeConnectRepo.onboardResult = Result.success("https://connect.stripe.com/onboard")
        val vm = StripeConnectViewModel(fakeConnectRepo)
        vm.startOnboarding()
        advanceUntilIdle()

        assertEquals("https://connect.stripe.com/onboard", vm.uiState.value.onboardingUrl)
        assertTrue(vm.uiState.value.showOnboarding)
    }

    // -- ReceiptListViewModel --

    @Test
    fun `loadReceipts sets list on success`() = runTest {
        fakeReceiptRepo.listResult = Result.success(
            Triple(listOf(testReceipt(1)), true, 25),
        )
        val vm = ReceiptListViewModel(fakeReceiptRepo)
        vm.loadReceipts()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.receipts.size)
        assertTrue(vm.uiState.value.hasMore)
        assertEquals(25, vm.uiState.value.totalCount)
    }

    @Test
    fun `loadMore appends receipts`() = runTest {
        fakeReceiptRepo.listResult = Result.success(
            Triple(listOf(testReceipt(1)), true, 25),
        )
        val vm = ReceiptListViewModel(fakeReceiptRepo)
        vm.loadReceipts()
        advanceUntilIdle()

        fakeReceiptRepo.listResult = Result.success(
            Triple(listOf(testReceipt(2)), false, 25),
        )
        vm.loadMore()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.receipts.size)
        assertFalse(vm.uiState.value.hasMore)
    }

    // -- ReceiptDetailViewModel --

    @Test
    fun `loadReceipt sets url on success`() = runTest {
        fakeReceiptRepo.singleResult = Result.success(
            testReceipt(1000, "https://example.com/receipt.pdf"),
        )
        val vm = ReceiptDetailViewModel(fakeReceiptRepo)
        vm.loadReceipt(1000)
        advanceUntilIdle()

        assertEquals("https://example.com/receipt.pdf", vm.uiState.value.receiptUrl)
    }

    // -- Helpers --

    private fun testTransaction(id: Int, status: String = "completed") = Transaction(
        id = id, transactionStatus = TransactionStatus.COMPLETED,
        deliveryMatchId = 100, description = "Test",
        shipperName = "Alice", carrierName = "John",
        totalAmount = 150.0, subtotal = null, platformFee = null,
        carrierReceives = null, currency = "cad", tipAmount = null,
        clientSecret = null, customerId = null, ephemeralKey = null,
        payoutStatus = null, createdAt = null, updatedAt = null,
    )

    private fun testReceipt(id: Int, url: String? = null) = PaymentReceipt(
        id = id, receiptNumber = "RCP-$id", receiptUrl = url,
        date = "2026-03-29", dateFormatted = "Mar 29, 2026",
        role = "shipper", otherPartyName = "John",
        totalAmount = 150.0, carrierAmount = null, platformFee = null,
        tipAmount = null, currency = "cad", status = "completed",
        pickupCity = "Toronto", deliveryCity = "Montreal", packageTitle = null,
    )
}

// -- Fakes --

class FakeStripeConnectRepository : StripeConnectRepository {
    var onboardResult: Result<String> = Result.failure(Exception("Not set"))
    var statusResult: Result<StripeConnectStatusJson> = Result.failure(Exception("Not set"))
    var dashboardResult: Result<String> = Result.failure(Exception("Not set"))

    override suspend fun startOnboarding() = onboardResult
    override suspend fun checkStatus() = statusResult
    override suspend fun getDashboardUrl() = dashboardResult
}

class FakeReceiptRepository : ReceiptRepository {
    var listResult: Result<Triple<List<PaymentReceipt>, Boolean, Int>> = Result.success(Triple(emptyList(), false, 0))
    var singleResult: Result<PaymentReceipt>? = null

    override suspend fun fetchReceipts(page: Int, perPage: Int) = listResult
    override suspend fun fetchReceipt(transactionId: Int) = singleResult ?: Result.failure(Exception("Not set"))
}
