package com.efthemiosprime.pasabayan.features.payments.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionAmountsTest {

    @Test
    fun `hasTwoSidedBreakdown is true when baseAmount is present`() {
        val amounts = sampleAmounts(baseAmount = 100.0)
        assertTrue(amounts.hasTwoSidedBreakdown)
    }

    @Test
    fun `hasTwoSidedBreakdown is false when baseAmount is null`() {
        val amounts = sampleAmounts(baseAmount = null)
        assertFalse(amounts.hasTwoSidedBreakdown)
    }

    @Test
    fun `senderFee is total minus base minus tax`() {
        // total = 115, base = 100, tax = 5  → senderFee = 10
        val amounts = sampleAmounts(total = 115.0, baseAmount = 100.0, tax = 5.0)
        assertEquals(10.0, amounts.senderFee, 0.001)
    }

    @Test
    fun `senderFee treats null tax as zero`() {
        // total = 110, base = 100, tax = null → senderFee = 10
        val amounts = sampleAmounts(total = 110.0, baseAmount = 100.0, tax = null)
        assertEquals(10.0, amounts.senderFee, 0.001)
    }

    @Test
    fun `senderFee returns zero when baseAmount missing`() {
        val amounts = sampleAmounts(total = 110.0, baseAmount = null)
        assertEquals(0.0, amounts.senderFee, 0.001)
    }

    @Test
    fun `senderFee never goes negative`() {
        // total < base + tax → would be negative, clamp to 0
        val amounts = sampleAmounts(total = 100.0, baseAmount = 110.0, tax = 5.0)
        assertEquals(0.0, amounts.senderFee, 0.001)
    }

    @Test
    fun `carrierFee is base minus carrierReceives`() {
        // base = 100, carrierReceives = 95 → carrierFee = 5
        val amounts = sampleAmounts(baseAmount = 100.0, carrierReceives = 95.0)
        assertEquals(5.0, amounts.carrierFee, 0.001)
    }

    @Test
    fun `carrierFee returns zero when baseAmount missing`() {
        val amounts = sampleAmounts(baseAmount = null, carrierReceives = 95.0)
        assertEquals(0.0, amounts.carrierFee, 0.001)
    }

    @Test
    fun `carrierFee never goes negative`() {
        val amounts = sampleAmounts(baseAmount = 90.0, carrierReceives = 95.0)
        assertEquals(0.0, amounts.carrierFee, 0.001)
    }

    @Test
    fun `carrierTotal defaults to carrierReceives plus tip`() {
        val amounts = TransactionAmounts(total = 105.0, carrierReceives = 100.0, tip = 5.0)
        assertEquals(105.0, amounts.carrierTotal, 0.001)
    }

    @Test
    fun `currency defaults to cad`() {
        val amounts = TransactionAmounts(total = 50.0)
        assertEquals("cad", amounts.currency)
    }

    private fun sampleAmounts(
        total: Double = 100.0,
        baseAmount: Double? = null,
        carrierReceives: Double = 0.0,
        tax: Double? = null,
    ) = TransactionAmounts(
        total = total,
        baseAmount = baseAmount,
        carrierReceives = carrierReceives,
        tax = tax,
    )
}
