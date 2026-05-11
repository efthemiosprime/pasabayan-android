package com.efthemiosprime.pasabayan.features.notifications.services

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceTokenRegisterResponseParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `200 with success true returns Registered`() {
        val body = """
        {"success":true,"message":"ok","data":{"device_token":{"id":7,"platform":"android","is_active":true,"created_at":"x"}}}
        """.trimIndent()
        val result = DeviceTokenRegisterResponseParser.parse(200, body, json)
        assertTrue(result is DeviceTokenRegisterResult.Registered)
        val info = (result as DeviceTokenRegisterResult.Registered).info
        assertEquals(7, info.id)
        assertEquals("android", info.platform)
    }

    @Test
    fun `201 with success true returns Registered`() {
        val body = """
        {"success":true,"data":{"device_token":{"id":1,"platform":"android","is_active":true,"created_at":"x"}}}
        """.trimIndent()
        val result = DeviceTokenRegisterResponseParser.parse(201, body, json)
        assertTrue(result is DeviceTokenRegisterResult.Registered)
    }

    @Test
    fun `200 with success false returns ServerError`() {
        val body = """{"success":false,"message":"backend rejection"}"""
        val result = DeviceTokenRegisterResponseParser.parse(200, body, json)
        assertTrue(result is DeviceTokenRegisterResult.ServerError)
        assertEquals("backend rejection", (result as DeviceTokenRegisterResult.ServerError).message)
    }

    @Test
    fun `403 with consent_required returns ConsentRequired`() {
        val body = """
        {"success":false,"message":"Consent required for push_notifications","consent_required":"push_notifications"}
        """.trimIndent()
        val result = DeviceTokenRegisterResponseParser.parse(403, body, json)
        assertTrue(result is DeviceTokenRegisterResult.ConsentRequired)
        val consent = result as DeviceTokenRegisterResult.ConsentRequired
        assertEquals("push_notifications", consent.purpose)
        assertEquals("Consent required for push_notifications", consent.message)
    }

    @Test
    fun `403 without consent_required returns ServerError`() {
        val body = """{"success":false,"message":"Forbidden"}"""
        val result = DeviceTokenRegisterResponseParser.parse(403, body, json)
        assertTrue(result is DeviceTokenRegisterResult.ServerError)
    }

    @Test
    fun `500 with message returns ServerError`() {
        val body = """{"success":false,"message":"Server exploded"}"""
        val result = DeviceTokenRegisterResponseParser.parse(500, body, json)
        assertTrue(result is DeviceTokenRegisterResult.ServerError)
        assertEquals("Server exploded", (result as DeviceTokenRegisterResult.ServerError).message)
    }

    @Test
    fun `500 with empty body returns HttpError`() {
        val result = DeviceTokenRegisterResponseParser.parse(500, "", json)
        assertTrue(result is DeviceTokenRegisterResult.HttpError)
        assertEquals(500, (result as DeviceTokenRegisterResult.HttpError).statusCode)
    }

    @Test
    fun `200 with malformed body returns DecodingError`() {
        val result = DeviceTokenRegisterResponseParser.parse(200, "not-json", json)
        assertTrue(result is DeviceTokenRegisterResult.DecodingError)
    }

    @Test
    fun `200 with missing data device_token returns DecodingError`() {
        val body = """{"success":true,"data":{}}"""
        val result = DeviceTokenRegisterResponseParser.parse(200, body, json)
        assertTrue(result is DeviceTokenRegisterResult.DecodingError)
    }
}
