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

    @Test
    fun `DeliveryMatchJson decodes auto_charge nested object`() {
        val raw = fixture("delivery_match_full.json")
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertNotNull(match.autoCharge)
        assertEquals("pending", match.autoCharge!!.status)
        assertEquals(150.50, match.autoCharge!!.amount!!, 0.001)
        assertEquals("CAD", match.autoCharge!!.currency)
    }

    @Test
    fun `DeliveryMatchJson decodes with minimal fields`() {
        val raw = """{"id": 50, "match_status": "pending", "agreed_price": 100.0}"""
        val match = json.decodeFromString<DeliveryMatchJson>(raw)

        assertEquals(50, match.id)
        assertEquals(MatchStatus.PENDING, match.matchStatus)
        assertEquals(100.0, match.agreedPrice!!, 0.001)
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
