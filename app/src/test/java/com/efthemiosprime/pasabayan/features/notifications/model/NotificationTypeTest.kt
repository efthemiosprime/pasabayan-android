package com.efthemiosprime.pasabayan.features.notifications.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTypeTest {

    @Test
    fun `every raw value resolves to its enum case`() {
        val cases = mapOf(
            "match_request" to NotificationType.MATCH_REQUEST,
            "match_accepted" to NotificationType.MATCH_ACCEPTED,
            "match_declined" to NotificationType.MATCH_DECLINED,
            "chat_message" to NotificationType.CHAT_MESSAGE,
            "delivery_picked_up" to NotificationType.DELIVERY_PICKED_UP,
            "delivery_in_transit" to NotificationType.DELIVERY_IN_TRANSIT,
            "delivery_delivered" to NotificationType.DELIVERY_DELIVERED,
            "match_auto_cancelled" to NotificationType.MATCH_AUTO_CANCELLED,
            "payment_received" to NotificationType.PAYMENT_RECEIVED,
            "payment_released" to NotificationType.PAYMENT_RELEASED,
            "rating_received" to NotificationType.RATING_RECEIVED,
            "premium_approved" to NotificationType.PREMIUM_APPROVED,
            "premium_rejected" to NotificationType.PREMIUM_REJECTED,
            "test" to NotificationType.TEST,
            "payout_completed" to NotificationType.PAYOUT_COMPLETED,
            "payout_scheduled" to NotificationType.PAYOUT_SCHEDULED,
            "refund_processed" to NotificationType.REFUND_PROCESSED,
            "tip_received" to NotificationType.TIP_RECEIVED,
            "payment_auto_charge_failed" to NotificationType.PAYMENT_AUTO_CHARGE_FAILED,
            "counter_offer" to NotificationType.COUNTER_OFFER,
        )
        for ((raw, expected) in cases) {
            assertEquals("raw '$raw' should map to $expected", expected, NotificationType.fromRaw(raw))
        }
    }

    @Test
    fun `unknown raw value falls back to UNKNOWN`() {
        assertEquals(NotificationType.UNKNOWN, NotificationType.fromRaw("some_new_type"))
        assertEquals(NotificationType.UNKNOWN, NotificationType.fromRaw(""))
        assertEquals(NotificationType.UNKNOWN, NotificationType.fromRaw(null))
    }

    @Test
    fun `every case exposes non-null icon color and displayName resource`() {
        for (case in NotificationType.entries) {
            assertNotNull("$case missing icon", case.icon)
            assertNotNull("$case missing color", case.color)
            assertTrue("$case displayNameRes must be a valid resource id", case.displayNameRes != 0)
        }
    }

    @Test
    fun `UNKNOWN rawValue is empty so it never collides with backend strings`() {
        assertEquals("", NotificationType.UNKNOWN.rawValue)
    }
}
