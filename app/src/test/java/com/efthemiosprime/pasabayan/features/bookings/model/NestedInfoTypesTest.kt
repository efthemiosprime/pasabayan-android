package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CurrentLocationData
import com.efthemiosprime.pasabayan.features.bookings.model.nested.DeliveryAddressData
import com.efthemiosprime.pasabayan.features.bookings.model.nested.PackageRequestInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.TripCapacity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NestedInfoTypesTest {

    @Test
    fun `CarrierTripInfo holds trip projection fields`() {
        val info = CarrierTripInfo(
            id = 1,
            originCity = "Toronto",
            destinationCity = "Vancouver",
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-01T14:00:00Z",
            transportationMethod = "flight",
        )
        assertEquals(1, info.id)
        assertEquals("Toronto", info.originCity)
        assertEquals("Vancouver", info.destinationCity)
        assertEquals("Toronto → Vancouver", info.route)
    }

    @Test
    fun `PackageRequestInfo holds package projection fields`() {
        val info = PackageRequestInfo(
            id = 10,
            description = "Laptop",
            weightKg = 5.0,
            pickupCity = "Montreal",
            deliveryCity = "Ottawa",
        )
        assertEquals(10, info.id)
        assertEquals("Laptop", info.description)
        assertEquals(5.0, info.weightKg!!, 0.001)
    }

    @Test
    fun `CurrentLocationData reports stale when old`() {
        val fresh = CurrentLocationData(
            latitude = 43.65,
            longitude = -79.38,
            lastUpdatedAt = "2026-03-29T10:00:00Z",
            isStale = false,
        )
        assertFalse(fresh.isStale)

        val stale = CurrentLocationData(
            latitude = 43.65,
            longitude = -79.38,
            lastUpdatedAt = "2026-03-28T10:00:00Z",
            isStale = true,
        )
        assertTrue(stale.isStale)
    }

    @Test
    fun `DeliveryAddressData converts string coordinates to double`() {
        val addr = DeliveryAddressData(
            address = "456 Oak Ave",
            city = "Montreal",
            latitude = "45.5017",
            longitude = "-73.5673",
        )
        assertEquals(45.5017, addr.latitudeDouble!!, 0.001)
        assertEquals(-73.5673, addr.longitudeDouble!!, 0.001)
    }

    @Test
    fun `DeliveryAddressData returns null for invalid coordinates`() {
        val addr = DeliveryAddressData(
            address = null,
            city = null,
            latitude = "invalid",
            longitude = "",
        )
        assertNull(addr.latitudeDouble)
        assertNull(addr.longitudeDouble)
    }

    @Test
    fun `TripCapacity holds capacity fields`() {
        val capacity = TripCapacity(
            availableWeightKg = 25.0,
            availableSpaceLiters = 50.0,
            totalWeightKg = 100.0,
        )
        assertEquals(25.0, capacity.availableWeightKg, 0.001)
        assertEquals(50.0, capacity.availableSpaceLiters!!, 0.001)
    }
}
