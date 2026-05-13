package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierLocationJsonDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `decodes full snapshot with current_location and delivery_address`() {
        val raw = """
            {
              "success": true,
              "data": {
                "match_id": 100,
                "carrier": {"id": 42, "name": "Carrie"},
                "current_location": {
                  "latitude": 14.5995,
                  "longitude": 120.9842,
                  "last_updated_at": "2026-05-13T10:15:00Z",
                  "is_stale": false
                },
                "delivery_address": {
                  "address": "123 Main St",
                  "city": "Manila",
                  "latitude": "14.6760",
                  "longitude": "121.0437"
                },
                "match_status": "in_transit"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<CarrierLocationDataResponseJson>(raw)

        assertTrue(response.success)
        val data = response.data
        assertNotNull(data)
        requireNotNull(data)
        assertEquals(100, data.matchId)
        assertEquals(42, data.carrier?.id)
        assertEquals("Carrie", data.carrier?.name)

        val current = data.currentLocation
        assertNotNull(current)
        requireNotNull(current)
        assertEquals(14.5995, current.latitude!!, 0.0001)
        assertEquals(120.9842, current.longitude!!, 0.0001)
        assertEquals("2026-05-13T10:15:00Z", current.lastUpdatedAt)
        assertEquals(false, current.isStale)

        val delivery = data.deliveryAddress
        assertNotNull(delivery)
        requireNotNull(delivery)
        assertEquals("123 Main St", delivery.address)
        assertEquals("Manila", delivery.city)
        assertEquals("14.6760", delivery.latitude)
        assertEquals("121.0437", delivery.longitude)

        assertEquals(MatchStatus.IN_TRANSIT, data.matchStatus)
    }

    @Test
    fun `decodes snapshot with no current_location yet`() {
        val raw = """
            {
              "success": true,
              "data": {
                "match_id": 200,
                "delivery_address": {
                  "address": "456 Side Rd",
                  "city": "Quezon City",
                  "latitude": "14.6760",
                  "longitude": "121.0437"
                },
                "match_status": "confirmed"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<CarrierLocationDataResponseJson>(raw)
        val data = response.data
        requireNotNull(data)

        assertNull(data.currentLocation)
        assertEquals(MatchStatus.CONFIRMED, data.matchStatus)
        assertEquals("456 Side Rd", data.deliveryAddress?.address)
    }

    @Test
    fun `decodes snapshot with stale flag true`() {
        val raw = """
            {
              "success": true,
              "data": {
                "match_id": 300,
                "current_location": {
                  "latitude": 14.5995,
                  "longitude": 120.9842,
                  "last_updated_at": "2026-05-13T00:00:00Z",
                  "is_stale": true
                }
              }
            }
        """.trimIndent()

        val data = json.decodeFromString<CarrierLocationDataResponseJson>(raw).data
        requireNotNull(data)
        assertEquals(true, data.currentLocation?.isStale)
    }
}
