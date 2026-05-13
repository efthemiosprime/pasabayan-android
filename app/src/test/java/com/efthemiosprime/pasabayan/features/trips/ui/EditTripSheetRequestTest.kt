package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditTripSheetRequestTest {

    private val json = Json { encodeDefaults = false }

    private val dep = DateTimeParsing.parseApiDateTime("2026-04-01T08:00:00Z")!!
    private val arr = DateTimeParsing.parseApiDateTime("2026-04-01T14:00:00Z")!!
    private val sharedPickup = DateTimeParsing.parseApiDateTime("2026-04-01T06:30:00Z")!!
    private val sharedDelivery = DateTimeParsing.parseApiDateTime("2026-04-01T15:00:00Z")!!

    private fun request(
        routeLocked: Boolean = false,
        isLandTransport: Boolean = false,
        priceText: String = "15",
        originalPricePerKg: Double? = null,
        originalFlatTripPrice: Double? = null,
        weightText: String = "12.5",
        spaceText: String = "60",
        originalWeightKg: Double? = 10.0,
        originalSpaceLiters: Double? = 50.0,
        notesText: String = "Fragile",
        originalNotes: String? = "Old notes",
    ) = buildUpdateRequest(
        routeLocked = routeLocked,
        originCity = "Vancouver", originCountry = "CA",
        destinationCity = "Calgary", destinationCountry = "CA",
        pickupAddress = "1 Pacific Blvd", dropoffAddress = "9 Stampede Trail",
        departureMillis = dep, arrivalMillis = arr,
        sharedPickupMillis = sharedPickup, sharedDeliveryMillis = sharedDelivery,
        weightText = weightText, spaceText = spaceText,
        priceText = priceText, isLandTransport = isLandTransport,
        notesText = notesText,
        originalDepartureDate = "2026-03-01T08:00:00Z",
        originalArrivalDate = "2026-03-01T14:00:00Z",
        originalPickupDate = null,
        originalDeliveryDate = null,
        originalWeightKg = originalWeightKg,
        originalSpaceLiters = originalSpaceLiters,
        originalPricePerKg = originalPricePerKg,
        originalFlatTripPrice = originalFlatTripPrice,
        originalNotes = originalNotes,
    )

    @Test
    fun `flight planning trip routes price into pricePerKg`() {
        val r = request(isLandTransport = false, priceText = "18.5")
        assertEquals(18.5, r.pricePerKg!!, 0.001)
        assertNull(r.flatTripPrice)
    }

    @Test
    fun `land planning trip routes price into flatTripPrice`() {
        val r = request(isLandTransport = true, priceText = "120")
        assertEquals(120.0, r.flatTripPrice!!, 0.001)
        assertNull(r.pricePerKg)
    }

    @Test
    fun `unchanged price is not re-sent`() {
        val flight = request(isLandTransport = false, priceText = "15", originalPricePerKg = 15.0)
        assertNull(flight.pricePerKg)
        val land = request(isLandTransport = true, priceText = "120", originalFlatTripPrice = 120.0)
        assertNull(land.flatTripPrice)
    }

    @Test
    fun `locked trip drops both pricing fields`() {
        val r = request(routeLocked = true, isLandTransport = false, priceText = "99")
        assertNull(r.pricePerKg)
        assertNull(r.flatTripPrice)
    }

    @Test
    fun `planning trip emits every changed route schedule capacity and price field`() {
        val r = request()
        assertEquals("Vancouver", r.originCity)
        assertEquals(DateTimeParsing.formatApiDateTime(dep), r.departureDate)
        assertEquals(12.5, r.availableWeightKg!!, 0.001)
        assertEquals(60.0, r.availableSpaceLiters!!, 0.001)
        assertEquals(15.0, r.pricePerKg!!, 0.001)
        assertEquals("Fragile", r.specialNotes)
    }

    @Test
    fun `locked trip drops every route and schedule field even when text differs`() {
        val r = request(routeLocked = true, isLandTransport = false, priceText = "99")
        assertNull(r.originCity)
        assertNull(r.departureDate)
        assertNull(r.pickupDate)
        assertNull(r.pricePerKg)
        // Capacity + notes still flow through.
        assertEquals(12.5, r.availableWeightKg!!, 0.001)
        assertEquals(60.0, r.availableSpaceLiters!!, 0.001)
        assertEquals("Fragile", r.specialNotes)
    }

    @Test
    fun `no-op save emits an empty body`() {
        val r = buildUpdateRequest(
            routeLocked = false,
            originCity = "", originCountry = "",
            destinationCity = "", destinationCountry = "",
            pickupAddress = "", dropoffAddress = "",
            departureMillis = null, arrivalMillis = null,
            sharedPickupMillis = null, sharedDeliveryMillis = null,
            weightText = "10.0", spaceText = "50",
            priceText = "15", isLandTransport = false,
            notesText = "Same note",
            originalDepartureDate = null,
            originalArrivalDate = null,
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalSpaceLiters = 50.0,
            originalPricePerKg = 15.0,
            originalFlatTripPrice = null,
            originalNotes = "Same note",
        )
        val encoded = json.encodeToString(
            com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson.serializer(),
            r,
        )
        assertEquals("{}", encoded)
    }

    @Test
    fun `wire body uses snake case for all keys including pricing`() {
        val r = request()
        val encoded = json.encodeToString(
            com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson.serializer(),
            r,
        )
        assertTrue(encoded.contains("\"origin_city\":\"Vancouver\""))
        assertTrue(encoded.contains("\"departure_date\":"))
        assertTrue(encoded.contains("\"available_weight_kg\":12.5"))
        assertTrue(encoded.contains("\"available_space_liters\":60.0"))
        assertTrue(encoded.contains("\"price_per_kg\":15.0"))
        assertFalse(encoded.contains("\"flat_trip_price\""))
        assertFalse(encoded.contains("\"pricePerKg\""))
    }
}
