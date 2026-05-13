package com.efthemiosprime.pasabayan.features.payments.components

import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig
import com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentSummaryComputationTest {

    private val defaultConfig = StripeConfig(
        mode = "sandbox",
        publicKey = "pk_test",
        currency = "cad",
        minDeliveryPrice = StripeConfig.DEFAULT_MIN_DELIVERY_PRICE,
        senderFeePercentage = StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
        carrierFeePercentage = StripeConfig.DEFAULT_CARRIER_FEE_PERCENTAGE,
        platformFeePercentage = StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
    )

    @Test
    fun `Prepayment derives fee, total, and carrierReceives from StripeConfig`() {
        val rows = PaymentSummarySource.Prepayment(150.0, defaultConfig).compute()
        assertEquals(150.0, rows.baseAmount!!, 0.001)
        assertEquals(15.0, rows.serviceFee, 0.001) // 10%
        assertEquals(165.0, rows.total, 0.001)
        assertEquals(142.5, rows.carrierReceives, 0.001) // -5%
        assertEquals("cad", rows.currency)
    }

    @Test
    fun `Prepayment passes through alternate currencies`() {
        val usd = defaultConfig.copy(currency = "usd")
        val rows = PaymentSummarySource.Prepayment(100.0, usd).compute()
        assertEquals("usd", rows.currency)
        assertEquals(110.0, rows.total, 0.001)
    }

    @Test
    fun `Postpayment reads server values verbatim`() {
        val rows = PaymentSummarySource.Postpayment(
            amounts = TransactionAmounts(
                total = 165.0,
                subtotal = 150.0,
                platformFee = 15.0,
                carrierReceives = 142.5,
                currency = "cad",
                baseAmount = 150.0,
            ),
        ).compute()
        assertEquals(150.0, rows.baseAmount!!, 0.001)
        assertEquals(15.0, rows.serviceFee, 0.001)
        assertEquals(165.0, rows.total, 0.001)
        assertEquals(142.5, rows.carrierReceives, 0.001)
    }

    @Test
    fun `Postpayment falls back to subtotal when baseAmount missing`() {
        val rows = PaymentSummarySource.Postpayment(
            amounts = TransactionAmounts(
                total = 120.0,
                subtotal = 100.0,
                platformFee = 20.0,
                carrierReceives = 95.0,
                baseAmount = null,
            ),
        ).compute()
        assertEquals(100.0, rows.baseAmount!!, 0.001)
    }

    @Test
    fun `Postpayment baseAmount is null when both baseAmount and subtotal are null`() {
        val rows = PaymentSummarySource.Postpayment(
            amounts = TransactionAmounts(total = 50.0, subtotal = null, baseAmount = null),
        ).compute()
        assertNull(rows.baseAmount)
        assertEquals(50.0, rows.total, 0.001)
    }
}
