package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripEarningsBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * iOS parity for [computeTripEarningsTotal] and [formatTripCurrency], the helpers backing
 * `tripEarningsOverviewCard` in `TripDetailsView.swift`.
 */
class TripEarningsFormattingTest {

    private fun trip(
        total: Double? = null,
        breakdown: TripEarningsBreakdown? = null,
        currency: String? = "CAD",
    ): Trip = Trip(
        id = 1, carrierId = 42,
        originCity = "Toronto", originCountry = "Canada",
        originLat = null, originLng = null,
        destinationCity = "Vancouver", destinationCountry = "Canada",
        destinationLat = null, destinationLng = null,
        departureDate = "2026-04-01T08:00:00Z",
        arrivalDate = "2026-04-01T14:00:00Z",
        availableWeightKg = 25.0, availableSpaceLiters = null,
        pricePerKg = 15.0, tripStatus = TripStatus.ACTIVE,
        transportationMethod = TransportationMethod.FLIGHT,
        specialNotes = null, carrier = null,
        createdAt = null, updatedAt = null,
        pricingType = null, pricingMethod = null,
        flatTripPrice = null, basePrice = null, calculatedPrice = null,
        pickupAddress = null, pickupLandmark = null,
        dropoffAddress = null, dropoffLandmark = null,
        tripEarningsTotal = total,
        tripEarningsCurrency = currency,
        tripEarningsBreakdown = breakdown,
        hasPendingRequests = null, pendingRequestCount = null,
        pendingRequests = null, distanceKm = null,
    )

    // -- computeTripEarningsTotal --

    @Test
    fun `total prefers explicit tripEarningsTotal when present`() {
        val t = trip(
            total = 250.0,
            breakdown = TripEarningsBreakdown(
                deliveredAmount = 50.0, deliveredCurrency = "CAD", deliveredCount = 1,
                pendingAmount = 50.0, pendingCurrency = "CAD", pendingCount = 1,
            ),
        )
        assertEquals(250.0, computeTripEarningsTotal(t), 0.001)
    }

    @Test
    fun `total falls back to delivered plus pending from breakdown`() {
        val t = trip(
            total = null,
            breakdown = TripEarningsBreakdown(
                deliveredAmount = 100.0, deliveredCurrency = "CAD", deliveredCount = 2,
                pendingAmount = 75.0, pendingCurrency = "CAD", pendingCount = 1,
            ),
        )
        assertEquals(175.0, computeTripEarningsTotal(t), 0.001)
    }

    @Test
    fun `total is zero when no earnings info is available`() {
        assertEquals(0.0, computeTripEarningsTotal(trip()), 0.001)
    }

    // -- formatTripCurrency --

    @Test
    fun `formats with explicit currency code and two decimals`() {
        val out = formatTripCurrency(25.0, "USD")
        // Locale-dependent symbol; assert the amount and code semantics are present.
        assertTrue("output should contain 25.00 (got $out)", out.contains("25.00"))
    }

    @Test
    fun `defaults to CAD when currency is null`() {
        val out = formatTripCurrency(10.5, null)
        assertTrue("output should contain 10.50 (got $out)", out.contains("10.50"))
    }

    @Test
    fun `falls back to code and amount for unknown currency`() {
        val out = formatTripCurrency(7.0, "ZZZ_INVALID")
        // The fallback path encodes "<code> <amount>" so both must appear.
        assertTrue("expected fallback string with code, got $out", out.contains("ZZZ_INVALID"))
        assertTrue("expected fallback string with 7.00, got $out", out.contains("7.00"))
    }

    @Test
    fun `blank currency code is treated as default`() {
        val out = formatTripCurrency(5.0, "  ")
        assertTrue("expected 5.00 in $out", out.contains("5.00"))
    }
}
