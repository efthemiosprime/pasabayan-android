package com.efthemiosprime.pasabayan.core.network.payments

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectMethodsReceiptDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/payments/$name")!!
            .bufferedReader().readText()

    // -- StripeConnectStatus --

    @Test
    fun `StripeConnectStatusJson decodes`() {
        val raw = fixture("connect_status.json")
        val response = json.decodeFromString<StripeStatusResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertTrue(response.data!!.hasStripeAccount)
        assertEquals("acct_abc123", response.data!!.stripeAccountId)
        assertTrue(response.data!!.onboardingComplete)
        assertTrue(response.data!!.chargesEnabled)
        assertTrue(response.data!!.payoutsEnabled)
        assertTrue(response.data!!.canReceivePayouts)
    }

    // -- PaymentMethods --

    @Test
    fun `PaymentMethodsResponseJson decodes list`() {
        val raw = fixture("payment_methods.json")
        val response = json.decodeFromString<PaymentMethodsResponseJson>(raw)

        assertTrue(response.success)
        assertEquals(2, response.data.size)

        val visa = response.data[0]
        assertEquals("pm_visa_4242", visa.id)
        assertNotNull(visa.card)
        assertEquals("visa", visa.card!!.brand)
        assertEquals("4242", visa.card!!.last4)
        assertEquals(12, visa.card!!.expMonth)
        assertEquals(2028, visa.card!!.expYear)

        val mc = response.data[1]
        assertEquals("mastercard", mc.card!!.brand)
    }

    @Test
    fun `SetupIntentResponseJson decodes`() {
        val raw = """{"success": true, "data": {"client_secret": "seti_123_secret", "customer_id": "cus_abc", "ephemeral_key": "ek_def"}}"""
        val response = json.decodeFromString<SetupIntentResponseJson>(raw)

        assertTrue(response.success)
        assertEquals("seti_123_secret", response.data!!.clientSecret)
        assertEquals("cus_abc", response.data!!.customerId)
    }

    @Test
    fun `DefaultPaymentMethodResponseJson decodes`() {
        val raw = """{"success": true, "data": {"payment_method_id": "pm_visa_4242"}}"""
        val response = json.decodeFromString<DefaultPaymentMethodResponseJson>(raw)

        assertTrue(response.success)
        assertEquals("pm_visa_4242", response.data!!.paymentMethodId)
    }

    // -- Receipts --

    @Test
    fun `PaymentReceiptJson decodes full receipt`() {
        val raw = fixture("receipt.json")
        val response = json.decodeFromString<SinglePaymentReceiptResponseJson>(raw)

        assertTrue(response.success)
        val receipt = response.data!!
        assertEquals(1000, receipt.id)
        assertEquals("RCP-2026-001", receipt.receiptNumber)
        assertEquals("shipper", receipt.role)
        assertEquals("completed", receipt.status)

        assertNotNull(receipt.otherParty)
        assertEquals("John Carrier", receipt.otherParty!!.name)

        assertNotNull(receipt.amount)
        assertEquals(175.50, receipt.amount!!.total, 0.001)
        assertEquals(5.0, receipt.amount!!.tip!!, 0.001)
        assertEquals("cad", receipt.amount!!.currency)

        assertNotNull(receipt.delivery)
        assertEquals("Toronto", receipt.delivery!!.pickupCity)
        assertEquals("Montreal", receipt.delivery!!.deliveryCity)
    }

    // -- Onboarding response --

    @Test
    fun `StripeOnboardingResponseJson decodes`() {
        val raw = """{"success": true, "data": {"onboarding_url": "https://connect.stripe.com/onboard", "stripe_account_id": "acct_new"}}"""
        val response = json.decodeFromString<StripeOnboardingResponseJson>(raw)

        assertTrue(response.success)
        assertEquals("https://connect.stripe.com/onboard", response.data!!.onboardingUrl)
        assertEquals("acct_new", response.data!!.stripeAccountId)
    }
}
