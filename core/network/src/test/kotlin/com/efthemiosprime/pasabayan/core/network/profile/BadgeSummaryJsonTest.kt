package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeSummaryJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `decodes a fully-populated payload`() {
        val raw = """
            {
              "unread_notifications": 4,
              "action_required": {
                "total": 6,
                "active_deliveries_needing_update": 1,
                "pending_booking_requests": 2,
                "packages_ready_for_pickup": 1,
                "trips_without_activity": 2
              },
              "pending_reviews_count": 3,
              "verification": {
                "phone_verification_needed": true,
                "payout_setup_needed": true
              },
              "total": 15,
              "computed_at": "2026-05-14T17:59:41Z",
              "degraded": false
            }
        """.trimIndent()

        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertEquals(4, summary.unreadNotifications)
        assertEquals(6, summary.actionRequired.total)
        assertEquals(1, summary.actionRequired.activeDeliveriesNeedingUpdate)
        assertEquals(2, summary.actionRequired.pendingBookingRequests)
        assertEquals(1, summary.actionRequired.packagesReadyForPickup)
        assertEquals(2, summary.actionRequired.tripsWithoutActivity)
        assertEquals(3, summary.pendingReviewsCount)
        assertTrue(summary.verification.phoneVerificationNeeded)
        assertTrue(summary.verification.payoutSetupNeeded)
        assertEquals(15, summary.total)
        assertEquals("2026-05-14T17:59:41Z", summary.computedAt)
        assertFalse(summary.degraded)
    }

    @Test
    fun `defaults every field when payload is empty object`() {
        val summary = json.decodeFromString<BadgeSummaryJson>("{}")

        assertEquals(0, summary.unreadNotifications)
        assertEquals(ActionRequiredJson(), summary.actionRequired)
        assertEquals(0, summary.pendingReviewsCount)
        assertEquals(VerificationJson(), summary.verification)
        assertEquals(0, summary.total)
        assertNull(summary.computedAt)
        assertFalse(summary.degraded)
    }

    @Test
    fun `defaults unread_notifications when key missing`() {
        val raw = """{ "total": 1 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertEquals(0, summary.unreadNotifications)
        assertEquals(1, summary.total)
    }

    @Test
    fun `defaults action_required to empty object when key missing`() {
        val raw = """{ "total": 0 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertEquals(ActionRequiredJson(), summary.actionRequired)
        assertEquals(0, summary.actionRequired.total)
        assertEquals(0, summary.actionRequired.pendingBookingRequests)
    }

    @Test
    fun `defaults verification to empty object when key missing`() {
        val raw = """{ "total": 0 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertFalse(summary.verification.phoneVerificationNeeded)
        assertFalse(summary.verification.payoutSetupNeeded)
    }

    @Test
    fun `defaults pending_reviews_count when key missing`() {
        val raw = """{ "total": 0 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertEquals(0, summary.pendingReviewsCount)
    }

    @Test
    fun `defaults computed_at to null when key missing`() {
        val raw = """{ "total": 0 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertNull(summary.computedAt)
    }

    @Test
    fun `defaults degraded to false when key missing`() {
        val raw = """{ "total": 0 }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)
        assertFalse(summary.degraded)
    }

    @Test
    fun `decodes account-setup-only fixture (regression for iOS empty-drawer bug)`() {
        val raw = """
            {
              "unread_notifications": 0,
              "action_required": { "total": 0 },
              "pending_reviews_count": 0,
              "verification": {
                "phone_verification_needed": true,
                "payout_setup_needed": true
              },
              "total": 2,
              "degraded": false
            }
        """.trimIndent()

        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertEquals(0, summary.unreadNotifications)
        assertEquals(0, summary.actionRequired.total)
        assertEquals(0, summary.pendingReviewsCount)
        assertTrue(summary.verification.phoneVerificationNeeded)
        assertTrue(summary.verification.payoutSetupNeeded)
        assertEquals(2, summary.total)
    }

    @Test
    fun `propagates degraded flag when server reports a partial response`() {
        val raw = """
            {
              "unread_notifications": 1,
              "action_required": { "total": 0 },
              "pending_reviews_count": 0,
              "verification": { "phone_verification_needed": false, "payout_setup_needed": false },
              "total": 1,
              "degraded": true
            }
        """.trimIndent()

        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertTrue(summary.degraded)
        assertEquals(1, summary.total)
        assertEquals(1, summary.unreadNotifications)
    }

    @Test
    fun `defaults partial action_required nested fields`() {
        val raw = """
            {
              "action_required": { "pending_booking_requests": 2 }
            }
        """.trimIndent()

        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertEquals(0, summary.actionRequired.total)
        assertEquals(0, summary.actionRequired.activeDeliveriesNeedingUpdate)
        assertEquals(2, summary.actionRequired.pendingBookingRequests)
        assertEquals(0, summary.actionRequired.packagesReadyForPickup)
        assertEquals(0, summary.actionRequired.tripsWithoutActivity)
    }

    @Test
    fun `defaults partial verification nested fields`() {
        val raw = """{ "verification": { "phone_verification_needed": true } }"""
        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertTrue(summary.verification.phoneVerificationNeeded)
        assertFalse(summary.verification.payoutSetupNeeded)
    }

    @Test
    fun `tolerates unknown extra fields`() {
        val raw = """
            {
              "unread_notifications": 1,
              "action_required": {
                "total": 0,
                "future_action": "soon"
              },
              "future_top_level": [1, 2, 3],
              "total": 1,
              "degraded": false
            }
        """.trimIndent()

        val summary = json.decodeFromString<BadgeSummaryJson>(raw)

        assertEquals(1, summary.unreadNotifications)
        assertEquals(1, summary.total)
    }
}
