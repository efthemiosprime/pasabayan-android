package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentPreferencesJsonDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `PUT consent-preferences response decodes`() {
        val raw = readFixture("/api-fixtures/profile/consent_put_success.json")
        val decoded = json.decodeFromString<ConsentPreferencesResponseJson>(raw)
        assertTrue(decoded.success)
        val d = requireNotNull(decoded.data)
        assertTrue(d.pushNotifications)
        assertFalse(d.locationTracking)
        assertFalse(d.analytics)
        assertTrue(d.marketingCommunications)
    }

    @Test
    fun `ConsentPreferencesUpdateJson encodes snake_case keys`() {
        val body = ConsentPreferencesUpdateJson(
            pushNotifications = true,
            locationTracking = false,
            analytics = true,
            marketingCommunications = false,
        )
        val encoded = json.encodeToString(ConsentPreferencesUpdateJson.serializer(), body)
        assertTrue(encoded.contains("\"push_notifications\":true"))
        assertTrue(encoded.contains("\"location_tracking\":false"))
        assertTrue(encoded.contains("\"analytics\":true"))
        assertTrue(encoded.contains("\"marketing_communications\":false"))
    }

    private fun readFixture(resourcePath: String): String =
        javaClass.getResourceAsStream(resourcePath)!!.bufferedReader().use { it.readText() }
}
