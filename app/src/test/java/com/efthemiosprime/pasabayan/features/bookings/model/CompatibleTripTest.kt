package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for `CompatibleTrip` computed properties — mirrors iOS
 * `CompatibleTrip` derived getters (PackageRequest.swift line 1102).
 */
class CompatibleTripTest {

    private fun trip(
        pricingType: String? = null,
        transportationMethod: String? = null,
        availableWeightKg: String? = null,
        pricePerKg: String? = null,
        flatTripPrice: String? = null,
        calculatedPrice: String? = null,
        canRequest: Boolean? = null,
        shipperRequestStatus: String? = null,
    ) = CompatibleTrip(
        id = 1, carrierId = 1,
        originCity = "A", originCountry = "CA",
        destinationCity = "B", destinationCountry = "CA",
        departureDate = null, arrivalDate = null,
        pickupDate = null, deliveryDate = null,
        availableWeightKg = availableWeightKg,
        availableSpaceLiters = null,
        pricePerKg = pricePerKg,
        flatTripPrice = flatTripPrice,
        calculatedPrice = calculatedPrice,
        pricingType = pricingType,
        pricingMethod = null,
        tripStatus = "active",
        transportationMethod = transportationMethod,
        specialNotes = null,
        createdAt = null, updatedAt = null,
        carrier = null,
        shipperRequestStatus = shipperRequestStatus,
        canRequest = canRequest,
        requestMessage = null,
        requestedAt = null,
        distanceKm = null,
    )

    @Test
    fun `usesFlatPricing prefers explicit pricing_type`() {
        assertTrue(trip(pricingType = "flat", transportationMethod = "flight").usesFlatPricing)
        assertFalse(trip(pricingType = "per_kg", transportationMethod = "car").usesFlatPricing)
    }

    @Test
    fun `usesFlatPricing falls back to transportation method for land transport`() {
        assertTrue(trip(transportationMethod = "car").usesFlatPricing)
        assertTrue(trip(transportationMethod = "truck").usesFlatPricing)
        assertFalse(trip(transportationMethod = "flight").usesFlatPricing)
        assertFalse(trip(transportationMethod = "ship").usesFlatPricing)
    }

    @Test
    fun `effectiveFlatPrice prefers calculated_price when positive`() {
        val t = trip(calculatedPrice = "120.50", flatTripPrice = "100.00")
        assertEquals(120.50, t.effectiveFlatPrice!!, 0.0001)
    }

    @Test
    fun `effectiveFlatPrice falls back to flat_trip_price when calculated is zero`() {
        val t = trip(calculatedPrice = "0", flatTripPrice = "100.00")
        assertEquals(100.0, t.effectiveFlatPrice!!, 0.0001)
    }

    @Test
    fun `effectiveFlatPrice returns null when both missing`() {
        assertNull(trip().effectiveFlatPrice)
    }

    @Test
    fun `canRequestTrip defaults to true when missing`() {
        assertTrue(trip().canRequestTrip)
        assertTrue(trip(canRequest = true).canRequestTrip)
        assertFalse(trip(canRequest = false).canRequestTrip)
    }

    @Test
    fun `hasActiveRequest matches shipper_requested status only`() {
        assertTrue(trip(shipperRequestStatus = "shipper_requested").hasActiveRequest)
        assertFalse(trip(shipperRequestStatus = "declined").hasActiveRequest)
        assertFalse(trip().hasActiveRequest)
    }

    @Test
    fun `canCarryPackageWeight returns true when weight fits or info missing`() {
        val t = trip(availableWeightKg = "20.0")
        assertTrue(t.canCarryPackageWeight(15.0))
        assertTrue(t.canCarryPackageWeight(20.0))
        assertFalse(t.canCarryPackageWeight(25.0))
        // Missing package weight or trip capacity → permissive (server arbitrates)
        assertTrue(t.canCarryPackageWeight(null))
        assertTrue(trip().canCarryPackageWeight(15.0))
    }

    @Test
    fun `transportationMethodEnum maps strings case-insensitively`() {
        assertEquals(TransportationMethod.FLIGHT, trip(transportationMethod = "flight").transportationMethodEnum)
        assertEquals(TransportationMethod.CAR, trip(transportationMethod = "CAR").transportationMethodEnum)
        assertEquals(TransportationMethod.NONE, trip(transportationMethod = "unknown").transportationMethodEnum)
        assertEquals(TransportationMethod.NONE, trip().transportationMethodEnum)
    }
}
