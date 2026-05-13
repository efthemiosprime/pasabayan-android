package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/bookings/$name")!!
            .bufferedReader().readText()

    // -- DeliveryMatchJson --

    @Test
    fun `DeliveryMatchJson decodes full match`() {
        val raw = fixture("delivery_match_full.json")
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertEquals(100, match.id)
        assertEquals(1, match.tripId)
        assertEquals(10, match.packageRequestId)
        assertEquals(MatchStatus.CONFIRMED, match.matchStatus)
        assertEquals(150.50, match.agreedPrice!!, 0.001)
        assertEquals(InitiatedBy.CARRIER, match.initiatedBy)
        assertFalse(match.isCounterOffer)
        assertEquals("120.00", match.originalPrice)
        assertEquals(3, match.remainingCounterOffers)
        assertTrue(match.canCounterOffer == true)
        assertEquals("I can carry this safely", match.carrierMessage)
        assertEquals("123456", match.pickupConfirmationCode)
        assertEquals(10, match.platformFeePercent)
        assertTrue(match.isBelowRate == true)

        // Carrier (UserSummary)
        assertNotNull(match.carrier)
        assertEquals(42, match.carrier!!.id)
        assertEquals("John Carrier", match.carrier!!.name)

        // Shipper (UserSummary)
        assertNotNull(match.shipper)
        assertEquals(5, match.shipper!!.id)

        assertEquals(77, match.chatConversationId)
    }

    // Note: `auto_charge` was moved out of DeliveryMatchJson in the advisory-weight
    // sweep (slice A.1) — it now lives on MatchConfirmResponseJson (sibling of `data`)
    // and is covered by BookingsRepositoryImplTest.confirmMatch decodes auto_charge.
    // Decoder tolerance for an extra `auto_charge` field in a match-shaped body
    // (the fixture still contains one) is verified implicitly by the full-match test
    // above succeeding with `ignoreUnknownKeys = true`.

    @Test
    fun `DeliveryMatchJson decodes with minimal fields`() {
        val raw = """{"id": 50, "match_status": "pending", "agreed_price": 100.0}"""
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertEquals(50, match.id)
        assertEquals(MatchStatus.PENDING, match.matchStatus)
        assertEquals(100.0, match.agreedPrice!!, 0.001)
    }

    // -- CancelMatchResponseJson + RefundResult parity (iOS b3d7675) --

    @Test
    fun `CancelMatchResponseJson decodes data, chat_conversation_id, refund`() {
        val raw = fixture("cancel_match_response.json")
        val response = json.decodeFromString<CancelMatchResponseJson>(raw)

        assertEquals("Match cancelled and refund processed", response.message)
        assertNotNull(response.data)
        assertEquals(100, response.data!!.id)
        assertEquals(MatchStatus.CANCELLED, response.data!!.matchStatus)
        assertEquals(77, response.chatConversationId)
        assertNotNull(response.refund)
    }

    @Test
    fun `RefundResultJson decodes processed, amount, transaction_id`() {
        val raw = fixture("cancel_match_response.json")
        val response = json.decodeFromString<CancelMatchResponseJson>(raw)

        val refund = response.refund!!
        assertTrue(refund.processed)
        assertEquals(150.50, refund.amount!!, 0.001)
        assertEquals(555, refund.transactionId)
        assertEquals(null, refund.error)
    }

    @Test
    fun `CancelMatchResponseJson tolerates missing data, conversation, and refund`() {
        val raw = """{"message": "Cancelled"}"""
        val response = json.decodeFromString<CancelMatchResponseJson>(raw)

        assertEquals("Cancelled", response.message)
        assertEquals(null, response.data)
        assertEquals(null, response.chatConversationId)
        assertEquals(null, response.refund)
    }

    @Test
    fun `RefundResultJson surfaces error string when refund fails`() {
        val raw = """{"processed": false, "amount": null, "error": "stripe_error"}"""
        val refund = json.decodeFromString<RefundResultJson>(raw)

        assertFalse(refund.processed)
        assertEquals(null, refund.amount)
        assertEquals("stripe_error", refund.error)
    }

    // -- DeliveryMatchJson transaction parity (iOS c871184) --

    @Test
    fun `DeliveryMatchJson decodes transaction_status string`() {
        val raw = fixture("delivery_match_full.json")
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertEquals("captured", match.transactionStatus)
    }

    @Test
    fun `DeliveryMatchJson decodes nested transaction object`() {
        val raw = fixture("delivery_match_full.json")
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertNotNull(match.transaction)
        assertEquals(555, match.transaction!!.id)
        assertEquals("captured", match.transaction!!.status)
        assertEquals("150.50", match.transaction!!.totalAmount)
        assertEquals("15.05", match.transaction!!.platformFee)
        assertEquals("135.45", match.transaction!!.carrierAmount)
        assertEquals("CAD", match.transaction!!.currency)
    }

    @Test
    fun `MatchTransactionJson decodes amount fields when sent as numbers`() {
        val raw = """
            {
              "id": 1,
              "status": "pending",
              "total_amount": 99.99,
              "platform_fee": 9.99,
              "carrier_amount": 90.0
            }
        """.trimIndent()
        val txn = json.decodeFromString<MatchTransactionJson>(raw)

        assertEquals(1, txn.id)
        assertEquals("pending", txn.status)
        assertEquals("99.99", txn.totalAmount)
        assertEquals("9.99", txn.platformFee)
        assertEquals("90.0", txn.carrierAmount)
    }

    @Test
    fun `MatchTransactionJson decodes stripe error fields when present`() {
        val raw = """
            {
              "id": 2,
              "status": "requires_action",
              "requires_action_at": "2026-04-01T12:00:00Z",
              "error_code": "card_declined",
              "error_message": "Your card was declined."
            }
        """.trimIndent()
        val txn = json.decodeFromString<MatchTransactionJson>(raw)

        assertEquals("requires_action", txn.status)
        assertEquals("2026-04-01T12:00:00Z", txn.requiresActionAt)
        assertEquals("card_declined", txn.errorCode)
        assertEquals("Your card was declined.", txn.errorMessage)
    }

    @Test
    fun `MatchTransactionJson defaults all nullable fields to null`() {
        val txn = json.decodeFromString<MatchTransactionJson>("""{"id": 9}""")

        assertEquals(9, txn.id)
        assertEquals(null, txn.status)
        assertEquals(null, txn.totalAmount)
        assertEquals(null, txn.currency)
        assertEquals(null, txn.errorCode)
    }

    // -- CarrierTripInfoJson shared pickup/delivery window (iOS parity c6c63db) --

    @Test
    fun `CarrierTripInfoJson decodes pickup_date and delivery_date when present`() {
        val raw = fixture("delivery_match_full.json")
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertNotNull(match.carrierTrip)
        assertEquals("2026-04-02", match.carrierTrip!!.pickupDate)
        assertEquals("2026-04-03", match.carrierTrip!!.deliveryDate)
        // Legacy fields remain populated for fallback
        assertEquals("2026-04-01", match.carrierTrip!!.departureDate)
        assertEquals("2026-04-03", match.carrierTrip!!.arrivalDate)
    }

    @Test
    fun `CarrierTripInfoJson defaults pickup_date and delivery_date to null when absent`() {
        val raw = """{"id": 7, "origin_city": "A", "destination_city": "B"}"""
        val trip = json.decodeFromString<CarrierTripInfoJson>(raw)

        assertEquals(7, trip.id)
        assertEquals(null, trip.pickupDate)
        assertEquals(null, trip.deliveryDate)
    }

    // -- CounterOfferResponseJson --

    @Test
    fun `CounterOfferResponseJson decodes`() {
        val raw = fixture("counter_offer_response.json")
        val response = json.decodeFromString<CounterOfferResponseJson>(raw)

        assertTrue(response.success)
        assertEquals("Counter offer submitted", response.message)
        assertNotNull(response.data)
        assertTrue(response.data!!.isCounterOffer)
        assertEquals(135.0, response.data!!.agreedPrice!!, 0.001)
        assertEquals(1, response.data!!.counterOfferRound)
        assertEquals(2, response.data!!.remainingCounterOffers)
    }

    // -- PickupCodeResponseJson --

    @Test
    fun `PickupCodeResponseJson decodes`() {
        val raw = fixture("pickup_code_response.json")
        val response = json.decodeFromString<PickupCodeResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("654321", response.data!!.code)
        assertNotNull(response.data!!.expiresAt)
        assertNotNull(response.data!!.generatedAt)
    }

    // -- MatchCreationRequestJson encoding --

    @Test
    fun `MatchCreationRequestJson encodes`() {
        val request = MatchCreationRequestJson(
            tripId = 1,
            packageRequestId = 10,
            agreedPrice = 150.0,
            carrierMessage = "I can carry this",
        )
        val encoded = json.encodeToString(MatchCreationRequestJson.serializer(), request)
        assertTrue(encoded.contains("\"trip_id\":1"))
        assertTrue(encoded.contains("\"package_request_id\":10"))
        assertTrue(encoded.contains("\"agreed_price\":150.0"))
    }

    // -- ShipperCounterOfferRequestJson encoding --

    @Test
    fun `ShipperCounterOfferRequestJson encodes with counter-offer flag`() {
        val request = ShipperCounterOfferRequestJson(
            proposedPrice = 135.0,
            message = "Can you do less?",
            isCounterOffer = true,
        )
        val encoded = json.encodeToString(ShipperCounterOfferRequestJson.serializer(), request)
        // Round-trip verification — more reliable than string matching
        val decoded = json.decodeFromString<ShipperCounterOfferRequestJson>(encoded)
        assertEquals(135.0, decoded.proposedPrice, 0.001)
        assertTrue(decoded.isCounterOffer)
        assertEquals("Can you do less?", decoded.message)
    }
}
