package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `GET profile with user decodes`() {
        val raw = readFixture("/api-fixtures/profile/profile_get_user.json")
        val decoded = json.decodeFromString<ProfileResponseJson>(raw)
        assertTrue(decoded.success)
        val p = requireNotNull(decoded.data.profile)
        assertEquals("Test User", p.fullName)
        assertEquals("verified", p.verificationLevel)
        assertEquals(101, decoded.data.homeCityId)
    }

    @Test
    fun `carrier profile decodes string doubles and routes`() {
        val raw = readFixture("/api-fixtures/profile/carrier_profile_get.json")
        val decoded = json.decodeFromString<CarrierProfileResponseJson>(raw)
        val c = requireNotNull(decoded.data)
        assertEquals(50.5, c.maxWeightCapacityKg!!, 0.01)
        assertEquals(1, c.availableRoutes?.size)
        assertEquals("Montreal", c.availableRoutes!![0].origin)
    }

    @Test
    fun `carrier stats decodes nested stats and string success_rate`() {
        val raw = readFixture("/api-fixtures/profile/carrier_stats.json")
        val decoded = json.decodeFromString<CarrierStatsResponseJson>(raw)
        assertTrue(decoded.success)
        val d = requireNotNull(decoded.data)
        assertNotNull(d.deliveries)
        assertEquals(0.95, d.deliveries!!.successRate!!, 0.01)
    }

    @Test
    fun `user stats decodes`() {
        val raw = readFixture("/api-fixtures/profile/user_stats.json")
        val decoded = json.decodeFromString<UserStatsResponseJson>(raw)
        assertTrue(decoded.success)
        assertEquals(4, decoded.data?.packagesCount)
        assertEquals(4.2, decoded.data?.averageRating!!, 0.01)
    }

    @Test
    fun `carrier status toggle decodes dict user_types`() {
        val raw = readFixture("/api-fixtures/profile/carrier_status_toggle.json")
        val decoded = json.decodeFromString<CarrierStatusResponseJson>(raw)
        val types = decoded.data?.userTypes
        assertNotNull(types)
        assertTrue(types!!.contains("shipper"))
        assertTrue(types.contains("carrier"))
    }

    private fun readFixture(resourcePath: String): String =
        javaClass.getResourceAsStream(resourcePath)!!.bufferedReader().use { it.readText() }
}
