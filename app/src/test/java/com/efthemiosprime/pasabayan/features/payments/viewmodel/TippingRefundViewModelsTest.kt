package com.efthemiosprime.pasabayan.features.payments.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus
import com.efthemiosprime.pasabayan.core.network.payments.TipResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
import com.efthemiosprime.pasabayan.features.payments.model.RefundRequest
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
class TippingRefundViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePaymentRepo: FakePaymentRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePaymentRepo = FakePaymentRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- TippingViewModel --

    @Test
    fun `selectPresetTip sets amount`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        assertEquals(5.0, vm.uiState.value.selectedTipAmount, 0.001)
    }

    @Test
    fun `clearPresetTip resets to zero`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(10.0)
        vm.clearPresetTip()
        assertEquals(0.0, vm.uiState.value.selectedTipAmount, 0.001)
    }

    @Test
    fun `effectiveTipAmount uses preset when custom is empty`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        assertEquals(5.0, vm.uiState.value.effectiveTipAmount, 0.001)
    }

    @Test
    fun `effectiveTipAmount uses custom when set`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.setCustomTipAmount("7.50")
        assertEquals(7.50, vm.uiState.value.effectiveTipAmount, 0.001)
    }

    @Test
    fun `isValidTip true for valid range`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        assertTrue(vm.uiState.value.isValidTip)
    }

    @Test
    fun `isValidTip false below minimum`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.setCustomTipAmount("0.50")
        assertFalse(vm.uiState.value.isValidTip)
    }

    @Test
    fun `isValidTip false above maximum`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.setCustomTipAmount("600")
        assertFalse(vm.uiState.value.isValidTip)
    }

    @Test
    fun `addTip success without clientSecret`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, data = TransactionJson(id = 1, status = "completed")),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showSuccess)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `addTip with clientSecret sets showPaymentSheet`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, clientSecret = "pi_tip_secret"),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showPaymentSheet)
        assertFalse(vm.uiState.value.showSuccess)
    }

    @Test
    fun `addTip rejects invalid amount before repository call`() = runTest {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.setCustomTipAmount("0.5")
        vm.addTip(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
        assertFalse(vm.uiState.value.isLoading)
    }

    // -- B3: PaymentSheet bridge --

    @Test
    fun `setupPaymentSheet stores secret and config and marks ready`() {
        val vm = TippingViewModel(fakePaymentRepo)
        vm.setupPaymentSheet("pi_tip_secret")

        assertEquals("pi_tip_secret", vm.uiState.value.clientSecret)
        assertTrue(vm.uiState.value.paymentSheetReady)
        assertTrue(vm.uiState.value.paymentSheetConfig != null)
    }

    @Test
    fun `addTip with clientSecret auto-invokes setupPaymentSheet`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, clientSecret = "pi_tip_xyz"),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        assertEquals("pi_tip_xyz", vm.uiState.value.clientSecret)
        assertTrue(vm.uiState.value.paymentSheetReady)
        assertTrue(vm.uiState.value.paymentSheetConfig != null)
        assertTrue(vm.uiState.value.showPaymentSheet)
    }

    @Test
    fun `addTip preserves updatedTransaction from response data`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(
                success = true,
                data = TransactionJson(id = 42, status = "completed"),
            ),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        assertEquals(42, vm.uiState.value.updatedTransaction?.id)
    }

    @Test
    fun `handlePaymentResult COMPLETED sets showSuccess and clears sheet`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, clientSecret = "pi_tip_ok"),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        vm.handlePaymentResult(PaymentSheetResultKind.COMPLETED)

        assertEquals(PaymentSheetResultKind.COMPLETED, vm.uiState.value.paymentSheetResult)
        assertTrue(vm.uiState.value.showSuccess)
        assertFalse(vm.uiState.value.showPaymentSheet)
        assertFalse(vm.uiState.value.paymentSheetReady)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `handlePaymentResult CANCELED clears sheet without success`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, clientSecret = "pi_tip_cancel"),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        vm.handlePaymentResult(PaymentSheetResultKind.CANCELED)

        assertEquals(PaymentSheetResultKind.CANCELED, vm.uiState.value.paymentSheetResult)
        assertFalse(vm.uiState.value.showSuccess)
        assertFalse(vm.uiState.value.showPaymentSheet)
    }

    @Test
    fun `handlePaymentResult FAILED clears secret and surfaces error`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(success = true, clientSecret = "pi_tip_fail"),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        vm.handlePaymentResult(PaymentSheetResultKind.FAILED, "Card declined")

        assertEquals(PaymentSheetResultKind.FAILED, vm.uiState.value.paymentSheetResult)
        assertNull(vm.uiState.value.clientSecret)
        assertNull(vm.uiState.value.paymentSheetConfig)
        assertEquals("Card declined", vm.uiState.value.errorMessage)
    }

    @Test
    fun `addTip success without secret leaves sheet untouched`() = runTest {
        fakePaymentRepo.tipResult = Result.success(
            TipResponseJson(
                success = true,
                data = TransactionJson(id = 7, status = "completed"),
            ),
        )
        val vm = TippingViewModel(fakePaymentRepo)
        vm.selectPresetTip(5.0)
        vm.addTip(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showSuccess)
        assertFalse(vm.uiState.value.paymentSheetReady)
        assertFalse(vm.uiState.value.showPaymentSheet)
    }

    // -- RefundViewModel --

    @Test
    fun `selectReason sets selectedReason`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        assertEquals(RefundReason.DAMAGED, vm.uiState.value.selectedReason)
    }

    @Test
    fun `effectiveReason uses preset display text`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        assertEquals("Package was damaged during delivery", vm.uiState.value.effectiveReason)
    }

    @Test
    fun `effectiveReason uses custom when OTHER`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.OTHER)
        vm.setCustomReason("Custom reason text here")
        assertEquals("Custom reason text here", vm.uiState.value.effectiveReason)
    }

    @Test
    fun `isValidRequest false when reason too short`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.OTHER)
        vm.setCustomReason("short")
        assertFalse(vm.uiState.value.isValidRequest)
    }

    @Test
    fun `isValidRequest true for preset reason`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        assertTrue(vm.uiState.value.isValidRequest)
    }

    @Test
    fun `submitRefund success`() = runTest {
        fakePaymentRepo.refundResult = Result.success(
            RefundRequest(id = 1, transactionId = 500, status = RefundStatus.PENDING, reasonText = "Damaged"),
        )
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.submitRefundRequest(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.refundSuccess)
        assertEquals(1, vm.uiState.value.refundRequest?.id)
        assertEquals(RefundStatus.PENDING, vm.uiState.value.refundRequest?.status)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `submitRefund fails fast when reason invalid`() = runTest {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.OTHER)
        vm.setCustomReason("short")
        vm.submitRefundRequest(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `submitRefund fails fast when partial amount invalid`() = runTest {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.setPartialRefund(true)
        vm.setPartialAmount("abc")
        vm.submitRefundRequest(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `reset clears all state`() {
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.reset()
        assertNull(vm.uiState.value.selectedReason)
        assertFalse(vm.uiState.value.refundSuccess)
    }

    // -- B4: checkRefundStatus (silent-failure polling) --

    @Test
    fun `checkRefundStatus success overwrites refundRequest`() = runTest {
        // Seed an initial value via submitRefundRequest to verify the overwrite later.
        fakePaymentRepo.refundResult = Result.success(
            RefundRequest(id = 5, transactionId = 500, status = RefundStatus.PENDING),
        )
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.submitRefundRequest(500)
        advanceUntilIdle()
        assertEquals(RefundStatus.PENDING, vm.uiState.value.refundRequest?.status)

        // Now status changes to APPROVED on the server.
        fakePaymentRepo.refundStatusQueue.add(
            Result.success(RefundRequest(id = 5, transactionId = 500, status = RefundStatus.APPROVED)),
        )
        vm.checkRefundStatus(500)
        advanceUntilIdle()

        assertEquals(RefundStatus.APPROVED, vm.uiState.value.refundRequest?.status)
    }

    @Test
    fun `checkRefundStatus silent failure preserves prior refundRequest`() = runTest {
        fakePaymentRepo.refundResult = Result.success(
            RefundRequest(id = 9, transactionId = 500, status = RefundStatus.PENDING),
        )
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.submitRefundRequest(500)
        advanceUntilIdle()
        val beforeRefundRequest = vm.uiState.value.refundRequest
        val beforeError = vm.uiState.value.errorMessage

        fakePaymentRepo.refundStatusQueue.add(Result.failure(Exception("transient network")))
        vm.checkRefundStatus(500)
        advanceUntilIdle()

        // Prior state untouched — no error surfaced, no nulling of the existing request.
        assertEquals(beforeRefundRequest, vm.uiState.value.refundRequest)
        assertEquals(beforeError, vm.uiState.value.errorMessage)
    }

    @Test
    fun `checkRefundStatus does not surface error when state is empty`() = runTest {
        fakePaymentRepo.refundStatusQueue.add(Result.failure(Exception("auth required")))
        val vm = RefundViewModel(fakePaymentRepo)
        vm.checkRefundStatus(500)
        advanceUntilIdle()

        assertNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.refundRequest)
    }
}
