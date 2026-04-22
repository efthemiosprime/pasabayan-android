package com.efthemiosprime.pasabayan.core.network.trips

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResourceAsStream("api-fixtures/trips/$name")!!
            .bufferedReader().readText()

    @Test
    fun `TripJson decodes full flight trip`() {
        val raw = fixture("trip_full.json")
        val trip = json.decodeFromString<TripJson>(raw)

        assertEquals(1, trip.id)
        assertEquals(42, trip.carrierId)
        assertEquals("Toronto", trip.originCity)
        assertEquals("Vancouver", trip.destinationCity)
        assertEquals(TripStatus.ACTIVE, trip.tripStatus)
        assertEquals(TransportationMethod.FLIGHT, trip.transportationMethod)
        assertEquals(25.0, trip.availableWeightKg!!, 0.001)
        assertEquals(15.0, trip.pricePerKg!!, 0.001)
        assertEquals("Fragile items only", trip.specialNotes)
        assertEquals("123 Main St", trip.pickupAddress)
        assertEquals("456 Oak Ave", trip.dropoffAddress)
        assertTrue(trip.hasPendingRequests == true)
        assertEquals(2, trip.pendingRequestCount)
        assertEquals(3365.0, trip.distanceKm!!, 0.1)

        // Carrier (UserSummary)
        val carrier = requireNotNull(trip.carrier)
        assertEquals(42, carrier.id)
        assertEquals("John Carrier", carrier.name)
        assertEquals("4.8", carrier.rating)
        assertEquals("verified", carrier.verificationLevel)
    }

    @Test
    fun `TripJson decodes land transport with flat pricing`() {
        val raw = fixture("trip_land_pricing.json")
        val trip = json.decodeFromString<TripJson>(raw)

        assertEquals(2, trip.id)
        assertEquals(TransportationMethod.CAR, trip.transportationMethod)
        assertEquals(TripStatus.PLANNING, trip.tripStatus)
        assertEquals("flat", trip.pricingType)
        assertEquals("manual", trip.pricingMethod)
        assertEquals(200.0, trip.flatTripPrice!!, 0.001)
        // Flexible decode: weight as string "50"
        assertEquals(50.0, trip.availableWeightKg!!, 0.001)
        assertEquals(100.0, trip.availableSpaceLiters!!, 0.001)
    }

    @Test
    fun `TripsResponseJson decodes paginated response`() {
        val raw = fixture("trips_paginated.json")
        val response = json.decodeFromString<TripsResponseJson>(raw)

        assertEquals("Trips retrieved successfully", response.message)
        val page = requireNotNull(response.data)
        assertEquals(2, page.data.size)
        assertEquals(1, page.currentPage)
        assertEquals(3, page.lastPage)
        assertEquals(45, page.total)
        assertEquals(15, page.perPage)

        assertEquals("Toronto", page.data[0].originCity)
        assertEquals("Montreal", page.data[1].originCity)
    }

    @Test
    fun `TripMatchPackageJson decodes with flexible agreedPrice`() {
        val raw = fixture("trip_match.json")
        val match = json.decodeFromString<TripMatchPackageJson>(raw)

        assertEquals(100, match.id)
        assertEquals(MatchStatus.CONFIRMED, match.matchStatus)
        assertEquals(150.50, match.agreedPrice!!, 0.001)
        assertEquals(77, match.chatConversationId)
        assertNotNull(match.confirmedAt)

        val pkg = requireNotNull(match.packageInfo)
        assertEquals(55, pkg.id)
        assertEquals("Electronics bundle", pkg.description)

        val shipper = requireNotNull(match.shipper)
        assertEquals(8, shipper.id)
        assertEquals("Jane Shipper", shipper.name)
    }

    @Test
    fun `TripJson decodes with missing optional fields`() {
        val raw = """{"id": 99, "trip_status": "cancelled", "transportation_method": "other"}"""
        val trip = json.decodeFromString<TripJson>(raw)

        assertEquals(99, trip.id)
        assertEquals(TripStatus.CANCELLED, trip.tripStatus)
        assertNull(trip.carrier)
        assertNull(trip.specialNotes)
        assertNull(trip.pricePerKg)
        assertNull(trip.flatTripPrice)
        assertNull(trip.tripEarningsBreakdown)
    }

    @Test
    fun `CreateTripRequestJson encodes all fields`() {
        val request = CreateTripRequestJson(
            originCity = "Toronto",
            originCountry = "Canada",
            destinationCity = "Vancouver",
            destinationCountry = "Canada",
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-01T14:00:00Z",
            availableWeightKg = 25.0,
            transportationMethod = "flight",
            pricePerKg = 15.0,
            autoRequestPackageId = 55,
            proposedPrice = 150.0,
            requestMessage = "I can carry this",
        )
        val encoded = json.encodeToString(CreateTripRequestJson.serializer(), request)
        assertTrue(encoded.contains("\"origin_city\":\"Toronto\""))
        assertTrue(encoded.contains("\"auto_request_package_id\":55"))
        assertTrue(encoded.contains("\"proposed_price\":150.0"))
    }

    @Test
    fun `TripEarningsBreakdownJson decodes`() {
        val raw = """{
            "delivered_amount": "500.00",
            "delivered_currency": "CAD",
            "pending_amount": 200.50,
            "pending_currency": "CAD"
        }"""
        val breakdown = json.decodeFromString<TripEarningsBreakdownJson>(raw)
        assertEquals(500.0, breakdown.deliveredAmount!!, 0.001)
        assertEquals("CAD", breakdown.deliveredCurrency)
        assertEquals(200.5, breakdown.pendingAmount!!, 0.001)
    }

    @Test
    fun `PendingTripRequestJson decodes`() {
        val raw = """{
            "id": 10,
            "shipper_id": 5,
            "shipper_name": "Alice",
            "package_description": "Books",
            "proposed_price": "75.00",
            "message": "Please carry my books"
        }"""
        val req = json.decodeFromString<PendingTripRequestJson>(raw)
        assertEquals(10, req.id)
        assertEquals("Alice", req.shipperName)
        assertEquals(75.0, req.proposedPrice!!, 0.001)
    }

    @Test
    fun `TripTemplateResponseJson decodes package template payload`() {
        val raw = fixture("trip_template_response.json")
        val response = json.decodeFromString<TripTemplateResponseJson>(raw)

        assertEquals("Trip template generated", response.message)
        val template = requireNotNull(response.data)
        assertEquals("Toronto", template.originCity)
        assertEquals("Montreal", template.destinationCity)
        assertEquals(15.5, template.suggestedWeightKg!!, 0.001)

        val packageDetails = requireNotNull(template.packageDetails)
        assertEquals(88, packageDetails.id)
        assertTrue(packageDetails.isFragile)
    }

    @Test
    fun `TripMatchesResponseJson decodes trip and matches`() {
        val raw = fixture("trip_matches_response.json")
        val response = json.decodeFromString<TripMatchesResponseJson>(raw)

        val trip = requireNotNull(response.trip)
        assertEquals(501, trip.id)
        assertEquals(TransportationMethod.CAR, trip.transportationMethod)
        assertEquals(1, response.matches.size)
        assertEquals(MatchStatus.CONFIRMED, response.matches.first().matchStatus)
    }

    @Test
    fun `PopularRoutesResponseJson decodes routes payload`() {
        val raw = fixture("popular_routes.json")
        val response = json.decodeFromString<PopularRoutesResponseJson>(raw)

        assertTrue(response.success)
        assertEquals(2, response.data.size)
        assertEquals("Toronto", response.data.first().originCity)
        assertEquals("Montreal", response.data.first().destinationCity)
        assertEquals(9, response.data.first().packageCount)
        assertEquals(47.25, response.data.first().averagePrice!!, 0.001)
    }

    @Test
    fun `RouteActivitySummaryResponseJson decodes carrier summary`() {
        val raw = fixture("route_activity_summary.json")
        val response = json.decodeFromString<RouteActivitySummaryResponseJson>(raw)

        assertTrue(response.success)
        assertEquals(12, response.data.totalTrips)
        assertEquals(3, response.data.activeTrips)
        assertEquals(7, response.data.completedTrips)
        assertEquals(845.75, response.data.totalEarnings!!, 0.001)
        assertEquals("CAD", response.data.currency)
    }
}
