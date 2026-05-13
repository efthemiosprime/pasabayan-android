package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AttentionSignalsJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `decodes a fully-populated payload`() {
        val raw = """
            {
              "pending_reviews_count": 3,
              "phone_verification_needed": true,
              "payout_setup_needed": true,
              "total": 5,
              "computed_at": "2026-05-13T18:30:00Z",
              "degraded": false
            }
        """.trimIndent()

        val signals = json.decodeFromString<AttentionSignalsJson>(raw)

        assertEquals(3, signals.pendingReviewsCount)
        assertTrue(signals.phoneVerificationNeeded)
        assertTrue(signals.payoutSetupNeeded)
        assertEquals(5, signals.total)
        assertEquals("2026-05-13T18:30:00Z", signals.computedAt)
        assertFalse(signals.degraded)
    }

    @Test
    fun `defaults missing fields to zero, false, and null`() {
        val signals = json.decodeFromString<AttentionSignalsJson>("{}")

        assertEquals(0, signals.pendingReviewsCount)
        assertFalse(signals.phoneVerificationNeeded)
        assertFalse(signals.payoutSetupNeeded)
        assertEquals(0, signals.total)
        assertNull(signals.computedAt)
        assertFalse(signals.degraded)
    }

    @Test
    fun `propagates degraded flag when server reports a partial response`() {
        val raw = """
            {
              "pending_reviews_count": 0,
              "phone_verification_needed": false,
              "payout_setup_needed": false,
              "total": 0,
              "degraded": true
            }
        """.trimIndent()

        val signals = json.decodeFromString<AttentionSignalsJson>(raw)

        assertTrue(signals.degraded)
        assertEquals(0, signals.total)
    }

    @Test
    fun `tolerates unknown extra fields`() {
        val raw = """
            {
              "pending_reviews_count": 1,
              "future_field": "not yet defined",
              "phone_verification_needed": false,
              "payout_setup_needed": false,
              "total": 1,
              "degraded": false
            }
        """.trimIndent()

        val signals = json.decodeFromString<AttentionSignalsJson>(raw)

        assertEquals(1, signals.pendingReviewsCount)
        assertEquals(1, signals.total)
    }
}
