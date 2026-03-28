package com.efthemiosprime.pasabayan.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateTimeParsingTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun epochOf(dateStr: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = utc
        return sdf.parse(dateStr)!!.time
    }

    // -- parseApiDate --

    @Test
    fun `parseApiDate parses date-only string`() {
        val result = DateTimeParsing.parseApiDate("2026-03-28")
        assertEquals(epochOf("2026-03-28"), result)
    }

    @Test
    fun `parseApiDate parses ISO8601 with Z`() {
        val result = DateTimeParsing.parseApiDate("2026-03-28T14:30:00Z")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDate extracts date from ISO8601 with fractional seconds`() {
        val result = DateTimeParsing.parseApiDate("2026-03-28T14:30:00.123Z")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDate returns null for null`() {
        assertNull(DateTimeParsing.parseApiDate(null))
    }

    @Test
    fun `parseApiDate returns null for empty string`() {
        assertNull(DateTimeParsing.parseApiDate(""))
    }

    @Test
    fun `parseApiDate returns null for blank string`() {
        assertNull(DateTimeParsing.parseApiDate("   "))
    }

    @Test
    fun `parseApiDate returns null for garbage`() {
        assertNull(DateTimeParsing.parseApiDate("not-a-date"))
    }

    // -- parseApiDateTime --

    @Test
    fun `parseApiDateTime parses ISO8601 UTC`() {
        val result = DateTimeParsing.parseApiDateTime("2026-03-28T14:30:00Z")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDateTime parses ISO8601 with milliseconds`() {
        val result = DateTimeParsing.parseApiDateTime("2026-03-28T14:30:00.123Z")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDateTime parses ISO8601 with microseconds`() {
        val result = DateTimeParsing.parseApiDateTime("2026-03-28T14:30:00.123456Z")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDateTime parses space-separated format`() {
        val result = DateTimeParsing.parseApiDateTime("2026-03-28 14:30:00")
        assertNotNull(result)
    }

    @Test
    fun `parseApiDateTime falls back to date-only`() {
        val result = DateTimeParsing.parseApiDateTime("2026-03-28")
        assertEquals(epochOf("2026-03-28"), result)
    }

    @Test
    fun `parseApiDateTime returns null for null`() {
        assertNull(DateTimeParsing.parseApiDateTime(null))
    }

    @Test
    fun `parseApiDateTime returns null for garbage`() {
        assertNull(DateTimeParsing.parseApiDateTime("nope"))
    }

    // -- combineDateAndTime --

    @Test
    fun `combineDateAndTime with time string`() {
        val result = DateTimeParsing.combineDateAndTime("2026-03-28", "14:30")
        assertNotNull(result)
        val formatted = DateTimeParsing.formatDateTime(result!!)
        assert(formatted.contains("2:30 PM") || formatted.contains("14:30")) {
            "Expected time in formatted output, got: $formatted"
        }
    }

    @Test
    fun `combineDateAndTime with null time returns date at midnight`() {
        val result = DateTimeParsing.combineDateAndTime("2026-03-28", null)
        assertEquals(epochOf("2026-03-28"), result)
    }

    @Test
    fun `combineDateAndTime with blank time returns date at midnight`() {
        val result = DateTimeParsing.combineDateAndTime("2026-03-28", "")
        assertEquals(epochOf("2026-03-28"), result)
    }

    @Test
    fun `combineDateAndTime returns null for null date`() {
        assertNull(DateTimeParsing.combineDateAndTime(null, "14:30"))
    }

    // -- formatDateOnly --

    @Test
    fun `formatDateOnly produces readable date`() {
        val formatted = DateTimeParsing.formatDateOnly(epochOf("2026-03-28"))
        assertEquals("Mar 28, 2026", formatted)
    }

    // -- formatDateTime --

    @Test
    fun `formatDateTime produces readable datetime`() {
        val millis = epochOf("2026-03-28") // midnight UTC
        val formatted = DateTimeParsing.formatDateTime(millis)
        assert(formatted.contains("Mar 28, 2026")) {
            "Expected date in output, got: $formatted"
        }
    }

    // -- parseTimeString --

    @Test
    fun `parseTimeString parses HH-mm`() {
        assertNotNull(DateTimeParsing.parseTimeString("14:30"))
    }

    @Test
    fun `parseTimeString parses HH-mm-ss`() {
        assertNotNull(DateTimeParsing.parseTimeString("14:30:45"))
    }

    @Test
    fun `parseTimeString parses H-mm`() {
        assertNotNull(DateTimeParsing.parseTimeString("9:05"))
    }

    @Test
    fun `parseTimeString parses 12-hour format`() {
        assertNotNull(DateTimeParsing.parseTimeString("2:30 PM"))
    }

    @Test
    fun `parseTimeString parses microsecond format`() {
        assertNotNull(DateTimeParsing.parseTimeString("14:30:00.123456"))
    }

    @Test
    fun `parseTimeString returns null for null`() {
        assertNull(DateTimeParsing.parseTimeString(null))
    }

    @Test
    fun `parseTimeString returns null for empty`() {
        assertNull(DateTimeParsing.parseTimeString(""))
    }

    @Test
    fun `parseTimeString returns null for garbage`() {
        assertNull(DateTimeParsing.parseTimeString("not-a-time"))
    }
}
