package com.efthemiosprime.pasabayan.core.network.system

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityLogJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `ActivityLogJson encodes camelCase keys`() {
        val payload = ActivityLogJson(
            userId = 42,
            userName = "Jane Doe",
            userEmail = "jane@example.com",
            userType = "shipper",
            action = "create_package",
            description = "Created package request #123",
            logType = "package",
            subjectType = "PackageRequest",
            subjectId = 123,
            properties = mapOf("key" to "value"),
            ipAddress = "Android_a1b2c3d4",
            userAgent = "Pasabayan Android 1.0.0",
        )
        val raw = json.encodeToString(ActivityLogJson.serializer(), payload)
        assertTrue(raw.contains("\"userId\":42"))
        assertTrue(raw.contains("\"userType\":\"shipper\""))
        assertTrue(raw.contains("\"logType\":\"package\""))
        assertTrue(raw.contains("\"subjectType\":\"PackageRequest\""))
        assertTrue(raw.contains("\"subjectId\":123"))
        assertTrue(raw.contains("\"ipAddress\":\"Android_a1b2c3d4\""))
        assertTrue(raw.contains("\"userAgent\":\"Pasabayan Android 1.0.0\""))
    }

    @Test
    fun `ActivityLogJson omits null optional fields when configured to`() {
        val terse = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = false
        }
        val payload = ActivityLogJson(
            userId = 1, userName = "n", userEmail = "e@e.com", userType = "shipper",
            action = "a", description = "d", logType = "system",
            ipAddress = "Android_abc12345", userAgent = "Pasabayan Android 1.0",
        )
        val raw = terse.encodeToString(ActivityLogJson.serializer(), payload)
        assertEquals(false, raw.contains("subjectType"))
        assertEquals(false, raw.contains("subjectId"))
        assertEquals(false, raw.contains("properties"))
    }

    @Test
    fun `HealthCheckResponseJson decodes minimal status payload`() {
        val raw = """{"success":true,"status":"ok"}"""
        val response = json.decodeFromString<HealthCheckResponseJson>(raw)
        assertTrue(response.success)
        assertEquals("ok", response.status)
        assertNull(response.message)
    }

    @Test
    fun `ActivityLogResponseJson decodes minimal success body`() {
        val response = json.decodeFromString<ActivityLogResponseJson>("""{"success":true,"message":"logged"}""")
        assertTrue(response.success)
        assertEquals("logged", response.message)
    }
}
