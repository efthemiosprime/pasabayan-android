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
    fun `ConversationsResponseJson decodes the live API paginator shape`() {
        // Live API (ChatController@index) wraps the paginator under `data`:
        //   { "message": "...", "data": { "current_page": 1, "data": [...], "last_page": 1, ... } }
        // iOS reads $0.data.data; Android must do the same via PaginatedConversationsJson.
        val raw = """
            {
              "message": "Conversations retrieved successfully",
              "data": {
                "current_page": 1,
                "last_page": 1,
                "per_page": 15,
                "total": 1,
                "data": [
                  {
                    "id": 77,
                    "status": "active",
                    "status_display": "Active",
                    "unread_count": 1,
                    "other_participant": {
                      "id": 10,
                      "name": "Carrier One"
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationsResponseJson>(raw)

        assertEquals(1, decoded.conversationsOrEmpty().size)
        assertEquals(77, decoded.conversationsOrEmpty().first().id)
        assertEquals(1, decoded.data!!.currentPage)
        assertEquals(1, decoded.data.lastPage)
        assertEquals(1, decoded.data.total)
    }

    @Test
    fun `ConversationsResponseJson falls back to legacy flat conversations array`() {
        // Older fixtures and any realtime helper that wraps a list under `conversations`
        // continue to decode via the legacy fallback path.
        val raw = """
            {
              "message": "Conversations retrieved",
              "conversations": [
                {
                  "id": 77,
                  "status": "active",
                  "status_display": "Active",
                  "unread_count": 1,
                  "other_participant": {
                    "id": 10,
                    "name": "Carrier One"
                  }
                }
              ]
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationsResponseJson>(raw)

        assertEquals(1, decoded.conversationsOrEmpty().size)
        assertEquals(77, decoded.conversationsOrEmpty().first().id)
    }

    @Test
    fun `ConversationsResponseJson exposes paginator page numbers for infinite scroll`() {
        // Mirrors iOS ChatViewModel.loadNextConversationsPage: page 2 of N means
        // hasMore = currentPage < lastPage. The decoder must surface both so the
        // VM can drive the prefetch trigger and stop at the last page.
        val raw = """
            {
              "message": "Conversations retrieved successfully",
              "data": {
                "current_page": 2,
                "last_page": 4,
                "per_page": 15,
                "total": 47,
                "data": [
                  {
                    "id": 78,
                    "status": "active",
                    "status_display": "Active",
                    "unread_count": 0,
                    "other_participant": { "id": 11, "name": "Carrier Two" }
                  }
                ]
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationsResponseJson>(raw)

        assertEquals(1, decoded.conversationsOrEmpty().size)
        assertEquals(2, decoded.resolvedCurrentPage())
        assertEquals(4, decoded.resolvedLastPage())
        assertEquals(47, decoded.resolvedTotal())
        assertTrue(decoded.hasMorePages())
    }

    @Test
    fun `ConversationsResponseJson legacy flat array decodes as single page`() {
        // The legacy flat-array shape has no paginator metadata. Treat it as a
        // single page so the VM's hasMore predicate naturally evaluates false.
        val raw = """
            {
              "message": "Conversations retrieved",
              "conversations": [
                { "id": 77, "status": "active", "status_display": "Active", "unread_count": 0,
                  "other_participant": { "id": 10, "name": "Carrier One" } }
              ]
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationsResponseJson>(raw)

        assertEquals(1, decoded.resolvedCurrentPage())
        assertEquals(1, decoded.resolvedLastPage())
        assertFalse(decoded.hasMorePages())
    }

    @Test
    fun `ConversationsResponseJson decodes empty paginator without crashing`() {
        // First-time user with no conversations — the API still emits the
        // paginator envelope with an empty `data` array. We must not throw.
        val raw = """
            {
              "message": "Conversations retrieved successfully",
              "data": {
                "current_page": 1,
                "last_page": 1,
                "per_page": 15,
                "total": 0,
                "data": []
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<ConversationsResponseJson>(raw)

        assertEquals(0, decoded.conversationsOrEmpty().size)
    }

    @Test
    fun `MessagesResponseJson decodes nested messages paginator wrapper`() {
        val raw = """
            {
              "message": "Messages retrieved",
              "messages": {
                "current_page": 2,
                "last_page": 5,
                "data": [
                  {
                    "id": 101,
                    "message": "hello",
                    "message_type": "text",
                    "created_at": "2026-01-01T00:00:00Z"
                  }
                ]
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<MessagesResponseJson>(raw)

        assertEquals(1, decoded.messagesOrEmpty().size)
        assertEquals(2, decoded.resolvedCurrentPage())
        assertEquals(5, decoded.resolvedLastPage())
    }

    @Test
    fun `SenderJson decodes is_me from the API payload`() {
        val raw = """
            {
              "id": 42,
              "name": "Alice",
              "is_me": true
            }
        """.trimIndent()

        val decoded = json.decodeFromString<SenderJson>(raw)

        assertEquals(42, decoded.id)
        assertEquals("Alice", decoded.name)
        assertEquals(true, decoded.isMe)
    }

    @Test
    fun `SenderJson defaults is_me to false when omitted`() {
        // Older payloads / synthetic system-message sender objects may omit is_me.
        val raw = """{ "id": 0, "name": "System" }"""

        val decoded = json.decodeFromString<SenderJson>(raw)

        assertEquals(false, decoded.isMe)
    }

    @Test
    fun `LastMessageJson decodes is_system_message`() {
        val raw = """
            {
              "id": 5,
              "message": "Match created",
              "message_type": "system",
              "is_system_message": true,
              "created_at": "2026-01-01T00:00:00Z"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<LastMessageJson>(raw)

        assertEquals(true, decoded.isSystemMessage)
    }

    @Test
    fun `LastMessageJson defaults is_system_message to false when omitted`() {
        val raw = """
            {
              "id": 5,
              "message": "Regular chat message",
              "message_type": "text"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<LastMessageJson>(raw)

        assertEquals(false, decoded.isSystemMessage)
    }

    @Test
    fun `SendMessageResponseJson prefers chat_message when present`() {
        val raw = """
            {
              "message": "Message sent successfully",
              "chat_message": {
                "id": 202,
                "message": "sent",
                "message_type": "text",
                "created_at": "2026-01-01T00:00:00Z"
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<SendMessageResponseJson>(raw)

        assertNotNull(decoded.messageOrNull())
        assertEquals(202, decoded.messageOrNull()!!.id)
    }

    @Test
    fun `SendMessageResponseJson decodes legacy message object`() {
        val raw = """
            {
              "message": {
                "id": 303,
                "message": "legacy",
                "message_type": "text",
                "created_at": "2026-01-01T00:00:00Z"
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<SendMessageResponseJson>(raw)

        assertNotNull(decoded.messageOrNull())
        assertEquals(303, decoded.messageOrNull()!!.id)
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
