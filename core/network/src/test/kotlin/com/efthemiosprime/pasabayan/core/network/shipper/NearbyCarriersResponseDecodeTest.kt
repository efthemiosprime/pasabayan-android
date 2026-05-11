package com.efthemiosprime.pasabayan.core.network.shipper

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyCarriersResponseDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `decodes object payload with carriers`() {
        val raw = """
        {
          "success": true,
          "message": "ok",
          "data": {
            "home_city_id": 87,
            "home_city_name": "Toronto",
            "radius_km": 25.0,
            "carriers": [
              {"id": 1, "name": "Alex", "avatar": null, "completed_deliveries": 12, "distance_km": 4.2},
              {"id": 2, "name": "Sam", "avatar": "https://x/a.png", "completed_deliveries": 0, "distance_km": null}
            ]
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<NearbyCarriersResponseJson>(raw)
        assertTrue(res.success)
        val data = res.data!!
        assertEquals(87, data.homeCityId)
        assertEquals("Toronto", data.homeCityName)
        assertEquals(2, data.carriers.size)
        assertEquals(12, data.carriers[0].completedDeliveries)
        assertNull(data.carriers[1].distanceKm)
    }

    @Test
    fun `decodes array payload when home city unset`() {
        val raw = """{"success": true, "message": "no home city", "data": []}"""
        val res = json.decodeFromString<NearbyCarriersResponseJson>(raw)
        assertTrue(res.success)
        val data = res.data!!
        assertNull(data.homeCityId)
        assertTrue(data.carriers.isEmpty())
    }

    @Test
    fun `decodes null data`() {
        val raw = """{"success": false, "message": "error", "data": null}"""
        val res = json.decodeFromString<NearbyCarriersResponseJson>(raw)
        assertFalse(res.success)
        assertNull(res.data)
    }

    @Test
    fun `decodes missing data field`() {
        val raw = """{"success": true, "message": "no carriers nearby"}"""
        val res = json.decodeFromString<NearbyCarriersResponseJson>(raw)
        assertTrue(res.success)
        assertEquals("no carriers nearby", res.message)
        assertNull(res.data)
    }
}
