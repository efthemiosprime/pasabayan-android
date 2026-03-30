package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.network.payments.CardDetailsJson
import com.efthemiosprime.pasabayan.core.network.payments.OtherPartyJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodApiJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentReceiptJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptAmountJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptDeliveryJson
import com.efthemiosprime.pasabayan.core.network.payments.StripeConfigJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionAmountsJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionUserJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentModelsTest {

    // -- Transaction --

    @Test
    fun `TransactionJson toDomain maps core fields`() {
        val json = TransactionJson(
            id = 500,
            status = "completed",
            deliveryMatchId = 100,
            description = "Payment for delivery",
            shipper = TransactionUserJson(id = 5, name = "Alice"),
            carrier = TransactionUserJson(id = 42, name = "John"),
            amounts = TransactionAmountsJson(total = 175.50, currency = "cad", tip = 5.0),
        )
        val tx = json.toDomain()

        assertEquals(500, tx.id)
        assertEquals(TransactionStatus.COMPLETED, tx.transactionStatus)
        assertEquals(100, tx.deliveryMatchId)
        assertEquals("Alice", tx.shipperName)
        assertEquals("John", tx.carrierName)
        assertEquals(175.50, tx.totalAmount, 0.001)
        assertEquals("cad", tx.currency)
        assertEquals(5.0, tx.tipAmount!!, 0.001)
    }

    @Test
    fun `Transaction transactionStatus parses unknown to UNKNOWN`() {
        val json = TransactionJson(id = 1, status = "garbage")
        val tx = json.toDomain()
        assertEquals(TransactionStatus.UNKNOWN, tx.transactionStatus)
    }

    @Test
    fun `Transaction mapper prefers transaction_status when present`() {
        val json = TransactionJson(
            id = 1,
            status = "pending",
            transactionStatus = "captured",
        )
        val tx = json.toDomain()
        assertEquals(TransactionStatus.CAPTURED, tx.transactionStatus)
    }

    @Test
    fun `Transaction isTerminal delegates to TransactionStatus`() {
        val completed = TransactionJson(id = 1, status = "completed").toDomain()
        assertTrue(completed.isTerminal)

        val pending = TransactionJson(id = 2, status = "pending").toDomain()
        assertFalse(pending.isTerminal)
    }

    @Test
    fun `Transaction handles null amounts`() {
        val json = TransactionJson(id = 1, status = "pending")
        val tx = json.toDomain()
        assertEquals(0.0, tx.totalAmount, 0.001)
        assertEquals("cad", tx.currency)
        assertNull(tx.tipAmount)
    }

    // -- StripeConfig --

    @Test
    fun `StripeConfigJson toDomain maps fields`() {
        val json = StripeConfigJson(
            mode = "sandbox",
            publicKey = "pk_test_abc",
            currency = "cad",
            minDeliveryPrice = 5.0,
            senderFeePercentage = 10.0,
            carrierFeePercentage = 5.0,
            platformFeePercentage = 10.0,
        )
        val config = json.toDomain()

        assertTrue(config.isSandbox)
        assertFalse(config.isLive)
        assertEquals("pk_test_abc", config.publicKey)
        assertEquals(5.0, config.minDeliveryPrice, 0.001)
    }

    @Test
    fun `StripeConfig fee calculations`() {
        val config = StripeConfig(
            mode = "live",
            publicKey = "pk_live_xyz",
            currency = "cad",
            minDeliveryPrice = 5.0,
            senderFeePercentage = 10.0,
            carrierFeePercentage = 5.0,
            platformFeePercentage = 10.0,
        )
        assertEquals(165.0, config.totalToPay(150.0), 0.001)
        assertEquals(15.0, config.serviceFeeAmount(150.0), 0.001)
        assertEquals(142.5, config.carrierReceives(150.0), 0.001)
    }

    @Test
    fun `StripeConfig price validation`() {
        val config = StripeConfig(
            mode = "sandbox", publicKey = "pk_test",
            currency = "cad", minDeliveryPrice = 5.0,
            senderFeePercentage = 10.0, carrierFeePercentage = 5.0, platformFeePercentage = 10.0,
        )
        assertNull(config.validateMinimumPrice(10.0))
        assertTrue(config.validateMinimumPrice(3.0) != null)
        assertNull(config.validateProposedPrice(100.0))
        assertTrue(config.validateProposedPrice(10000.0) != null)
    }

    // -- PaymentMethodDisplay --

    @Test
    fun `PaymentMethodApiJson toDomain maps card`() {
        val json = PaymentMethodApiJson(
            id = "pm_visa_4242",
            card = CardDetailsJson(brand = "visa", last4 = "4242", expMonth = 12, expYear = 2028),
        )
        val display = json.toDomain(isDefault = true)

        assertEquals("pm_visa_4242", display.id)
        assertEquals("visa", display.brand)
        assertEquals("4242", display.last4)
        assertEquals(12, display.expMonth)
        assertEquals(2028, display.expYear)
        assertTrue(display.isDefault)
        assertEquals("Visa •••• 4242", display.displayName)
        assertEquals("12/28", display.expiryDisplay)
    }

    // -- PaymentReceipt --

    @Test
    fun `PaymentReceiptJson toDomain maps fields`() {
        val json = PaymentReceiptJson(
            id = 1000,
            receiptNumber = "RCP-001",
            receiptUrl = "https://example.com/receipt.pdf",
            date = "2026-03-29T10:00:00Z",
            role = "shipper",
            otherParty = OtherPartyJson(id = 42, name = "John Carrier"),
            amount = ReceiptAmountJson(total = 175.50, tip = 5.0, currency = "cad"),
            status = "completed",
            delivery = ReceiptDeliveryJson(pickupCity = "Toronto", deliveryCity = "Montreal"),
        )
        val receipt = json.toDomain()

        assertEquals(1000, receipt.id)
        assertEquals("RCP-001", receipt.receiptNumber)
        assertTrue(receipt.isShipper)
        assertEquals("Toronto → Montreal", receipt.routeDescription)
        assertEquals(175.50, receipt.totalAmount, 0.001)
    }

    @Test
    fun `PaymentReceipt routeDescription fallback`() {
        val json = PaymentReceiptJson(
            id = 1, receiptNumber = "RCP-002", role = "carrier",
            otherParty = OtherPartyJson(id = 5, name = "Alice"),
            amount = ReceiptAmountJson(total = 100.0),
            status = "completed",
        )
        val receipt = json.toDomain()
        assertFalse(receipt.isShipper)
        assertEquals("Alice", receipt.routeDescription)
    }
}
