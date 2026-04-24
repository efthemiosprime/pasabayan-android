package com.efthemiosprime.pasabayan.core.network.chat

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `ConversationSummary decodes nested payload`() {
        val raw = """
            {
              "id": 10,
              "match_id": 77,
              "status": "active",
              "status_display": "Active",
              "user_role": "shipper",
              "other_participant": {
                "id": 9,
                "name": "Carrier One",
                "avatar": "https://cdn/avatar.png",
                "verification_level": "verified"
              },
              "match_info": {
                "route": "Toronto → Vancouver",
                "package_description": "Laptop",
                "trip_route": "YYZ-YVR",
                "departure_date": "2026-04-24T00:00:00Z"
              },
              "unread_count": 3,
              "last_message": {
                "id": 222,
                "message": "Hello",
                "message_type": "text",
                "sender_name": "Carrier One",
                "created_at": "2026-04-24T00:00:00Z"
              },
              "last_message_at": "2026-04-24T00:00:00Z",
              "created_at": "2026-04-20T00:00:00Z"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationSummaryJson>(raw)
        assertEquals(10, decoded.id)
        assertEquals(77, decoded.matchId)
        assertEquals("active", decoded.status)
        assertEquals("Carrier One", decoded.otherParticipant?.name)
        assertEquals("Toronto → Vancouver", decoded.matchInfo?.route)
        assertEquals(3, decoded.unreadCount)
        assertTrue(decoded.hasUnreadMessages)
        assertEquals(222, decoded.lastMessage?.id)
    }

    @Test
    fun `MessageItem decodes metadata read receipts and soft delete`() {
        val raw = """
            {
              "id": 501,
              "message": "Bring 2 apples",
              "message_type": "text",
              "sender": {"id": 3, "name": "Shipper A", "avatar": null},
              "is_read": true,
              "created_at": "2026-04-24T01:00:00Z",
              "formatted_message": "Bring 2 apples",
              "message_type_display": "Text",
              "read_at": "2026-04-24T01:10:00Z",
              "read_receipts": {"3": "2026-04-24T01:10:00Z", "9": "2026-04-24T01:12:00Z"},
              "delivery_status": "read",
              "delivered_at": "2026-04-24T01:05:00Z",
              "attachments": [{"id": 1, "url": "https://cdn/file.jpg", "type": "image"}],
              "can_edit": false,
              "can_delete": true,
              "is_deleted": true,
              "deleted_at": "2026-04-24T02:00:00Z",
              "metadata": {
                "type": "service_list_item",
                "list_item_index": 1,
                "list_item_total": 3,
                "list_item": {
                  "item": "Apple",
                  "quantity": 2,
                  "notes": "Green only",
                  "note_urls": ["https://cdn/note.jpg"]
                }
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<MessageItemJson>(raw)
        assertEquals(501, decoded.id)
        assertEquals("text", decoded.messageType)
        assertEquals("Shipper A", decoded.sender?.name)
        assertEquals("read", decoded.deliveryStatus)
        assertEquals(2, decoded.readReceipts.size)
        assertTrue(decoded.hasAttachments)
        assertTrue(decoded.isDeleted)
        assertEquals("service_list_item", decoded.metadata?.type)
        assertEquals("Apple", decoded.metadata?.listItem?.item)
        assertFalse(decoded.isSystemMessage)
    }

    @Test
    fun `BroadcastingConfig and ChannelAuth decode`() {
        val configRaw = """
            {
              "success": true,
              "reverb": {
                "key": "pasabayan",
                "host": "reverb.pasabayan.com",
                "port": 443,
                "scheme": "https"
              }
            }
        """.trimIndent()
        val authRaw = """{"auth":"token-signature"}"""

        val config = json.decodeFromString<BroadcastingConfigResponseJson>(configRaw)
        val auth = json.decodeFromString<ChannelAuthResponseJson>(authRaw)

        assertTrue(config.success)
        assertNotNull(config.reverb)
        assertEquals("reverb.pasabayan.com", config.reverb?.host)
        assertEquals(443, config.reverb?.port)
        assertEquals("https", config.reverb?.scheme)
        assertEquals("token-signature", auth.auth)
    }

    @Test
    fun `SendMessageRequest encodes expected keys`() {
        val encoded = json.encodeToString(
            SendMessageRequestJson.serializer(),
            SendMessageRequestJson(message = "hello", type = "system"),
        )
        assertTrue(encoded.contains("\"message\":\"hello\""))
        assertTrue(encoded.contains("\"type\":\"system\""))
    }
}
