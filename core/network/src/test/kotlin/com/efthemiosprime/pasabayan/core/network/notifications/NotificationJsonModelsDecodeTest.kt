package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // -- PushNotificationJson --

    @Test
    fun `PushNotificationJson decodes full sample`() {
        val raw = """
        {
          "id": 101,
          "title": "New Request",
          "body": "Alice wants to ship a package",
          "type": "match_request",
          "data": {
            "match_id": 42,
            "requester_id": 7,
            "requester_name": "Alice"
          },
          "recipient_role": "carrier",
          "sent_at": "2026-04-01T10:00:00Z",
          "read_at": null,
          "clicked_at": null,
          "is_read": false,
          "created_at": "2026-04-01T10:00:00Z"
        }
        """.trimIndent()
        val notif = json.decodeFromString<PushNotificationJson>(raw)
        assertEquals(101, notif.id)
        assertEquals("match_request", notif.type)
        assertEquals("carrier", notif.recipientRole)
        assertFalse(notif.isRead)
        assertEquals(42, notif.data?.matchId)
        assertEquals("Alice", notif.data?.requesterName)
    }

    @Test
    fun `NotificationDataJson decodes counter-offer payload`() {
        val raw = """
        {
          "match_id": 200,
          "new_price": 75.5,
          "original_price": 60.0,
          "price_difference": 15.5,
          "direction": "up",
          "counter_offerer_name": "Bob",
          "counter_offerer_id": 9,
          "counter_offer_round": 2,
          "initiated_by": "shipper",
          "remaining_counter_offers": 1,
          "can_counter_offer": true
        }
        """.trimIndent()
        val data = json.decodeFromString<NotificationDataJson>(raw)
        assertEquals(200, data.matchId)
        assertEquals(75.5, data.newPrice!!, 0.001)
        assertEquals("up", data.direction)
        assertEquals(2, data.counterOfferRound)
        assertEquals("shipper", data.initiatedBy)
        assertEquals(true, data.canCounterOffer)
    }

    @Test
    fun `PushNotificationJson decodes when optional data is absent`() {
        val raw = """
        {"id":1,"title":"t","body":"b","type":"test","sent_at":"x","is_read":true,"created_at":"x"}
        """.trimIndent()
        val notif = json.decodeFromString<PushNotificationJson>(raw)
        assertNull(notif.data)
        assertTrue(notif.isRead)
    }

    // -- DeviceToken --

    @Test
    fun `DeviceTokenResponseJson decodes nested data device_token`() {
        val raw = """
        {
          "success": true,
          "message": "Device token registered",
          "data": {
            "device_token": {
              "id": 123,
              "platform": "android",
              "is_active": true,
              "created_at": "2026-04-01T10:00:00Z"
            }
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<DeviceTokenResponseJson>(raw)
        assertTrue(res.success)
        val info = res.data?.deviceToken
        assertNotNull(info)
        assertEquals(123, info!!.id)
        assertEquals("android", info.platform)
        assertTrue(info.isActive)
        assertNull(res.consentRequired)
    }

    @Test
    fun `DeviceTokenResponseJson decodes 403 consent error payload`() {
        val raw = """
        {
          "success": false,
          "message": "Consent required for push_notifications",
          "consent_required": "push_notifications"
        }
        """.trimIndent()
        val res = json.decodeFromString<DeviceTokenResponseJson>(raw)
        assertFalse(res.success)
        assertEquals("push_notifications", res.consentRequired)
        assertNull(res.data)
    }

    // -- History + pagination --

    @Test
    fun `NotificationHistoryResponseJson decodes notifications and pagination`() {
        val raw = """
        {
          "success": true,
          "data": {
            "notifications": [
              {"id":1,"title":"a","body":"b","type":"chat_message","sent_at":"x","is_read":false,"created_at":"x"},
              {"id":2,"title":"c","body":"d","type":"payment_received","sent_at":"y","is_read":true,"created_at":"y"}
            ],
            "pagination": {"current_page":2,"last_page":5,"per_page":15,"total":75}
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<NotificationHistoryResponseJson>(raw)
        assertTrue(res.success)
        assertEquals(2, res.data?.notifications?.size)
        val pg = res.data?.pagination!!
        assertEquals(2, pg.currentPage)
        assertEquals(5, pg.lastPage)
        assertEquals(15, pg.perPage)
        assertEquals(75, pg.total)
        assertTrue(pg.hasMore)
    }

    @Test
    fun `PaginationInfoJson hasMore is false on last page`() {
        val pg = PaginationInfoJson(currentPage = 5, lastPage = 5, perPage = 15, total = 75)
        assertFalse(pg.hasMore)
    }

    // -- MarkRead --

    @Test
    fun `MarkReadResponseJson decodes single read`() {
        val raw = """
        {"success":true,"message":"ok","data":{"notification_id":123,"read_at":"2026-04-01T10:05:00Z"}}
        """.trimIndent()
        val res = json.decodeFromString<MarkReadResponseJson>(raw)
        assertTrue(res.success)
        assertEquals(123, res.data?.notificationId)
        assertEquals("2026-04-01T10:05:00Z", res.data?.readAt)
    }

    @Test
    fun `MarkAllReadDataJson prefers marked_count`() {
        val raw = """{"marked_count": 7, "updated_count": 99}"""
        val data = json.decodeFromString<MarkAllReadDataJson>(raw)
        assertEquals(7, data.resolvedCount)
    }

    @Test
    fun `MarkAllReadDataJson falls back to updated_count`() {
        val raw = """{"updated_count": 4}"""
        val data = json.decodeFromString<MarkAllReadDataJson>(raw)
        assertEquals(4, data.resolvedCount)
    }

    @Test
    fun `MarkAllReadDataJson returns 0 when both absent`() {
        val data = json.decodeFromString<MarkAllReadDataJson>("{}")
        assertEquals(0, data.resolvedCount)
    }
}
