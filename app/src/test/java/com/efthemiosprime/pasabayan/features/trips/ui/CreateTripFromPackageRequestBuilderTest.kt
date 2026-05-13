package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * iOS parity: every field captured in `CreateTripFromPackageView` must end up on the
 * `CreateTripFromPackageRequest`. The Android form previously dropped pickup / dropoff
 * addresses and special notes on the floor — these tests guard against the regression.
 */
class CreateTripFromPackageRequestBuilderTest {

    private val template = TripTemplateData(
        packageId = 88,
        originCity = "Toronto",
        originCountry = "Canada",
        destinationCity = "Montreal",
        destinationCountry = "Canada",
        suggestedDepartureDate = "2026-07-01T08:00:00Z",
        suggestedArrivalDate = "2026-07-01T16:00:00Z",
        suggestedWeightKg = 5.0,
        suggestedSpaceLiters = 25.0,
        packageDescription = "Electronics bundle",
        packageWeightKg = 3.2,
        packageUrgencyLevel = "urgent",
    )

    @Test
    fun `pickup address dropoff address and notes are forwarded to the request`() {
        val request = buildRequestFromState(
            packageId = 88,
            template = template,
            departureDate = "2026-07-01T08:00:00Z",
            arrivalDate = "2026-07-01T16:00:00Z",
            availableWeightKg = "5",
            availableSpaceLiters = "25",
            transportationMethod = TransportationMethod.CAR,
            price = "",
            pickupAddress = "123 Main St",
            dropoffAddress = "456 Oak Ave",
            specialNotes = "Handle with care",
            proposedPrice = "80",
            requestMessage = "Same day available",
        )

        assertEquals("123 Main St", request.pickupAddress)
        assertEquals("456 Oak Ave", request.dropoffAddress)
        assertEquals("Handle with care", request.specialNotes)
        assertEquals("Same day available", request.requestMessage)
        assertEquals(80.0, request.proposedPrice!!, 0.001)
        assertEquals("car", request.transportationMethod)
        assertEquals(5.0, request.availableWeightKg, 0.001)
        assertEquals(25.0, request.availableSpaceLiters, 0.001)
        assertEquals("Toronto", request.originCity)
        assertEquals("Montreal", request.destinationCity)
        assertEquals("2026-07-01T08:00:00Z", request.departureDate)
        assertEquals("2026-07-01T16:00:00Z", request.arrivalDate)
    }

    @Test
    fun `blank optional strings are normalized to null on the wire`() {
        val request = buildRequestFromState(
            packageId = 88,
            template = template,
            departureDate = "2026-07-01T08:00:00Z",
            arrivalDate = "2026-07-01T16:00:00Z",
            availableWeightKg = "5",
            availableSpaceLiters = "25",
            transportationMethod = TransportationMethod.CAR,
            price = "",
            pickupAddress = "  ",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = " ",
        )

        assertNull(request.pickupAddress)
        assertNull(request.dropoffAddress)
        assertNull(request.specialNotes)
        assertNull(request.requestMessage)
        assertNull(request.proposedPrice)
        assertNull(request.pricePerKg)
        assertNull(request.flatTripPrice)
    }

    // iOS parity (`populateFormFromTemplate` :202-217 + `syncCarrierLegFromSharedSchedule`):
    // shared pickup/delivery default to departure/arrival so the carrier doesn't have to
    // re-enter the schedule when creating a trip from a package.
    @Test
    fun `shared pickup and delivery default to departure and arrival`() {
        val request = buildRequestFromState(
            packageId = 88,
            template = template,
            departureDate = "2026-07-01T08:00:00Z",
            arrivalDate = "2026-07-01T16:00:00Z",
            availableWeightKg = "5",
            availableSpaceLiters = "25",
            transportationMethod = TransportationMethod.CAR,
            price = "",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )
        assertEquals("2026-07-01T08:00:00Z", request.sharedPickupDate)
        assertEquals("2026-07-01T16:00:00Z", request.sharedDeliveryDate)
    }

    @Test
    fun `blank departure or arrival leaves shared schedule null`() {
        val request = buildRequestFromState(
            packageId = 88,
            template = template,
            departureDate = "",
            arrivalDate = "",
            availableWeightKg = "5",
            availableSpaceLiters = "25",
            transportationMethod = TransportationMethod.CAR,
            price = "",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )
        assertNull(request.sharedPickupDate)
        assertNull(request.sharedDeliveryDate)
    }

    @Test
    fun `parseTransportationMethod maps server lowercase strings to enum`() {
        assertEquals(TransportationMethod.CAR, parseTransportationMethod("car"))
        assertEquals(TransportationMethod.FLIGHT, parseTransportationMethod("flight"))
        // Case-insensitive: server may upper-case or mix case.
        assertEquals(TransportationMethod.SHIP, parseTransportationMethod("Ship"))
    }

    @Test
    fun `parseTransportationMethod returns null for blank or unknown values`() {
        assertNull(parseTransportationMethod(""))
        assertNull(parseTransportationMethod("   "))
        assertNull(parseTransportationMethod("teleport"))
    }

    @Test
    fun `transportation method serializes to lowercase wire value`() {
        listOf(
            TransportationMethod.FLIGHT to "flight",
            TransportationMethod.CAR to "car",
            TransportationMethod.SHIP to "ship",
            TransportationMethod.TRAIN to "train",
            TransportationMethod.BUS to "bus",
        ).forEach { (method, expected) ->
            val request = buildRequestFromState(
                packageId = 1,
                template = template,
                departureDate = "",
                arrivalDate = "",
                availableWeightKg = "0",
                availableSpaceLiters = "0",
                transportationMethod = method,
                price = "",
                pickupAddress = "",
                dropoffAddress = "",
                specialNotes = "",
                proposedPrice = "",
                requestMessage = "",
            )
            assertEquals(expected, request.transportationMethod)
        }
    }

    @Test
    fun `null template still produces a usable request`() {
        val request = buildRequestFromState(
            packageId = 7,
            template = null,
            departureDate = "",
            arrivalDate = "",
            availableWeightKg = "1.5",
            availableSpaceLiters = "10",
            transportationMethod = TransportationMethod.CAR,
            price = "",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )

        assertEquals(7, request.packageId)
        assertEquals("", request.originCity)
        assertEquals("", request.destinationCity)
        assertEquals(1.5, request.availableWeightKg, 0.001)
    }

    // iOS parity (`pricingSection`): the single `price` input maps to flatTripPrice for land
    // transport and to pricePerKg for air / sea. Previously the screen forced both to null.
    @Test
    fun `price routes to flatTripPrice for land transport`() {
        val request = buildRequestFromState(
            packageId = 1,
            template = template,
            departureDate = "",
            arrivalDate = "",
            availableWeightKg = "5",
            availableSpaceLiters = "10",
            transportationMethod = TransportationMethod.CAR,
            price = "150",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )

        assertEquals(150.0, request.flatTripPrice!!, 0.001)
        assertNull(request.pricePerKg)
    }

    @Test
    fun `price routes to pricePerKg for air transport`() {
        val request = buildRequestFromState(
            packageId = 1,
            template = template,
            departureDate = "",
            arrivalDate = "",
            availableWeightKg = "5",
            availableSpaceLiters = "10",
            transportationMethod = TransportationMethod.FLIGHT,
            price = "12.50",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )

        assertEquals(12.50, request.pricePerKg!!, 0.001)
        assertNull(request.flatTripPrice)
    }

    @Test
    fun `unparseable price leaves both pricing fields null`() {
        val request = buildRequestFromState(
            packageId = 1,
            template = template,
            departureDate = "",
            arrivalDate = "",
            availableWeightKg = "5",
            availableSpaceLiters = "10",
            transportationMethod = TransportationMethod.SHIP,
            price = "abc",
            pickupAddress = "",
            dropoffAddress = "",
            specialNotes = "",
            proposedPrice = "",
            requestMessage = "",
        )

        assertNull(request.pricePerKg)
        assertNull(request.flatTripPrice)
    }
}
