package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.network.payments.OtherPartyJson
import com.efthemiosprime.pasabayan.core.network.payments.PaymentReceiptJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptAmountJson
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptDeliveryJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentReceiptMapperTest {

    // NOTE: `PaymentReceipt.url` (Uri) is excluded from JVM tests — `android.net.Uri.parse`
    // is unstubbed in plain JUnit; that computed is exercised on-device.

    @Test
    fun `maps full receipt with nested types`() {
        val json = PaymentReceiptJson(
            id = 1000,
            receiptNumber = "RCP-001",
            receiptUrl = "https://example.com/receipt.pdf",
            date = "2026-03-29T10:00:00Z",
            dateFormatted = "Mar 29, 2026",
            role = "shipper",
            otherParty = OtherPartyJson(id = 42, name = "John Carrier", verificationLevel = "verified"),
            amount = ReceiptAmountJson(total = 175.50, carrierAmount = 124.50, platformFee = 25.50, tip = 5.0, currency = "cad"),
            delivery = ReceiptDeliveryJson(pickupCity = "Toronto", deliveryCity = "Montreal", packageTitle = "Box"),
            status = "completed",
        )
        val receipt = json.toDomain()

        assertEquals(1000, receipt.id)
        assertEquals("RCP-001", receipt.receiptNumber)
        assertEquals("Mar 29, 2026", receipt.dateFormatted)
        assertTrue(receipt.isShipper)

        // OtherParty
        assertEquals(42, receipt.otherParty!!.id)
        assertEquals("John Carrier", receipt.otherParty!!.name)
        assertEquals("verified", receipt.otherParty!!.verificationLevel)

        // Amount
        assertEquals(175.50, receipt.amount.total, 0.001)
        assertEquals(124.50, receipt.amount.carrierAmount!!, 0.001)
        assertEquals(25.50, receipt.amount.platformFee!!, 0.001)
        assertEquals(5.0, receipt.amount.tip!!, 0.001)
        assertEquals("cad", receipt.amount.currency)

        // Delivery
        assertEquals("Toronto", receipt.delivery!!.pickupCity)
        assertEquals("Montreal", receipt.delivery!!.deliveryCity)
        assertEquals("Box", receipt.delivery!!.packageTitle)
    }

    @Test
    fun `routeDescription returns pickup arrow delivery when both cities present`() {
        val json = baseJson(
            delivery = ReceiptDeliveryJson(pickupCity = "Toronto", deliveryCity = "Montreal"),
        )
        assertEquals("Toronto → Montreal", json.toDomain().routeDescription)
    }

    @Test
    fun `routeDescription falls back to otherParty name when delivery missing`() {
        val json = baseJson(
            role = "carrier",
            otherParty = OtherPartyJson(id = 5, name = "Alice"),
            delivery = null,
        )
        val receipt = json.toDomain()
        assertFalse(receipt.isShipper)
        assertEquals("Alice", receipt.routeDescription)
    }

    @Test
    fun `routeDescription returns empty when nothing to display`() {
        val json = baseJson(otherParty = null, delivery = null)
        assertEquals("", json.toDomain().routeDescription)
    }

    @Test
    fun `displayAmountValue is total for shipper`() {
        val json = baseJson(
            role = "shipper",
            amount = ReceiptAmountJson(total = 175.50, carrierAmount = 124.50, tip = 5.0),
        )
        assertEquals(175.50, json.toDomain().displayAmountValue, 0.001)
    }

    @Test
    fun `displayAmountValue is carrierAmount plus tip for carrier`() {
        val json = baseJson(
            role = "carrier",
            amount = ReceiptAmountJson(total = 175.50, carrierAmount = 124.50, tip = 5.0),
        )
        assertEquals(129.50, json.toDomain().displayAmountValue, 0.001)
    }

    @Test
    fun `displayAmountValue treats null carrier fields as zero`() {
        val json = baseJson(
            role = "carrier",
            amount = ReceiptAmountJson(total = 100.0),
        )
        // carrierAmount + tip both null → 0
        assertEquals(0.0, json.toDomain().displayAmountValue, 0.001)
    }

    @Test
    fun `missing amount JSON yields default PaymentReceiptAmount`() {
        val json = baseJson(amount = null)
        val receipt = json.toDomain()
        assertEquals(0.0, receipt.amount.total, 0.001)
        assertEquals("cad", receipt.amount.currency)
        assertNull(receipt.amount.tip)
    }

    @Test
    fun `displayAmount string is formatted with currency`() {
        val json = baseJson(
            role = "shipper",
            amount = ReceiptAmountJson(total = 99.99, currency = "usd"),
        )
        val receipt = json.toDomain()
        assertEquals("$99.99 USD", receipt.displayAmount)
    }

    @Test
    fun `dateFormatted may be null`() {
        val json = baseJson(dateFormatted = null)
        assertNull(json.toDomain().dateFormatted)
    }

    @Test
    fun `otherParty preserves nullable verificationLevel`() {
        val json = baseJson(otherParty = OtherPartyJson(id = 1, name = "Bob"))
        val party = json.toDomain().otherParty
        assertNotNull(party)
        assertNull(party!!.verificationLevel)
    }

    private fun baseJson(
        id: Int = 1,
        receiptNumber: String = "RCP-1",
        dateFormatted: String? = "Mar 1, 2026",
        role: String = "shipper",
        otherParty: OtherPartyJson? = OtherPartyJson(id = 1, name = "Friend"),
        amount: ReceiptAmountJson? = ReceiptAmountJson(total = 100.0),
        delivery: ReceiptDeliveryJson? = null,
    ) = PaymentReceiptJson(
        id = id,
        receiptNumber = receiptNumber,
        date = "2026-03-01T00:00:00Z",
        dateFormatted = dateFormatted,
        role = role,
        otherParty = otherParty,
        amount = amount,
        delivery = delivery,
        status = "completed",
    )
}
