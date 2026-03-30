package com.efthemiosprime.pasabayan.core.network.payments

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/payments/$name")!!
            .bufferedReader().readText()

    // -- TransactionJson --

    @Test
    fun `TransactionJson decodes full transaction`() {
        val raw = fixture("transaction_full.json")
        val tx = json.decodeFromString<TransactionJson>(raw)

        assertEquals(500, tx.id)
        assertEquals("completed", tx.status)
        assertEquals(100, tx.deliveryMatchId)
        assertEquals("Package delivery payment", tx.description)
        assertEquals("completed", tx.payoutStatus)

        assertNotNull(tx.shipper)
        assertEquals(5, tx.shipper!!.id)
        assertEquals("Alice Shipper", tx.shipper!!.name)

        assertNotNull(tx.carrier)
        assertEquals(42, tx.carrier!!.id)
    }

    @Test
    fun `TransactionAmountsJson decodes flexible doubles from strings`() {
        val raw = fixture("transaction_full.json")
        val tx = json.decodeFromString<TransactionJson>(raw)
        val amounts = tx.amounts!!

        assertEquals(175.50, amounts.total!!, 0.001)
        assertEquals(150.0, amounts.subtotal!!, 0.001)
        assertEquals(25.50, amounts.platformFee!!, 0.001)
        assertEquals(124.50, amounts.carrierReceives!!, 0.001)
        assertEquals("cad", amounts.currency)
        assertEquals(5.0, amounts.tip!!, 0.001)
    }

    @Test
    fun `TransactionJson decodes stripe info`() {
        val raw = fixture("transaction_full.json")
        val tx = json.decodeFromString<TransactionJson>(raw)

        assertNotNull(tx.stripe)
        assertEquals("pi_123abc", tx.stripe!!.paymentIntentId)
        assertEquals("tr_456def", tx.stripe!!.transferId)
    }

    @Test
    fun `TransactionJson decodes tip info`() {
        val raw = fixture("transaction_full.json")
        val tx = json.decodeFromString<TransactionJson>(raw)

        assertNotNull(tx.tip)
        assertEquals(5.0, tx.tip!!.amount!!, 0.001)
    }

    // -- CreatePaymentResponseJson --

    @Test
    fun `CreatePaymentResponseJson decodes with top-level clientSecret`() {
        val raw = fixture("create_payment_response.json")
        val response = json.decodeFromString<CreatePaymentResponseJson>(raw)

        assertTrue(response.success)
        assertEquals("pi_top_level_secret_456", response.clientSecret)
        assertEquals("cus_abc123", response.customerId)
        assertEquals("ek_def456", response.ephemeralKey)
        assertEquals("pk_test_xyz", response.publicKey)
    }

    @Test
    fun `CreatePaymentResponseJson has nested data`() {
        val raw = fixture("create_payment_response.json")
        val response = json.decodeFromString<CreatePaymentResponseJson>(raw)

        assertNotNull(response.data)
        assertEquals(501, response.data!!.id)
        assertEquals("pending", response.data!!.status)
    }

    @Test
    fun `CreatePaymentResponseJson falls back to nested clientSecret`() {
        val raw = fixture("create_payment_response_nested_secret.json")
        val response = json.decodeFromString<CreatePaymentResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("pi_nested_secret_789", response.data!!.clientSecret)
        assertEquals("pi_nested_secret_789", response.clientSecret)
    }

    // -- StripeConfigResponseJson --

    @Test
    fun `StripeConfigResponseJson decodes`() {
        val raw = fixture("stripe_config.json")
        val response = json.decodeFromString<StripeConfigResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("sandbox", response.data!!.mode)
        assertEquals("pk_test_abc123", response.data!!.publicKey)
        assertEquals("cad", response.data!!.currency)
        assertEquals(5.0, response.data!!.minDeliveryPrice!!, 0.001)
        assertEquals(10.0, response.data!!.senderFeePercentage!!, 0.001)
        assertEquals(5.0, response.data!!.carrierFeePercentage!!, 0.001)
    }

    // -- CreatePaymentRequestJson encoding --

    @Test
    fun `CreatePaymentRequestJson encodes`() {
        val request = CreatePaymentRequestJson(
            deliveryMatchId = 100,
            amount = 150.0,
            currency = "cad",
        )
        val decoded = json.decodeFromString<CreatePaymentRequestJson>(
            json.encodeToString(CreatePaymentRequestJson.serializer(), request),
        )
        assertEquals(100, decoded.deliveryMatchId)
        assertEquals(150.0, decoded.amount, 0.001)
        assertEquals("cad", decoded.currency)
    }

    // -- RefundRequestBodyJson encoding --

    @Test
    fun `RefundRequestBodyJson encodes`() {
        val request = RefundRequestBodyJson(
            reason = "Package was damaged",
            amount = 50.0,
            description = "Partial refund for damaged item",
        )
        val decoded = json.decodeFromString<RefundRequestBodyJson>(
            json.encodeToString(RefundRequestBodyJson.serializer(), request),
        )
        assertEquals("Package was damaged", decoded.reason)
        assertEquals(50.0, decoded.amount!!, 0.001)
    }

    @Test
    fun `RefundStatusResponseJson decodes reviewed and processed timestamps`() {
        val raw = fixture("refund_status_response.json")
        val response = json.decodeFromString<RefundStatusResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(901, response.data!!.id)
        assertEquals(500, response.data!!.transactionId)
        assertEquals("approved", response.data!!.status)
        assertEquals("2026-03-30T10:00:00Z", response.data!!.reviewedAt)
        assertEquals("2026-03-30T10:05:00Z", response.data!!.processedAt)
    }

    @Test
    fun `TipRequestJson encodes`() {
        val request = TipRequestJson(amount = 5.0)
        val decoded = json.decodeFromString<TipRequestJson>(
            json.encodeToString(TipRequestJson.serializer(), request),
        )
        assertEquals(5.0, decoded.amount, 0.001)
    }
}
