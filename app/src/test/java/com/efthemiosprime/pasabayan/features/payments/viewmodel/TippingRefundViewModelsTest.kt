package com.efthemiosprime.pasabayan.features.payments.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import com.efthemiosprime.pasabayan.core.network.payments.TipResponseJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
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
            RefundRequestDataJson(id = 1, status = "pending", reason = "Damaged"),
        )
        val vm = RefundViewModel(fakePaymentRepo)
        vm.selectReason(RefundReason.DAMAGED)
        vm.submitRefundRequest(500)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.refundSuccess)
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
}
