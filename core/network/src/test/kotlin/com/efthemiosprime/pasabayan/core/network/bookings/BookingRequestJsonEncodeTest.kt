package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.BookingStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    // -- CarrierRequestBodyJson encode --

    @Test
    fun `CarrierRequestBodyJson encodes`() {
        val body = CarrierRequestBodyJson(
            proposedPrice = 150.0,
            message = "I can carry this",
        )
        val decoded = json.decodeFromString<CarrierRequestBodyJson>(
            json.encodeToString(CarrierRequestBodyJson.serializer(), body),
        )
        assertEquals(150.0, decoded.proposedPrice, 0.001)
        assertEquals("I can carry this", decoded.message)
    }

    // -- ShipperTripRequestJson encode --

    @Test
    fun `ShipperTripRequestJson encodes`() {
        val body = ShipperTripRequestJson(
            proposedPrice = 120.0,
            message = "Please carry my package",
        )
        val decoded = json.decodeFromString<ShipperTripRequestJson>(
            json.encodeToString(ShipperTripRequestJson.serializer(), body),
        )
        assertEquals(120.0, decoded.proposedPrice, 0.001)
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

    // -- CarrierRequestResponseJson decode --

    @Test
    fun `CarrierRequestResponseJson decodes`() {
        val raw = fixture("carrier_request_response.json")
        val response = json.decodeFromString<CarrierRequestResponseJson>(raw)

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(300, response.data!!.id)
        assertEquals(150.0, response.data!!.proposedPrice!!, 0.001)
    }
}
