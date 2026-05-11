package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.BookingStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingRequestJsonEncodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/bookings/$name")!!
            .bufferedReader().readText()

    // -- BookingJson decode --

    @Test
    fun `BookingJson decodes full booking`() {
        val raw = fixture("booking_full.json")
        val booking = json.decodeFromString<BookingJson>(raw)

        assertEquals(200, booking.id)
        assertEquals(10, booking.packageRequestId)
        assertEquals(1, booking.tripId)
        assertEquals(42, booking.carrierId)
        assertEquals(5, booking.shipperId)
        assertEquals("123 Main St, Toronto", booking.pickupLocation)
        assertEquals("456 Oak Ave, Montreal", booking.deliveryLocation)
        assertEquals(150.50, booking.agreedPrice!!, 0.001)
        assertEquals(BookingStatus.CONFIRMED, booking.status)
        assertEquals("Handle with care", booking.notes)
        assertEquals("TRK-123456", booking.trackingNumber)
        assertEquals(10, booking.autoCancelAfterDays)
        assertEquals(10, booking.platformFeePercent)
        assertEquals("confirmed", booking.rawMatchStatus)
        assertNotNull(booking.carrier)
        assertNotNull(booking.shipper)
    }

    // -- CounterOffer request bodies (iOS parity 8c9646d) --

    @Test
    fun `ShipperCounterOfferRequestJson omits null counter-offer fields`() {
        val body = ShipperCounterOfferRequestJson(
            proposedPrice = 150.0,
            message = "Initial offer",
            isCounterOffer = false,
        )
        val encoded = json.encodeToString(ShipperCounterOfferRequestJson.serializer(), body)
        assertFalse("Expected initial request to omit original_match_id", encoded.contains("original_match_id"))
        assertFalse("Expected initial request to omit original_price", encoded.contains("original_price"))
        assertTrue(encoded.contains("\"is_counter_offer\":false"))
    }

    @Test
    fun `ShipperCounterOfferRequestJson encodes counter-offer fields when set`() {
        val body = ShipperCounterOfferRequestJson(
            proposedPrice = 135.0,
            message = "Can you do less?",
            isCounterOffer = true,
            originalMatchId = 300,
            originalPrice = 150.0,
        )
        val encoded = json.encodeToString(ShipperCounterOfferRequestJson.serializer(), body)
        val decoded = json.decodeFromString<ShipperCounterOfferRequestJson>(encoded)

        assertEquals(135.0, decoded.proposedPrice, 0.001)
        assertTrue(decoded.isCounterOffer)
        assertEquals(300, decoded.originalMatchId)
        assertEquals(150.0, decoded.originalPrice!!, 0.001)
    }

    @Test
    fun `CarrierCounterOfferRequestJson encodes counter-offer fields when set`() {
        val body = CarrierCounterOfferRequestJson(
            proposedPrice = 160.0,
            message = "I can handle this safely",
            isCounterOffer = true,
            originalMatchId = 301,
            originalPrice = 150.0,
        )
        val encoded = json.encodeToString(CarrierCounterOfferRequestJson.serializer(), body)
        val decoded = json.decodeFromString<CarrierCounterOfferRequestJson>(encoded)

        assertEquals(160.0, decoded.proposedPrice, 0.001)
        assertTrue(decoded.isCounterOffer)
        assertEquals(301, decoded.originalMatchId)
        assertEquals(150.0, decoded.originalPrice!!, 0.001)
    }

    // -- Request envelopes with negotiation metadata (iOS parity 8c9646d) --

    @Test
    fun `CarrierRequestResponseJson decodes envelope with warnings and negotiation`() {
        val raw = fixture("carrier_request_response.json")
        val response = json.decodeFromString<CarrierRequestResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(300, response.data!!.id)
        assertEquals(MatchStatus.CARRIER_REQUESTED, response.data!!.matchStatus)
        assertEquals(listOf("pickup_address_outside_range"), response.warnings)
        assertEquals(true, response.negotiationNeeded)
        assertEquals(false, response.isCounterOffer)
    }

    @Test
    fun `ShipperRequestResponseJson decodes envelope and surfaces counter-offer`() {
        val raw = fixture("shipper_request_response.json")
        val response = json.decodeFromString<ShipperRequestResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(MatchStatus.SHIPPER_REQUESTED, response.data!!.matchStatus)
        assertEquals(1, response.data!!.counterOfferRound)
        assertEquals(2, response.data!!.remainingCounterOffers)
        assertEquals(true, response.isCounterOffer)
        assertNull(response.warnings)
    }

    @Test
    fun `CarrierRequestResponseJson tolerates missing envelope fields`() {
        val raw = """{"success": true, "message": "OK", "data": {"id": 1, "match_status": "pending"}}"""
        val response = json.decodeFromString<CarrierRequestResponseJson>(raw)

        assertNull(response.warnings)
        assertNull(response.negotiationNeeded)
        assertNull(response.isCounterOffer)
    }

    // -- CreateBookingRequestJson encode --

    @Test
    fun `CreateBookingRequestJson encodes with coordinates`() {
        val body = CreateBookingRequestJson(
            tripId = 1,
            packageRequestId = 10,
            pickupDate = "2026-04-05",
            notes = "Fragile items",
        )
        val decoded = json.decodeFromString<CreateBookingRequestJson>(
            json.encodeToString(CreateBookingRequestJson.serializer(), body),
        )
        assertEquals(1, decoded.tripId)
        assertEquals(10, decoded.packageRequestId)
        assertEquals("2026-04-05", decoded.pickupDate)
    }

    // -- UpdateLocationRequestJson encode --

    @Test
    fun `UpdateLocationRequestJson encodes`() {
        val body = UpdateLocationRequestJson(
            latitude = 43.6532,
            longitude = -79.3832,
            accuracy = 10.0,
        )
        val decoded = json.decodeFromString<UpdateLocationRequestJson>(
            json.encodeToString(UpdateLocationRequestJson.serializer(), body),
        )
        assertEquals(43.6532, decoded.latitude, 0.001)
        assertEquals(-79.3832, decoded.longitude, 0.001)
    }
}
