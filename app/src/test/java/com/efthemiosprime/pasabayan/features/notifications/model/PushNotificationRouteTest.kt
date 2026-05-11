package com.efthemiosprime.pasabayan.features.notifications.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PushNotificationRouteTest {

    @Test
    fun `fromPushData coerces int and double values from string keys`() {
        val payload = mapOf(
            "type" to "payment_received",
            "notification_id" to "501",
            "match_id" to "42",
            "transaction_id" to "9001",
            "amount" to "125.75",
            "currency" to "cad",
            "recipient_role" to "carrier",
        )
        val route = PushNotificationRoute.fromPushData(payload)
        assertEquals(NotificationType.PAYMENT_RECEIVED, route.type)
        assertEquals(501, route.notificationId)
        assertEquals(42, route.matchId)
        assertEquals(9001, route.transactionId)
        assertEquals(125.75, route.amount!!, 0.001)
        assertEquals("cad", route.currency)
        assertEquals("carrier", route.recipientRole)
    }

    @Test
    fun `fromPushData falls back to UNKNOWN type when type missing`() {
        val route = PushNotificationRoute.fromPushData(emptyMap())
        assertEquals(NotificationType.UNKNOWN, route.type)
        assertNull(route.matchId)
        assertNull(route.data)
    }

    @Test
    fun `fromPushData decodes counter-offer payload fields`() {
        val payload = mapOf(
            "type" to "counter_offer",
            "match_id" to "200",
            "new_price" to "75.5",
            "original_price" to "60",
            "price_difference" to "15.5",
            "direction" to "up",
            "counter_offerer_name" to "Bob",
            "counter_offerer_id" to "9",
            "counter_offer_round" to "2",
            "initiated_by" to "shipper",
            "remaining_counter_offers" to "1",
            "can_counter_offer" to "true",
        )
        val route = PushNotificationRoute.fromPushData(payload)
        assertEquals(NotificationType.COUNTER_OFFER, route.type)
        val data = route.data!!
        assertEquals(200, data.matchId)
        assertEquals(75.5, data.newPrice!!, 0.001)
        assertEquals(60.0, data.originalPrice!!, 0.001)
        assertEquals("up", data.direction)
        assertEquals(2, data.counterOfferRound)
        assertEquals("shipper", data.initiatedBy)
        assertEquals(true, data.canCounterOffer)
    }

    @Test
    fun `fromPushData ignores unparseable numeric fields`() {
        val payload = mapOf(
            "type" to "match_request",
            "match_id" to "not_a_number",
            "amount" to "abc",
        )
        val route = PushNotificationRoute.fromPushData(payload)
        assertEquals(NotificationType.MATCH_REQUEST, route.type)
        assertNull(route.matchId)
        assertNull(route.amount)
    }

    @Test
    fun `fromPushData leaves data null when no payload-shape fields present`() {
        val payload = mapOf("type" to "test")
        val route = PushNotificationRoute.fromPushData(payload)
        assertTrue(route.data == null)
    }
}
