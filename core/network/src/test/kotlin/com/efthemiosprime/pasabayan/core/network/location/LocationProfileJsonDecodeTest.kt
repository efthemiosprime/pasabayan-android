package com.efthemiosprime.pasabayan.core.network.location

import com.efthemiosprime.pasabayan.core.network.profile.ProfileResponseJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileHomeCityRequestJson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationProfileJsonDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `GET cities CA decodes CitiesResponseJson with numeric and string lat lng`() {
        val raw = readFixture("/api-fixtures/location/cities_ca_success.json")
        val decoded = json.decodeFromString<CitiesResponseJson>(raw)
        assertTrue(decoded.success)
        assertEquals(2, decoded.data.size)
        val toronto = decoded.data[0]
        assertEquals(101, toronto.id)
        assertEquals("Toronto, ON", toronto.display)
        assertEquals("43.6532", toronto.latString())
        assertEquals("-79.3832", toronto.lngString())
        val mtl = decoded.data[1]
        assertEquals("45.5017", mtl.latString())
        assertEquals("-73.5673", mtl.lngString())
    }

    @Test
    fun `PUT profile response decodes ProfileResponseJson`() {
        val raw = readFixture("/api-fixtures/profile/profile_put_success.json")
        val decoded = json.decodeFromString<ProfileResponseJson>(raw)
        assertTrue(decoded.success)
        assertEquals(101, decoded.data.homeCityId)
        assertEquals(false, decoded.data.isComplete)
    }

    @Test
    fun `UpdateProfileHomeCityRequestJson encodes home_city_id`() {
        val body = UpdateProfileHomeCityRequestJson(homeCityId = 55)
        val encoded = json.encodeToString(UpdateProfileHomeCityRequestJson.serializer(), body)
        assertTrue(encoded.contains("\"home_city_id\":55"))
    }

    private fun readFixture(resourcePath: String): String =
        javaClass.getResourceAsStream(resourcePath)!!.bufferedReader().use { it.readText() }
}
