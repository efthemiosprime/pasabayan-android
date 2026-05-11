package com.efthemiosprime.pasabayan.core.network.location

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationCatalogJsonDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `LocationCatalogResponseJson decodes nested tree`() {
        val raw = """
        {
          "success": true,
          "version": "2026.05.11",
          "data": [
            {
              "name": "Canada", "iso2": "CA", "iso3": "CAN", "phone_code": "+1", "currency": "CAD",
              "states": [
                {
                  "name": "Ontario", "code": "ON",
                  "cities": [
                    {"id": 1, "name": "Toronto", "lat": 43.65, "lng": -79.38,
                     "aliases": [
                        {"alias": "GTA", "type": "metro"},
                        {"alias": "T-Dot", "type": "nickname"}
                     ]}
                  ]
                }
              ]
            }
          ]
        }
        """.trimIndent()
        val response = json.decodeFromString<LocationCatalogResponseJson>(raw)
        assertTrue(response.success)
        assertEquals("2026.05.11", response.version)
        val country = response.data.single()
        assertEquals("CA", country.iso2)
        val city = country.states.single().cities.single()
        assertEquals("Toronto", city.name)
        assertEquals(43.65, city.lat!!, 0.001)
        assertNotNull(city.aliases)
        assertEquals(2, city.aliases!!.size)
        assertEquals("metro", city.aliases!![0].type)
    }
}
