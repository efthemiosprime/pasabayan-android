package com.efthemiosprime.pasabayan.features.payments.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.model.StripeConnectStatus
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

    @Test
    fun `loadTransaction clears previous cancelSuccess`() = runTest {
        fakePaymentRepo.cancelResult = Result.success(testTransaction(500, "cancelled"))
        fakePaymentRepo.getResult = Result.success(testTransaction(500))
        val vm = TransactionDetailViewModel(fakePaymentRepo)
        vm.cancelTransaction(500)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.cancelSuccess)

        vm.loadTransaction(500)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.cancelSuccess)
    }

    // -- B5: role-aware computed flags --

    @Test
    fun `loadTransaction with viewer role sets state`() = runTest {
        fakePaymentRepo.getResult = Result.success(testTransaction(500))
        val vm = TransactionDetailViewModel(fakePaymentRepo)
        vm.loadTransaction(500, com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER)
        advanceUntilIdle()

        assertEquals(
            com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER,
            vm.uiState.value.viewerRole,
        )
    }

    @Test
    fun `setViewerRole updates role without reloading transaction`() = runTest {
        fakePaymentRepo.getResult = Result.success(testTransaction(500))
        val vm = TransactionDetailViewModel(fakePaymentRepo)
        vm.loadTransaction(500)
        advanceUntilIdle()

        vm.setViewerRole(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER)
        assertEquals(
            com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER,
            vm.uiState.value.viewerRole,
        )
    }

    @Test
    fun `showEarningsCard is true only for carrier viewer with amounts`() {
        val withAmounts = stateFor(tx = txWithAmounts(), role = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER)
        assertTrue(withAmounts.showEarningsCard)

        val shipper = withAmounts.copy(viewerRole = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER)
        assertFalse(shipper.showEarningsCard)

        val noAmounts = stateFor(tx = txNoAmounts(), role = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER)
        assertFalse(noAmounts.showEarningsCard)
    }

    @Test
    fun `showAmountBreakdown is true only for shipper viewer with amounts`() {
        val shipper = stateFor(tx = txWithAmounts(), role = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER)
        assertTrue(shipper.showAmountBreakdown)

        val carrier = shipper.copy(viewerRole = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER)
        assertFalse(carrier.showAmountBreakdown)
    }

    @Test
    fun `canRequestRefund matrix (shipper x status x refund)`() {
        data class Case(
            val role: com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole?,
            val status: TransactionStatus,
            val hasRefund: Boolean,
            val expected: Boolean,
        )
        val cases = listOf(
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.CAPTURED, false, true),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.COMPLETED, false, true),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.PENDING, false, false),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.COMPLETED, true, false),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER, TransactionStatus.COMPLETED, false, false),
            Case(null, TransactionStatus.COMPLETED, false, false),
        )
        cases.forEach { c ->
            val tx = txWithAmounts(status = c.status, refund = if (c.hasRefund) com.efthemiosprime.pasabayan.features.payments.model.RefundInfo(amount = 10.0) else null)
            val state = stateFor(tx = tx, role = c.role)
            assertEquals(
                "case role=${c.role} status=${c.status} hasRefund=${c.hasRefund}",
                c.expected,
                state.canRequestRefund,
            )
        }
    }

    @Test
    fun `canAddTip matrix (shipper x status x tip presence)`() {
        data class Case(
            val role: com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole?,
            val status: TransactionStatus,
            val hasTip: Boolean,
            val expected: Boolean,
        )
        val cases = listOf(
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.COMPLETED, false, true),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.COMPLETED, true, false),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.CAPTURED, false, false),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER, TransactionStatus.PENDING, false, false),
            Case(com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER, TransactionStatus.COMPLETED, false, false),
        )
        cases.forEach { c ->
            val tx = txWithAmounts(
                status = c.status,
                tip = if (c.hasTip) com.efthemiosprime.pasabayan.features.payments.model.TipInfo(amount = 5.0) else null,
            )
            val state = stateFor(tx = tx, role = c.role)
            assertEquals(
                "case role=${c.role} status=${c.status} hasTip=${c.hasTip}",
                c.expected,
                state.canAddTip,
            )
        }
    }

    @Test
    fun `showRefundStatusCard is true when refund present regardless of viewer`() {
        val tx = txWithAmounts(refund = com.efthemiosprime.pasabayan.features.payments.model.RefundInfo(amount = 10.0))
        assertTrue(stateFor(tx, com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER).showRefundStatusCard)
        assertTrue(stateFor(tx, com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER).showRefundStatusCard)
        assertTrue(stateFor(tx, null).showRefundStatusCard)

        assertFalse(stateFor(txWithAmounts(refund = null), com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER).showRefundStatusCard)
    }

    @Test
    fun `showPayoutStatusCard is true only for carrier viewer with payout status`() {
        val tx = txWithAmounts(payoutStatus = com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus.COMPLETED)
        assertTrue(stateFor(tx, com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER).showPayoutStatusCard)
        assertFalse(stateFor(tx, com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.SHIPPER).showPayoutStatusCard)
        assertFalse(stateFor(txWithAmounts(payoutStatus = null), com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole.CARRIER).showPayoutStatusCard)
    }

    private fun stateFor(
        tx: com.efthemiosprime.pasabayan.features.payments.model.Transaction?,
        role: com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole?,
    ) = TransactionDetailUiState(transaction = tx, viewerRole = role)

    private fun txWithAmounts(
        status: TransactionStatus = TransactionStatus.COMPLETED,
        refund: com.efthemiosprime.pasabayan.features.payments.model.RefundInfo? = null,
        tip: com.efthemiosprime.pasabayan.features.payments.model.TipInfo? = null,
        payoutStatus: com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus? = null,
    ) = com.efthemiosprime.pasabayan.features.payments.model.Transaction(
        id = 1,
        transactionStatus = status,
        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 100.0),
        refund = refund,
        tip = tip,
        payoutStatus = payoutStatus,
    )

    private fun txNoAmounts() = com.efthemiosprime.pasabayan.features.payments.model.Transaction(
        id = 1,
        transactionStatus = TransactionStatus.COMPLETED,
        amounts = null,
    )

    // -- StripeConnectViewModel --

    @Test
    fun `loadStatus sets status on success`() = runTest {
        fakeConnectRepo.statusResult = Result.success(
            StripeConnectStatus(
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

    @Test
    fun `openDashboard sets url on success`() = runTest {
        fakeConnectRepo.dashboardResult = Result.success("https://dashboard.stripe.com")
        val vm = StripeConnectViewModel(fakeConnectRepo)
        vm.openDashboard()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showDashboard)
        assertEquals("https://dashboard.stripe.com", vm.uiState.value.dashboardUrl)
    }

    @Test
    fun `handleOnboardingReturn clears onboarding and reloads status`() = runTest {
        fakeConnectRepo.statusResult = Result.success(
            StripeConnectStatus(onboardingComplete = true),
        )
        val vm = StripeConnectViewModel(fakeConnectRepo)
        fakeConnectRepo.onboardResult = Result.success("https://connect.stripe.com/onboard")
        vm.startOnboarding()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showOnboarding)

        vm.handleOnboardingReturn()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showOnboarding)
        assertNull(vm.uiState.value.onboardingUrl)
        assertTrue(vm.uiState.value.status?.onboardingComplete == true)
    }

    @Test
    fun `handleDashboardDismiss clears dashboard state`() = runTest {
        val vm = StripeConnectViewModel(fakeConnectRepo)
        fakeConnectRepo.dashboardResult = Result.success("https://dashboard.stripe.com")
        vm.openDashboard()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showDashboard)

        vm.handleDashboardDismiss()

        assertFalse(vm.uiState.value.showDashboard)
        assertNull(vm.uiState.value.dashboardUrl)
    }

    @Test
    fun `loadStatus debounces within 2 seconds`() = runTest {
        var calls = 0
        fakeConnectRepo.statusResult = Result.success(StripeConnectStatus())
        fakeConnectRepo.beforeStatusCheck = { calls++ }
        val clock = FakeClock(startMs = 1_000L)
        val vm = StripeConnectViewModel(fakeConnectRepo, clock)

        vm.loadStatus()
        advanceUntilIdle()
        assertEquals(1, calls)

        // Within debounce window — should be skipped
        clock.advanceMs(500)
        vm.loadStatus()
        advanceUntilIdle()
        assertEquals(1, calls)

        // After debounce window — should re-issue
        clock.advanceMs(2_500)
        vm.loadStatus()
        advanceUntilIdle()
        assertEquals(2, calls)
    }

    @Test
    fun `loadStatus forceRefresh bypasses debounce`() = runTest {
        var calls = 0
        fakeConnectRepo.statusResult = Result.success(StripeConnectStatus())
        fakeConnectRepo.beforeStatusCheck = { calls++ }
        val clock = FakeClock(startMs = 1_000L)
        val vm = StripeConnectViewModel(fakeConnectRepo, clock)

        vm.loadStatus()
        advanceUntilIdle()
        clock.advanceMs(100)

        vm.loadStatus(forceRefresh = true)
        advanceUntilIdle()
        assertEquals(2, calls)
    }

    @Test
    fun `startOnboarding short-circuits when already onboarded`() = runTest {
        val vm = StripeConnectViewModel(fakeConnectRepo)
        // Seed with onboarded status
        fakeConnectRepo.statusResult = Result.success(
            StripeConnectStatus(
                hasStripeAccount = true,
                onboardingComplete = true,
                chargesEnabled = true,
                payoutsEnabled = true,
                canReceivePayouts = true,
            ),
        )
        vm.loadStatus()
        advanceUntilIdle()

        var onboardCalled = false
        fakeConnectRepo.onboardResult = Result.success("https://connect.stripe.com/onboard")
        fakeConnectRepo.beforeOnboard = { onboardCalled = true }

        vm.startOnboarding()
        advanceUntilIdle()

        assertFalse("repository.startOnboarding should not be called", onboardCalled)
        assertTrue(vm.uiState.value.errorMessage != null)
    }

    @Test
    fun `shouldStartOnboarding returns false when isOnboarded`() {
        val onboarded = StripeConnectStatus(onboardingComplete = true)
        assertFalse(com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel.shouldStartOnboarding(onboarded))
        assertTrue(com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel.shouldStartOnboarding(null))
        assertTrue(
            com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel.shouldStartOnboarding(
                StripeConnectStatus(onboardingComplete = false),
            ),
        )
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

    @Test
    fun `loadReceipts failure clears stale list`() = runTest {
        fakeReceiptRepo.listResult = Result.success(Triple(listOf(testReceipt(1)), false, 1))
        val vm = ReceiptListViewModel(fakeReceiptRepo)
        vm.loadReceipts()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.receipts.size)

        fakeReceiptRepo.listResult = Result.failure(Exception("Failed"))
        vm.loadReceipts()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.receipts.isEmpty())
        assertTrue(vm.uiState.value.error != null)
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

    @Test
    fun `loadReceipt failure clears stale url`() = runTest {
        fakeReceiptRepo.singleResult = Result.success(
            testReceipt(1000, "https://example.com/receipt.pdf"),
        )
        val vm = ReceiptDetailViewModel(fakeReceiptRepo)
        vm.loadReceipt(1000)
        advanceUntilIdle()
        assertEquals("https://example.com/receipt.pdf", vm.uiState.value.receiptUrl)

        fakeReceiptRepo.singleResult = Result.failure(Exception("Unavailable"))
        vm.loadReceipt(1001)
        advanceUntilIdle()
        assertNull(vm.uiState.value.receiptUrl)
        assertTrue(vm.uiState.value.errorMessage != null)
    }

    // -- Helpers --

    private fun testTransaction(id: Int, status: String = "completed") = Transaction(
        id = id,
        transactionStatus = when (status) {
            "cancelled" -> TransactionStatus.CANCELLED
            else -> TransactionStatus.COMPLETED
        },
        deliveryMatchId = 100,
        description = "Test",
        shipper = com.efthemiosprime.pasabayan.features.payments.model.TransactionUser(id = 1, name = "Alice"),
        carrier = com.efthemiosprime.pasabayan.features.payments.model.TransactionUser(id = 2, name = "John"),
        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 150.0),
    )

    private fun testReceipt(id: Int, url: String? = null) = PaymentReceipt(
        id = id,
        receiptNumber = "RCP-$id",
        receiptUrl = url,
        date = "2026-03-29",
        dateFormatted = "Mar 29, 2026",
        role = "shipper",
        otherParty = com.efthemiosprime.pasabayan.features.payments.model.OtherParty(id = 1, name = "John"),
        amount = com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptAmount(total = 150.0),
        delivery = com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptDelivery(
            pickupCity = "Toronto",
            deliveryCity = "Montreal",
        ),
        status = "completed",
    )
}

// -- Fakes --

class FakeStripeConnectRepository : StripeConnectRepository {
    var onboardResult: Result<String> = Result.failure(Exception("Not set"))
    var statusResult: Result<StripeConnectStatus> = Result.failure(Exception("Not set"))
    var dashboardResult: Result<String> = Result.failure(Exception("Not set"))
    var beforeStatusCheck: () -> Unit = {}
    var beforeOnboard: () -> Unit = {}

    override suspend fun startOnboarding(): Result<String> {
        beforeOnboard()
        return onboardResult
    }

    override suspend fun checkStatus(): Result<StripeConnectStatus> {
        beforeStatusCheck()
        return statusResult
    }

    override suspend fun getDashboardUrl() = dashboardResult
}

class FakeClock(private var startMs: Long) : com.efthemiosprime.pasabayan.features.payments.viewmodel.Clock {
    override fun nowMillis(): Long = startMs
    fun advanceMs(delta: Long) {
        startMs += delta
    }
}

class FakeReceiptRepository : ReceiptRepository {
    var listResult: Result<Triple<List<PaymentReceipt>, Boolean, Int>> = Result.success(Triple(emptyList(), false, 0))
    var singleResult: Result<PaymentReceipt>? = null

    override suspend fun fetchReceipts(page: Int, perPage: Int) = listResult
    override suspend fun fetchReceipt(transactionId: Int) = singleResult ?: Result.failure(Exception("Not set"))
}
