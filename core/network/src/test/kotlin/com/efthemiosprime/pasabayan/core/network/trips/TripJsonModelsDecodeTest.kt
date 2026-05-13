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

    // iOS parity (`TripPackageInfo` in `TripModels.swift`): the embedded `package` object on
    // `/trips/{id}/matches` exposes `pickup_city`, `delivery_city`, `fragile`, `package_type`
    // alongside the basics.
    @Test
    fun `PackageSummaryJson decodes pickup_city delivery_city fragile and package_type`() {
        val raw = """
            {
              "id": 60,
              "description": "Laptop and accessories",
              "package_weight_kg": 2.5,
              "pickup_city": "Manila",
              "delivery_city": "Cebu",
              "fragile": true,
              "package_type": "fragile"
            }
        """.trimIndent()
        val pkg = json.decodeFromString<PackageSummaryJson>(raw)
        assertEquals("Manila", pkg.pickupCity)
        assertEquals("Cebu", pkg.deliveryCity)
        assertEquals(true, pkg.fragile)
        assertEquals("fragile", pkg.packageType)
    }

    @Test
    fun `PackageSummaryJson leaves new fields null when server omits them`() {
        val raw = """{"id": 60, "description": "Books", "package_weight_kg": 3.0}"""
        val pkg = json.decodeFromString<PackageSummaryJson>(raw)
        assertNull(pkg.pickupCity)
        assertNull(pkg.deliveryCity)
        assertNull(pkg.fragile)
        assertNull(pkg.packageType)
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
    fun `TripUpdateRequestJson for non planning update omits route fields`() {
        val request = TripUpdateRequestJson(
            availableWeightKg = 10.0,
            specialNotes = "Updated notes",
            originCity = null,
            destinationCity = null,
            departureDate = null,
            arrivalDate = null,
        )
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)

        assertTrue(encoded.contains("\"available_weight_kg\":10.0"))
        assertTrue(encoded.contains("\"special_notes\":\"Updated notes\""))
        assertTrue(!encoded.contains("\"origin_city\""))
        assertTrue(!encoded.contains("\"destination_city\""))
        assertTrue(!encoded.contains("\"departure_date\""))
        assertTrue(!encoded.contains("\"arrival_date\""))
    }

    @Test
    fun `TripUpdateRequestJson for planning update includes route fields`() {
        val request = TripUpdateRequestJson(
            originCity = "Toronto",
            destinationCity = "Montreal",
            departureDate = "2026-08-01T08:00:00Z",
            arrivalDate = "2026-08-01T12:00:00Z",
        )
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)

        assertTrue(encoded.contains("\"origin_city\":\"Toronto\""))
        assertTrue(encoded.contains("\"destination_city\":\"Montreal\""))
        assertTrue(encoded.contains("\"departure_date\":\"2026-08-01T08:00:00Z\""))
        assertTrue(encoded.contains("\"arrival_date\":\"2026-08-01T12:00:00Z\""))
    }

    // -- iOS-parity shared pickup / delivery window (spec 12 / slice A) --

    @Test
    fun `TripJson decodes pickup_date and delivery_date when present`() {
        val raw = """
        {"id":1,"origin_city":"Toronto","destination_city":"Vancouver",
          "departure_date":"2026-08-01T08:00:00Z","arrival_date":"2026-08-02T08:00:00Z",
          "pickup_date":"2026-08-01T06:00:00Z","delivery_date":"2026-08-02T10:00:00Z"}
        """.trimIndent()
        val trip = json.decodeFromString<TripJson>(raw)
        assertEquals("2026-08-01T06:00:00Z", trip.pickupDate)
        assertEquals("2026-08-02T10:00:00Z", trip.deliveryDate)
    }

    @Test
    fun `TripJson leaves pickup_date and delivery_date null when absent`() {
        val raw = """{"id":1,"departure_date":"2026-08-01T08:00:00Z","arrival_date":"2026-08-02T08:00:00Z"}"""
        val trip = json.decodeFromString<TripJson>(raw)
        assertNull(trip.pickupDate)
        assertNull(trip.deliveryDate)
    }

    @Test
    fun `CreateTripRequestJson encodes pickup_date and delivery_date when set`() {
        val request = CreateTripRequestJson(
            originCity = "Toronto",
            originCountry = "Canada",
            destinationCity = "Vancouver",
            destinationCountry = "Canada",
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-01T14:00:00Z",
            availableWeightKg = 25.0,
            transportationMethod = "flight",
            pickupDate = "2026-04-01T06:30:00Z",
            deliveryDate = "2026-04-01T15:00:00Z",
        )
        val encoded = json.encodeToString(CreateTripRequestJson.serializer(), request)
        assertTrue(encoded.contains("\"pickup_date\":\"2026-04-01T06:30:00Z\""))
        assertTrue(encoded.contains("\"delivery_date\":\"2026-04-01T15:00:00Z\""))
    }

    @Test
    fun `CreateTripRequestJson omits pickup_date and delivery_date when null`() {
        val terse = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
        val request = CreateTripRequestJson(
            originCity = "Toronto",
            originCountry = "Canada",
            destinationCity = "Vancouver",
            destinationCountry = "Canada",
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-01T14:00:00Z",
            availableWeightKg = 25.0,
            transportationMethod = "flight",
        )
        val encoded = terse.encodeToString(CreateTripRequestJson.serializer(), request)
        assertTrue("expected pickup_date omitted, got $encoded", !encoded.contains("pickup_date"))
        assertTrue("expected delivery_date omitted, got $encoded", !encoded.contains("delivery_date"))
    }

    @Test
    fun `TripUpdateRequestJson encodes pickup_date and delivery_date when set`() {
        val request = TripUpdateRequestJson(
            pickupDate = "2026-08-01T06:00:00Z",
            deliveryDate = "2026-08-02T10:00:00Z",
        )
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue(encoded.contains("\"pickup_date\":\"2026-08-01T06:00:00Z\""))
        assertTrue(encoded.contains("\"delivery_date\":\"2026-08-02T10:00:00Z\""))
    }

    // -- iOS-parity explicit-null flags (slice C) --

    @Test
    fun `TripUpdateRequestJson omits pickup_date when null and flag is false`() {
        val request = TripUpdateRequestJson(pickupDate = null, includePickupDateNull = false)
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue("pickup_date should be omitted, got $encoded", !encoded.contains("\"pickup_date\""))
    }

    @Test
    fun `TripUpdateRequestJson emits explicit null pickup_date when flag is true`() {
        val request = TripUpdateRequestJson(pickupDate = null, includePickupDateNull = true)
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue(
            "expected explicit pickup_date null, got $encoded",
            encoded.contains("\"pickup_date\":null"),
        )
    }

    @Test
    fun `TripUpdateRequestJson emits explicit null delivery_date when flag is true`() {
        val request = TripUpdateRequestJson(deliveryDate = null, includeDeliveryDateNull = true)
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue(
            "expected explicit delivery_date null, got $encoded",
            encoded.contains("\"delivery_date\":null"),
        )
    }

    @Test
    fun `TripUpdateRequestJson prefers concrete value over explicit-null flag`() {
        val request = TripUpdateRequestJson(
            pickupDate = "2026-08-01T06:00:00Z",
            includePickupDateNull = true,
        )
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue(encoded.contains("\"pickup_date\":\"2026-08-01T06:00:00Z\""))
        assertTrue("expected no extra null, got $encoded", !encoded.contains("\"pickup_date\":null"))
    }

    @Test
    fun `TripUpdateRequestJson does not encode the include-null booleans themselves`() {
        val request = TripUpdateRequestJson(
            includePickupDateNull = true,
            includeDeliveryDateNull = true,
        )
        val encoded = json.encodeToString(TripUpdateRequestJson.serializer(), request)
        assertTrue(!encoded.contains("includePickupDateNull"))
        assertTrue(!encoded.contains("include_pickup_date_null"))
        assertTrue(!encoded.contains("includeDeliveryDateNull"))
        assertTrue(!encoded.contains("include_delivery_date_null"))
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
    fun `TripEarningsBreakdownJson decodes delivered_count and pending_count`() {
        val raw = """{
            "delivered_amount": 500.0,
            "delivered_currency": "CAD",
            "delivered_count": 3,
            "pending_amount": 200.5,
            "pending_currency": "CAD",
            "pending_count": 2
        }"""
        val breakdown = json.decodeFromString<TripEarningsBreakdownJson>(raw)
        assertEquals(3, breakdown.deliveredCount)
        assertEquals(2, breakdown.pendingCount)
    }

    @Test
    fun `TripEarningsBreakdownJson leaves counts null when server omits them`() {
        val raw = """{"delivered_amount": 500.0, "pending_amount": 200.5}"""
        val breakdown = json.decodeFromString<TripEarningsBreakdownJson>(raw)
        assertNull(breakdown.deliveredCount)
        assertNull(breakdown.pendingCount)
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

    // iOS parity: trip template package_details also carries `package_type` for the
    // create-from-package card.
    @Test
    fun `PackageTemplateDetailsJson decodes package_type when server returns it`() {
        val raw = """
            {
              "id": 88,
              "description": "Fragile electronics",
              "weight_kg": 5.2,
              "is_fragile": true,
              "urgency_level": "high",
              "package_type": "fragile"
            }
        """.trimIndent()
        val details = json.decodeFromString<PackageTemplateDetailsJson>(raw)
        assertEquals("fragile", details.packageType)
        assertTrue(details.isFragile)
    }

    @Test
    fun `PackageTemplateDetailsJson defaults package_type to null when omitted`() {
        val raw = """{"id":88,"description":"Books","weight_kg":3.0}"""
        val details = json.decodeFromString<PackageTemplateDetailsJson>(raw)
        assertNull(details.packageType)
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

    // iOS parity: `/trips/{id}/matches` returns the array under `packages` and each
    // entry's identifier under `match_id` (`TripMatchesResponse` / `TripMatchPackage`
    // in `TripModels.swift`). This decode is what feeds the "X of Y delivered" count
    // on `TripCard`; without it the widget falls through to the Empty state.
    @Test
    fun `TripMatchesResponseJson decodes iOS-shape payload with packages and match_id`() {
        val raw = """
            {
              "trip": { "id": 501, "trip_status": "active", "transportation_method": "car" },
              "packages": [
                {
                  "match_id": 901,
                  "match_status": "delivered",
                  "agreed_price": 80.5,
                  "package": { "id": 77, "description": "Books", "weight_kg": 10 },
                  "shipper": { "id": 12, "name": "Jane Shipper" },
                  "delivered_at": "2026-05-02T10:00:00Z"
                },
                {
                  "match_id": 902,
                  "match_status": "in_transit",
                  "agreed_price": 25,
                  "package": { "id": 78, "description": "Laptop", "weight_kg": 3 },
                  "shipper": { "id": 13, "name": "John Shipper" }
                }
              ]
            }
        """.trimIndent()
        val response = json.decodeFromString<TripMatchesResponseJson>(raw)

        assertEquals(2, response.matches.size)
        assertEquals(901, response.matches[0].id)
        assertEquals(MatchStatus.DELIVERED, response.matches[0].matchStatus)
        assertEquals(902, response.matches[1].id)
        assertEquals(MatchStatus.IN_TRANSIT, response.matches[1].matchStatus)
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
