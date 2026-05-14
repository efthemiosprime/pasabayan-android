package com.efthemiosprime.pasabayan.features.bookings.ui

import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mirrors iOS `Trip.estimatedPrice(forWeight:)` — flat price for land-transport
 * trips (or trips with `pricing_type=flat`), per-kg × weight otherwise.
 */
class EstimateEarningsTest {

    @Test
    fun `per_kg trip computes weight times pricePerKg`() {
        val trip = trip(pricingType = "per_kg", pricePerKg = 7.5)
        assertEquals(37.5, estimateEarnings(trip, weightKg = 5.0)!!, 0.0001)
    }

    @Test
    fun `flat trip returns flat price regardless of weight`() {
        val trip = trip(pricingType = "flat", flatTripPrice = 120.0)
        assertEquals(120.0, estimateEarnings(trip, weightKg = 5.0)!!, 0.0001)
        assertEquals(120.0, estimateEarnings(trip, weightKg = null)!!, 0.0001)
    }

    @Test
    fun `null pricingType defaults to per_kg behaviour`() {
        val trip = trip(pricingType = null, pricePerKg = 6.0)
        assertEquals(30.0, estimateEarnings(trip, weightKg = 5.0)!!, 0.0001)
    }

    @Test
    fun `null pricingType falls back to flat when per-kg is missing`() {
        val trip = trip(pricingType = null, pricePerKg = null, flatTripPrice = 80.0)
        assertEquals(80.0, estimateEarnings(trip, weightKg = 5.0)!!, 0.0001)
    }

    @Test
    fun `returns null when no inputs are meaningful`() {
        assertNull(estimateEarnings(trip(), weightKg = 5.0))
        assertNull(estimateEarnings(trip(pricingType = "per_kg"), weightKg = null))
        assertNull(estimateEarnings(trip(pricingType = "per_kg", pricePerKg = 0.0), weightKg = 5.0))
        assertNull(estimateEarnings(trip(pricingType = "flat", flatTripPrice = 0.0), weightKg = null))
    }

    private fun trip(
        pricingType: String? = null,
        pricePerKg: Double? = null,
        flatTripPrice: Double? = null,
    ) = CarrierTripInfo(
        id = 1,
        originCity = "A",
        destinationCity = "B",
        pricingType = pricingType,
        pricePerKg = pricePerKg,
        flatTripPrice = flatTripPrice,
    )
}
