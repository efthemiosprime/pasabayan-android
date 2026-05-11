package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnreadCountResponseParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `parses canonical shape`() {
        val raw = """{"success": true, "data": {"unread_count": 5}}"""
        assertEquals(5, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `parses data as bare int`() {
        val raw = """{"success": true, "data": 3}"""
        assertEquals(3, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `parses flat snake_case`() {
        val raw = """{"unread_count": 7}"""
        assertEquals(7, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `parses flat camelCase`() {
        val raw = """{"unreadCount": 8}"""
        assertEquals(8, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `parses flat count alias`() {
        val raw = """{"count": 12}"""
        assertEquals(12, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `parses bare number`() {
        assertEquals(42, UnreadCountResponseParser.parse("42", json))
    }

    @Test
    fun `prefers canonical over flat duplicate keys`() {
        val raw = """{"success": true, "data": {"unread_count": 5}, "count": 99}"""
        assertEquals(5, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `prefers data int over flat root keys`() {
        val raw = """{"success": true, "data": 9, "unread_count": 99}"""
        assertEquals(9, UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `returns null for unrelated json`() {
        val raw = """{"foo": "bar"}"""
        assertNull(UnreadCountResponseParser.parse(raw, json))
    }

    @Test
    fun `returns null for malformed input`() {
        assertNull(UnreadCountResponseParser.parse("not json", json))
    }
}
