package com.efthemiosprime.pasabayan.core.network

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorMapperTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `401 is Unauthorized`() {
        val e = ApiErrorMapper.map(401, "{}".toByteArray(), json)
        assertEquals(DomainError.Unauthorized, e)
    }

    @Test
    fun `404 with trip message is TripNotFound`() {
        val body = """{"message":"CarrierTrip not found"}"""
        val e = ApiErrorMapper.map(404, body.toByteArray(), json)
        assertTrue(e is DomainError.TripNotFound)
        assertEquals("CarrierTrip not found", (e as DomainError.TripNotFound).message)
    }

    @Test
    fun `404 without trip wording is NotFound`() {
        val body = """{"message":"Nothing here"}"""
        val e = ApiErrorMapper.map(404, body.toByteArray(), json)
        assertEquals(DomainError.NotFound, e)
    }

    @Test
    fun `402 payment_required maps to PaymentRequired`() {
        val body = """{"error":"payment_required","message":"Pay up"}"""
        val e = ApiErrorMapper.map(402, body.toByteArray(), json)
        assertTrue(e is DomainError.PaymentRequired)
        assertEquals("Pay up", (e as DomainError.PaymentRequired).message)
    }

    @Test
    fun `409 maps message and expires_at`() {
        val body = """{"message":"Wait","expires_at":"2026-01-01"}"""
        val e = ApiErrorMapper.map(409, body.toByteArray(), json)
        assertTrue(e is DomainError.Conflict)
        val c = e as DomainError.Conflict
        assertEquals("Wait", c.message)
        assertEquals("2026-01-01", c.expiresAt)
    }

    @Test
    fun `409 fully booked message maps to TripOvercommitted`() {
        val body = """{"success":false,"message":"Trip is fully booked; no remaining capacity to accept matches.","current_status":null}"""
        val e = ApiErrorMapper.map(409, body.toByteArray(), json)
        assertTrue("expected TripOvercommitted, got $e", e is DomainError.TripOvercommitted)
        assertEquals("Trip is fully booked; no remaining capacity to accept matches.", (e as DomainError.TripOvercommitted).message)
    }

    @Test
    fun `409 generic conflict message stays as Conflict (regression)`() {
        val body = """{"message":"This action cannot be completed in its current state."}"""
        val e = ApiErrorMapper.map(409, body.toByteArray(), json)
        assertTrue("expected Conflict, got $e", e is DomainError.Conflict)
    }

    @Test
    fun `422 capacity_acknowledgment_required maps to CapacityAcknowledgmentRequired with kg fields`() {
        val body = """{
            "success": false,
            "error": "capacity_acknowledgment_required",
            "message": "Package weight (2kg) exceeds your stated capacity (1kg) by 1kg. Confirm you can accommodate by retrying with acknowledge_overage=true.",
            "compatibility": {
                "weight_over_capacity": true,
                "package_weight_kg": 2.0,
                "trip_available_weight_kg": 1.0,
                "overage_kg": 1.0,
                "requires_capacity_acknowledgment": true
            }
        }"""
        val e = ApiErrorMapper.map(422, body.toByteArray(), json)
        assertTrue("expected CapacityAcknowledgmentRequired, got $e", e is DomainError.CapacityAcknowledgmentRequired)
        val c = e as DomainError.CapacityAcknowledgmentRequired
        assertEquals(2.0, c.packageWeightKg!!, 0.001)
        assertEquals(1.0, c.tripAvailableWeightKg!!, 0.001)
        assertEquals(1.0, c.overageKg!!, 0.001)
        assertTrue(c.message!!.contains("exceeds"))
    }

    @Test
    fun `422 field validation without capacity error stays as ValidationError (regression)`() {
        val body = """{"message":"Invalid","errors":{"offered_price":["must be greater than 0"]}}"""
        val e = ApiErrorMapper.map(422, body.toByteArray(), json)
        assertTrue("expected ValidationError, got $e", e is DomainError.ValidationError)
    }

    @Test
    fun `422 validation body maps to ValidationError`() {
        val body = """{"message":"Invalid","errors":{"email":["bad"]}}"""
        val e = ApiErrorMapper.map(422, body.toByteArray(), json)
        assertTrue(e is DomainError.ValidationError)
        val v = e as DomainError.ValidationError
        assertEquals("Invalid", v.message)
        assertEquals(listOf("bad"), v.fieldErrors["email"])
    }

    @Test
    fun `422 carrier_onboarding_required takes precedence`() {
        val body = """{"message":"Finish profile","carrier_onboarding_required":true}"""
        val e = ApiErrorMapper.map(422, body.toByteArray(), json)
        assertTrue(e is DomainError.CarrierOnboardingRequired)
        assertEquals("Finish profile", (e as DomainError.CarrierOnboardingRequired).message)
    }

    @Test
    fun `400 validation maps to ValidationError`() {
        val body = """{"message":"Bad input","errors":{"x":["y"]}}"""
        val e = ApiErrorMapper.map(400, body.toByteArray(), json)
        assertTrue(e is DomainError.ValidationError)
    }

    @Test
    fun `500 extracts message`() {
        val body = """{"message":"DB exploded"}"""
        val e = ApiErrorMapper.map(500, body.toByteArray(), json)
        assertTrue(e is DomainError.ServerError)
        assertEquals("DB exploded", (e as DomainError.ServerError).message)
    }

    @Test
    fun `403 maps to ServerError with message`() {
        val body = """{"message":"Forbidden thing"}"""
        val e = ApiErrorMapper.map(403, body.toByteArray(), json)
        assertTrue(e is DomainError.ServerError)
        assertEquals("Forbidden thing", (e as DomainError.ServerError).message)
    }

    @Test
    fun `429 maps to RateLimited`() {
        val e = ApiErrorMapper.map(429, "{}".toByteArray(), json)
        assertTrue(e is DomainError.RateLimited)
    }
}
