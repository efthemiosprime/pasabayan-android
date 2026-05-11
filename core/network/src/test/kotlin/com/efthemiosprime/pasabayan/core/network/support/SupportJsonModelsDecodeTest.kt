package com.efthemiosprime.pasabayan.core.network.support

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `SupportTicketResponseJson decodes full sample`() {
        val raw = """
        {
          "success": true,
          "message": "Ticket created",
          "data": {
            "id": 501,
            "email": "user@example.com",
            "subject": "Delivery never arrived",
            "category": "delivery_issues",
            "priority": "high",
            "status": "open",
            "description": "Carrier did not show up.",
            "created_at": "2026-05-11T03:00:00Z",
            "updated_at": "2026-05-11T03:00:00Z",
            "user_id": 42,
            "attachments": [
              {"path": "/uploads/a.png", "original_name": "screenshot.png", "mime": "image/png", "size_kb": 128.5}
            ]
          }
        }
        """.trimIndent()
        val res = json.decodeFromString<SupportTicketResponseJson>(raw)
        assertTrue(res.success)
        val ticket = res.data!!
        assertEquals(501, ticket.id)
        assertEquals("delivery_issues", ticket.category)
        assertEquals("high", ticket.priority)
        assertEquals(1, ticket.attachments?.size)
        val att = ticket.attachments!![0]
        assertEquals("screenshot.png", att.originalName)
        assertEquals(128.5, att.sizeKb, 0.001)
    }

    @Test
    fun `SupportTicketResponseJson decodes failure body`() {
        val raw = """{"success": false, "message": "validation failed"}"""
        val res = json.decodeFromString<SupportTicketResponseJson>(raw)
        assertEquals(false, res.success)
        assertEquals("validation failed", res.message)
        assertNull(res.data)
    }

    @Test
    fun `SupportTicketJson decodes without attachments`() {
        val raw = """
        {"id": 1, "email": "x@x.com", "subject": "s", "category": "other", "priority": "low", "description": "d"}
        """.trimIndent()
        val ticket = json.decodeFromString<SupportTicketJson>(raw)
        assertEquals(1, ticket.id)
        assertNull(ticket.attachments)
        assertNull(ticket.status)
        assertNotNull(ticket.subject)
    }
}
