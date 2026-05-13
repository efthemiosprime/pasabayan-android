package com.efthemiosprime.pasabayan.features.trips.ui

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditTripSheetRequestTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun `planning trip emits every changed route field`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "Vancouver",
            originCountry = "CA",
            destinationCity = "Calgary",
            destinationCountry = "CA",
            pickupAddress = "1 Pacific Blvd",
            dropoffAddress = "9 Stampede Trail",
            weightText = "12.5",
            notesText = "Fragile",
            originalWeightKg = 10.0,
            originalNotes = "Old notes",
        )

        assertEquals("Vancouver", request.originCity)
        assertEquals("CA", request.originCountry)
        assertEquals("Calgary", request.destinationCity)
        assertEquals("CA", request.destinationCountry)
        assertEquals("1 Pacific Blvd", request.pickupAddress)
        assertEquals("9 Stampede Trail", request.dropoffAddress)
        assertEquals(12.5, request.availableWeightKg!!, 0.001)
        assertEquals("Fragile", request.specialNotes)
    }

    @Test
    fun `locked trip drops every route field even when text differs from trip`() {
        val request = buildUpdateRequest(
            routeLocked = true,
            originCity = "Edited origin city",
            originCountry = "ZZ",
            destinationCity = "Edited destination",
            destinationCountry = "ZZ",
            pickupAddress = "Edited pickup",
            dropoffAddress = "Edited dropoff",
            weightText = "12.5",
            notesText = "New note",
            originalWeightKg = 10.0,
            originalNotes = "Old note",
        )

        assertNull(request.originCity)
        assertNull(request.originCountry)
        assertNull(request.destinationCity)
        assertNull(request.destinationCountry)
        assertNull(request.pickupAddress)
        assertNull(request.dropoffAddress)
        // Capacity + notes still flow through — iOS keeps notes always editable, and a locked
        // trip on Android still accepts a weight tweak via PUT /trips/{id}.
        assertEquals(12.5, request.availableWeightKg!!, 0.001)
        assertEquals("New note", request.specialNotes)
    }

    @Test
    fun `no-op save emits an empty body`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "",
            originCountry = "",
            destinationCity = "",
            destinationCountry = "",
            pickupAddress = "",
            dropoffAddress = "",
            weightText = "10.0",
            notesText = "Same note",
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
    fun `blank notes clears to null and is sent only when it differs`() {
        // Trip had a note; user cleared it. The wire should NOT carry the cleared value
        // because TripUpdateRequestJson currently omits null fields entirely. iOS parity:
        // notes-only edits send `special_notes` with the new value or omit when unchanged.
        val cleared = buildUpdateRequest(
            routeLocked = false,
            originCity = "Toronto", originCountry = "CA",
            destinationCity = "Montreal", destinationCountry = "CA",
            pickupAddress = "1 A St", dropoffAddress = "2 B St",
            weightText = "10.0",
            notesText = "",
            originalWeightKg = 10.0,
            originalNotes = "Old note",
        )
        // The clear is detected (notesNormalized is null) — request.specialNotes stays null,
        // which today means "no change" on the wire. Recording the limitation: this is a
        // known iOS parity gap covered by Slice C (`includeSpecialNotesNull` flag).
        assertNull(cleared.specialNotes)

        val keptSame = buildUpdateRequest(
            routeLocked = false,
            originCity = "Toronto", originCountry = "CA",
            destinationCity = "Montreal", destinationCountry = "CA",
            pickupAddress = "1 A St", dropoffAddress = "2 B St",
            weightText = "10.0",
            notesText = "Old note",
            originalWeightKg = 10.0,
            originalNotes = "Old note",
        )
        assertNull(keptSame.specialNotes)
    }

    @Test
    fun `route field encodes to snake case in wire body`() {
        val request = buildUpdateRequest(
            routeLocked = false,
            originCity = "Vancouver", originCountry = "CA",
            destinationCity = "Calgary", destinationCountry = "CA",
            pickupAddress = "1 Pacific Blvd", dropoffAddress = "9 Stampede Trail",
            weightText = "12.5",
            notesText = "Fragile",
            originalWeightKg = 10.0,
            originalNotes = null,
        )
        val encoded = json.encodeToString(
            com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson.serializer(),
            request,
        )
        assertTrue(encoded.contains("\"origin_city\":\"Vancouver\""))
        assertTrue(encoded.contains("\"destination_city\":\"Calgary\""))
        assertTrue(encoded.contains("\"pickup_address\":\"1 Pacific Blvd\""))
        assertTrue(encoded.contains("\"dropoff_address\":\"9 Stampede Trail\""))
        assertFalse(encoded.contains("\"originCity\""))
    }
}
