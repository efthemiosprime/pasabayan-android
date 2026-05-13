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

    @Test
    fun `planning trip emits every changed route and schedule field`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "Vancouver", originCountry = "CA",
            destinationCity = "Calgary", destinationCountry = "CA",
            pickupAddress = "1 Pacific Blvd", dropoffAddress = "9 Stampede Trail",
            departureMillis = dep, arrivalMillis = arr,
            sharedPickupMillis = sharedPickup, sharedDeliveryMillis = sharedDelivery,
            weightText = "12.5", notesText = "Fragile",
            originalDepartureDate = "2026-03-01T08:00:00Z",
            originalArrivalDate = "2026-03-01T14:00:00Z",
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalNotes = "Old notes",
        )

        assertEquals("Vancouver", request.originCity)
        assertEquals("Calgary", request.destinationCity)
        assertEquals("1 Pacific Blvd", request.pickupAddress)
        assertEquals("9 Stampede Trail", request.dropoffAddress)
        assertEquals(DateTimeParsing.formatApiDateTime(dep), request.departureDate)
        assertEquals(DateTimeParsing.formatApiDateTime(arr), request.arrivalDate)
        assertEquals(DateTimeParsing.formatApiDateTime(sharedPickup), request.pickupDate)
        assertEquals(DateTimeParsing.formatApiDateTime(sharedDelivery), request.deliveryDate)
        assertEquals(12.5, request.availableWeightKg!!, 0.001)
        assertEquals("Fragile", request.specialNotes)
    }

    @Test
    fun `locked trip drops every route and schedule field even when text differs`() {
        val request = buildUpdateRequest(
            routeLocked = true,
            originCity = "Edited", originCountry = "ZZ",
            destinationCity = "Edited", destinationCountry = "ZZ",
            pickupAddress = "Edited", dropoffAddress = "Edited",
            departureMillis = dep, arrivalMillis = arr,
            sharedPickupMillis = sharedPickup, sharedDeliveryMillis = sharedDelivery,
            weightText = "12.5", notesText = "New note",
            originalDepartureDate = null,
            originalArrivalDate = null,
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalNotes = "Old note",
        )

        assertNull(request.originCity)
        assertNull(request.destinationCity)
        assertNull(request.pickupAddress)
        assertNull(request.dropoffAddress)
        assertNull(request.departureDate)
        assertNull(request.arrivalDate)
        assertNull(request.pickupDate)
        assertNull(request.deliveryDate)
        // Capacity + notes still flow through — iOS keeps notes always editable, and a locked
        // trip on Android still accepts a weight tweak via PUT /trips/{id}.
        assertEquals(12.5, request.availableWeightKg!!, 0.001)
        assertEquals("New note", request.specialNotes)
    }

    @Test
    fun `unchanged dates are not re-sent`() {
        val sameDepartureWire = "2026-04-01T08:00:00Z"
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "Toronto", originCountry = "CA",
            destinationCity = "Montreal", destinationCountry = "CA",
            pickupAddress = "1 A St", dropoffAddress = "2 B St",
            departureMillis = dep, arrivalMillis = arr,
            sharedPickupMillis = null, sharedDeliveryMillis = null,
            weightText = "10.0", notesText = "Same",
            originalDepartureDate = sameDepartureWire,
            originalArrivalDate = DateTimeParsing.formatApiDateTime(arr),
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalNotes = "Same",
        )
        assertNull(request.departureDate)
        assertNull(request.arrivalDate)
        assertNull(request.pickupDate)
        assertNull(request.deliveryDate)
    }

    @Test
    fun `no-op save emits an empty body`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "", originCountry = "",
            destinationCity = "", destinationCountry = "",
            pickupAddress = "", dropoffAddress = "",
            departureMillis = null, arrivalMillis = null,
            sharedPickupMillis = null, sharedDeliveryMillis = null,
            weightText = "10.0", notesText = "Same note",
            originalDepartureDate = null,
            originalArrivalDate = null,
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalNotes = "Same note",
        )

        val encoded = json.encodeToString(
            com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson.serializer(),
            request,
        )
        assertEquals("{}", encoded)
    }

    @Test
    fun `wire body uses snake case for route and schedule keys`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "Vancouver", originCountry = "CA",
            destinationCity = "Calgary", destinationCountry = "CA",
            pickupAddress = "1 Pacific Blvd", dropoffAddress = "9 Stampede Trail",
            departureMillis = dep, arrivalMillis = arr,
            sharedPickupMillis = sharedPickup, sharedDeliveryMillis = sharedDelivery,
            weightText = "12.5", notesText = "Fragile",
            originalDepartureDate = null,
            originalArrivalDate = null,
            originalPickupDate = null,
            originalDeliveryDate = null,
            originalWeightKg = 10.0,
            originalNotes = null,
        )
        val encoded = json.encodeToString(
            com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson.serializer(),
            request,
        )
        assertTrue(encoded.contains("\"origin_city\":\"Vancouver\""))
        assertTrue(encoded.contains("\"departure_date\":"))
        assertTrue(encoded.contains("\"arrival_date\":"))
        assertTrue(encoded.contains("\"pickup_date\":"))
        assertTrue(encoded.contains("\"delivery_date\":"))
        assertFalse(encoded.contains("\"departureDate\""))
    }
}
