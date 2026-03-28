package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.trips.PackageSummaryJson
import com.efthemiosprime.pasabayan.core.network.trips.PendingTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripEarningsBreakdownJson
import com.efthemiosprime.pasabayan.core.network.trips.TripJson
import com.efthemiosprime.pasabayan.core.network.trips.TripMatchPackageJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TripMapperTest {

    @Test
    fun `TripJson toDomain maps all core fields`() {
        val json = TripJson(
            id = 1,
            carrierId = 42,
            originCity = "Toronto",
            originCountry = "Canada",
            destinationCity = "Vancouver",
            destinationCountry = "Canada",
            tripStatus = TripStatus.ACTIVE,
            transportationMethod = TransportationMethod.FLIGHT,
            availableWeightKg = 25.0,
            pricePerKg = 15.0,
            specialNotes = "Fragile",
        )
        val trip = json.toDomain()

        assertEquals(1, trip.id)
        assertEquals(42, trip.carrierId)
        assertEquals("Toronto", trip.originCity)
        assertEquals("Vancouver", trip.destinationCity)
        assertEquals(TripStatus.ACTIVE, trip.tripStatus)
        assertEquals(TransportationMethod.FLIGHT, trip.transportationMethod)
        assertEquals(25.0, trip.availableWeightKg!!, 0.001)
        assertEquals(15.0, trip.pricePerKg!!, 0.001)
        assertEquals("Fragile", trip.specialNotes)
    }

    @Test
    fun `TripJson toDomain maps carrier UserSummary`() {
        val json = TripJson(
            id = 1,
            carrier = UserSummary(id = 42, name = "John", rating = "4.5"),
        )
        val trip = json.toDomain()
        assertNotNull(trip.carrier)
        assertEquals(42, trip.carrier!!.id)
        assertEquals("John", trip.carrier!!.name)
    }

    @Test
    fun `TripJson toDomain maps earnings breakdown`() {
        val json = TripJson(
            id = 1,
            tripEarningsBreakdown = TripEarningsBreakdownJson(
                deliveredAmount = 500.0,
                deliveredCurrency = "CAD",
                pendingAmount = 200.0,
                pendingCurrency = "CAD",
            ),
        )
        val trip = json.toDomain()
        assertNotNull(trip.tripEarningsBreakdown)
        assertEquals(500.0, trip.tripEarningsBreakdown!!.deliveredAmount, 0.001)
    }

    @Test
    fun `TripJson toDomain maps pending requests`() {
        val json = TripJson(
            id = 1,
            pendingRequests = listOf(
                PendingTripRequestJson(id = 10, shipperId = 5, shipperName = "Alice"),
            ),
        )
        val trip = json.toDomain()
        assertEquals(1, trip.pendingRequests!!.size)
        assertEquals("Alice", trip.pendingRequests!![0].shipperName)
    }

    @Test
    fun `TripJson toDomain handles null optional fields`() {
        val json = TripJson(id = 99)
        val trip = json.toDomain()
        assertNull(trip.carrier)
        assertNull(trip.specialNotes)
        assertNull(trip.tripEarningsBreakdown)
        assertNull(trip.pendingRequests)
    }

    @Test
    fun `TripMatchPackageJson toDomain maps fields`() {
        val json = TripMatchPackageJson(
            id = 100,
            matchStatus = MatchStatus.CONFIRMED,
            agreedPrice = 150.0,
            packageInfo = PackageSummaryJson(
                id = 55, description = "Books", weightKg = 5.0,
            ),
            shipper = UserSummary(id = 8, name = "Jane"),
            chatConversationId = 77,
        )
        val match = json.toDomain()
        assertEquals(100, match.id)
        assertEquals(MatchStatus.CONFIRMED, match.matchStatus)
        assertEquals(150.0, match.agreedPrice!!, 0.001)
        assertEquals("Books", match.packageDescription)
        assertEquals(5.0, match.packageWeightKg!!, 0.001)
        assertEquals(55, match.packageId)
        assertEquals("Jane", match.shipper!!.name)
        assertEquals(77, match.chatConversationId)
    }
}
