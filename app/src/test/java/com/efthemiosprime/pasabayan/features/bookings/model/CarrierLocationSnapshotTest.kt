package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.bookings.CarrierLocationDataJson
import com.efthemiosprime.pasabayan.core.network.bookings.CurrentLocationJson
import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryAddressJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierLocationSnapshotTest {

    @Test
    fun `toDomain copies carrier and parses String delivery coords`() {
        val dto = CarrierLocationDataJson(
            matchId = 100,
            carrier = UserSummary(id = 42, name = "Carrie"),
            currentLocation = CurrentLocationJson(
                latitude = 14.5995,
                longitude = 120.9842,
                lastUpdatedAt = "2026-05-13T10:15:00Z",
                isStale = false,
            ),
            deliveryAddress = DeliveryAddressJson(
                address = "123 Main St",
                city = "Manila",
                latitude = "14.6760",
                longitude = "121.0437",
            ),
            matchStatus = MatchStatus.IN_TRANSIT,
        )

        val snapshot = dto.toDomain()

        assertEquals(100, snapshot.matchId)
        assertEquals(42, snapshot.carrier?.id)
        assertEquals(14.5995, snapshot.carrierLat!!, 0.0001)
        assertEquals(120.9842, snapshot.carrierLng!!, 0.0001)
        assertEquals("2026-05-13T10:15:00Z", snapshot.lastUpdatedAt)
        assertEquals(false, snapshot.isStale)
        assertEquals("123 Main St", snapshot.deliveryAddress)
        assertEquals("Manila", snapshot.deliveryCity)
        assertEquals(14.6760, snapshot.deliveryLat!!, 0.0001)
        assertEquals(121.0437, snapshot.deliveryLng!!, 0.0001)
        assertEquals(MatchStatus.IN_TRANSIT, snapshot.matchStatus)
        assertTrue(snapshot.hasCarrierLocation)
        assertTrue(snapshot.hasDeliveryLocation)
    }

    @Test
    fun `toDomain treats unparseable String coords as null`() {
        val dto = CarrierLocationDataJson(
            matchId = 1,
            deliveryAddress = DeliveryAddressJson(
                address = "x",
                city = "y",
                latitude = "not-a-number",
                longitude = "nope",
            ),
        )

        val snapshot = dto.toDomain()
        assertNull(snapshot.deliveryLat)
        assertNull(snapshot.deliveryLng)
        assertFalse(snapshot.hasDeliveryLocation)
    }

    @Test
    fun `hasCarrierLocation false when coords are null`() {
        val dto = CarrierLocationDataJson(matchId = 1, currentLocation = null)
        val snapshot = dto.toDomain()
        assertFalse(snapshot.hasCarrierLocation)
    }

    @Test
    fun `hasCarrierLocation false when coords are both zero`() {
        val dto = CarrierLocationDataJson(
            matchId = 1,
            currentLocation = CurrentLocationJson(latitude = 0.0, longitude = 0.0),
        )
        assertFalse(dto.toDomain().hasCarrierLocation)
    }
}
