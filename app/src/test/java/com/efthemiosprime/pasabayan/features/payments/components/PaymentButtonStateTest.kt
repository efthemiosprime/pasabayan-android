package com.efthemiosprime.pasabayan.features.payments.components

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentFlowStatus
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentButtonStateTest {

    @Test
    fun `transaction CAPTURED is treated as already paid`() {
        val state = PaymentUiState(transaction = txWithStatus(TransactionStatus.CAPTURED))
        assertTrue(isAlreadyPaid(state))
    }

    @Test
    fun `transaction COMPLETED is treated as already paid`() {
        val state = PaymentUiState(transaction = txWithStatus(TransactionStatus.COMPLETED))
        assertTrue(isAlreadyPaid(state))
    }

    @Test
    fun `flowStatus ALREADY_PAID is treated as already paid even with no transaction`() {
        val state = PaymentUiState(flowStatus = PaymentFlowStatus.ALREADY_PAID)
        assertTrue(isAlreadyPaid(state))
    }

    @Test
    fun `flowStatus PAYMENT_SUCCESS is treated as already paid`() {
        val state = PaymentUiState(flowStatus = PaymentFlowStatus.PAYMENT_SUCCESS)
        assertTrue(isAlreadyPaid(state))
    }

    @Test
    fun `transaction PENDING with SHEET_READY is NOT already paid`() {
        val state = PaymentUiState(
            transaction = txWithStatus(TransactionStatus.PENDING),
            flowStatus = PaymentFlowStatus.SHEET_READY,
        )
        assertFalse(isAlreadyPaid(state))
    }

    @Test
    fun `default state is NOT already paid`() {
        assertFalse(isAlreadyPaid(PaymentUiState()))
    }

    @Test
    fun `CANCELLED status does not count as paid`() {
        val state = PaymentUiState(transaction = txWithStatus(TransactionStatus.CANCELLED))
        assertFalse(isAlreadyPaid(state))
    }

    private fun txWithStatus(status: TransactionStatus) = Transaction(id = 1, transactionStatus = status)
}
