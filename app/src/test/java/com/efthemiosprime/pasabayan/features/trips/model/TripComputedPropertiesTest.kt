package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PricingType
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripComputedPropertiesTest {

    private fun baseTrip(
        tripStatus: TripStatus = TripStatus.ACTIVE,
        transportationMethod: TransportationMethod = TransportationMethod.FLIGHT,
        availableWeightKg: Double? = 25.0,
        pricePerKg: Double? = 15.0,
        pricingType: String? = null,
        flatTripPrice: Double? = null,
        calculatedPrice: Double? = null,
        originLat: Double? = null,
        originLng: Double? = null,
        destinationLat: Double? = null,
        destinationLng: Double? = null,
        distanceKm: Double? = null,
    ) = Trip(
        id = 1, carrierId = 42,
        originCity = "Toronto", originCountry = "Canada",
        originLat = originLat, originLng = originLng,
        destinationCity = "Vancouver", destinationCountry = "Canada",
        destinationLat = destinationLat, destinationLng = destinationLng,
        departureDate = "2026-04-01T08:00:00Z",
        arrivalDate = "2026-04-01T14:00:00Z",
        availableWeightKg = availableWeightKg,
        availableSpaceLiters = 50.0,
        pricePerKg = pricePerKg,
        tripStatus = tripStatus,
        transportationMethod = transportationMethod,
        specialNotes = null, carrier = null,
        createdAt = null, updatedAt = null,
        pricingType = pricingType, pricingMethod = null,
        flatTripPrice = flatTripPrice, basePrice = null,
        calculatedPrice = calculatedPrice,
        pickupAddress = null, pickupLandmark = null,
        dropoffAddress = null, dropoffLandmark = null,
        tripEarningsTotal = null, tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null, pendingRequestCount = null,
        pendingRequests = null, distanceKm = distanceKm,
    )

    @Test
    fun `route combines origin and destination`() {
        assertEquals("Toronto → Vancouver", baseTrip().route)
    }

    @Test
    fun `hasCapacity true when weight available`() {
        assertTrue(baseTrip(availableWeightKg = 10.0).hasCapacity)
    }

    @Test
    fun `hasCapacity false when weight zero`() {
        assertFalse(baseTrip(availableWeightKg = 0.0).hasCapacity)
    }

    @Test
    fun `hasCapacity false when weight null`() {
        assertFalse(baseTrip(availableWeightKg = null).hasCapacity)
    }

    @Test
    fun `isBookable true for active trip with capacity`() {
        assertTrue(baseTrip(tripStatus = TripStatus.ACTIVE, availableWeightKg = 10.0).isBookable)
    }

    @Test
    fun `isBookable true for planning trip with capacity`() {
        assertTrue(baseTrip(tripStatus = TripStatus.PLANNING, availableWeightKg = 10.0).isBookable)
    }

    @Test
    fun `isBookable false for completed trip`() {
        assertFalse(baseTrip(tripStatus = TripStatus.COMPLETED).isBookable)
    }

    @Test
    fun `isBookable false for cancelled trip`() {
        assertFalse(baseTrip(tripStatus = TripStatus.CANCELLED).isBookable)
    }

    @Test
    fun `isBookable false for active trip without capacity`() {
        assertFalse(baseTrip(tripStatus = TripStatus.ACTIVE, availableWeightKg = 0.0).isBookable)
    }

    @Test
    fun `effectivePricingType returns flat for explicit flat`() {
        assertEquals(PricingType.FLAT, baseTrip(pricingType = "flat").effectivePricingType)
    }

    @Test
    fun `effectivePricingType returns perKg for explicit per_kg`() {
        assertEquals(PricingType.PER_KG, baseTrip(pricingType = "per_kg").effectivePricingType)
    }

    @Test
    fun `effectivePricingType defaults to transport method default`() {
        assertEquals(
            PricingType.PER_KG,
            baseTrip(transportationMethod = TransportationMethod.FLIGHT).effectivePricingType,
        )
        assertEquals(
            PricingType.FLAT,
            baseTrip(transportationMethod = TransportationMethod.CAR).effectivePricingType,
        )
    }

    @Test
    fun `effectivePrice returns pricePerKg for perKg pricing`() {
        assertEquals(15.0, baseTrip(pricePerKg = 15.0).effectivePrice, 0.001)
    }

    @Test
    fun `effectivePrice returns calculatedPrice over flatTripPrice for flat pricing`() {
        val trip = baseTrip(
            pricingType = "flat",
            calculatedPrice = 300.0,
            flatTripPrice = 200.0,
        )
        assertEquals(300.0, trip.effectivePrice, 0.001)
    }

    @Test
    fun `effectivePrice falls back to flatTripPrice when no calculatedPrice`() {
        val trip = baseTrip(pricingType = "flat", flatTripPrice = 200.0)
        assertEquals(200.0, trip.effectivePrice, 0.001)
    }

    @Test
    fun `estimatedPrice multiplies for perKg`() {
        val trip = baseTrip(pricePerKg = 10.0)
        assertEquals(50.0, trip.estimatedPrice(5.0), 0.001)
    }

    @Test
    fun `estimatedPrice returns flat price regardless of weight`() {
        val trip = baseTrip(pricingType = "flat", flatTripPrice = 200.0)
        assertEquals(200.0, trip.estimatedPrice(5.0), 0.001)
        assertEquals(200.0, trip.estimatedPrice(50.0), 0.001)
    }

    @Test
    fun `formattedCapacity formats weight`() {
        assertEquals("25.0 kg", baseTrip(availableWeightKg = 25.0).formattedCapacity)
    }

    @Test
    fun `routeDistanceKm uses distanceKm when available`() {
        assertEquals(3365.0, baseTrip(distanceKm = 3365.0).routeDistanceKm, 0.1)
    }

    @Test
    fun `routeDistanceKm computes haversine when no distanceKm`() {
        val trip = baseTrip(
            originLat = 43.6532, originLng = -79.3832,
            destinationLat = 49.2827, destinationLng = -123.1207,
        )
        assertTrue(trip.routeDistanceKm > 3000)
        assertTrue(trip.routeDistanceKm < 4000)
    }

    @Test
    fun `routeDistanceKm returns zero when no coordinates`() {
        assertEquals(0.0, baseTrip().routeDistanceKm, 0.001)
    }

    @Test
    fun `formattedDepartureDate formats valid date`() {
        val trip = baseTrip()
        assertTrue(trip.formattedDepartureDate.isNotEmpty())
    }

    @Test
    fun `formattedDepartureDate empty for null date`() {
        val trip = baseTrip().copy(departureDate = null)
        assertEquals("", trip.formattedDepartureDate)
    }

    @Test
    fun `priceDisplayString mirrors formatted price`() {
        val trip = baseTrip(pricePerKg = 22.0)
        assertEquals(trip.formattedPrice, trip.priceDisplayString)
    }

    @Test
    fun `formattedDuration renders hours and minutes`() {
        val trip = baseTrip().copy(
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-01T14:30:00Z",
        )
        assertEquals("6h 30m", trip.formattedDuration)
    }
}
