package com.efthemiosprime.pasabayan.core.domain.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * String-based date/time parsing with a fallback chain matching iOS format support.
 *
 * Supports: yyyy-MM-dd, ISO8601 with/without fractional seconds, microseconds,
 * timezone offset variants. All parsing is done with [SimpleDateFormat] to avoid
 * java.time desugaring requirements on minSdk 25.
 */
object DateTimeParsing {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    // Ordered from most specific to least; first match wins.
    private val dateTimeFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",   // microseconds UTC
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",       // milliseconds UTC
        "yyyy-MM-dd'T'HH:mm:ss'Z'",           // no fractional UTC
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",      // microseconds with offset
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",         // milliseconds with offset
        "yyyy-MM-dd'T'HH:mm:ssX",             // no fractional with offset
        "yyyy-MM-dd HH:mm:ss",                // space separator, no timezone
    )

    private val dateOnlyFormat = "yyyy-MM-dd"

    private val displayDateFormat = "MMM d, yyyy"
    private val displayDateTimeFormat = "MMM d, yyyy h:mm a"

    private val timeFormats = listOf(
        "HH:mm:ss.SSSSSS",
        "HH:mm:ss",
        "HH:mm",
        "H:mm:ss",
        "H:mm",
        "h:mm:ss a",
        "h:mm a",
    )

    /**
     * Parse a date-only string (yyyy-MM-dd). Returns epoch millis or null.
     */
    fun parseApiDate(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val trimmed = value.trim()

        // Try date-only first
        parseDateOnly(trimmed)?.let { return it }

        // Fall through to dateTime formats (extract date part)
        parseDateTime(trimmed)?.let { return it }

        // Last resort: try extracting yyyy-MM-dd prefix
        if (trimmed.length >= 10) {
            parseDateOnly(trimmed.substring(0, 10))?.let { return it }
        }

        return null
    }

    /**
     * Parse a datetime string in any supported ISO8601 variant.
     * Returns epoch millis or null.
     */
    fun parseApiDateTime(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val trimmed = value.trim()

        parseDateTime(trimmed)?.let { return it }

        // Fallback: try date-only (set time to midnight UTC)
        parseDateOnly(trimmed)?.let { return it }

        return null
    }

    /**
     * Combine a date string and optional time string into a single epoch millis.
     * If time is null/blank, returns the date at midnight.
     */
    fun combineDateAndTime(dateStr: String?, timeStr: String?): Long? {
        val dateMillis = parseApiDate(dateStr) ?: return null
        if (timeStr.isNullOrBlank()) return dateMillis

        val timeParts = parseTimeComponents(timeStr.trim()) ?: return dateMillis
        val cal = Calendar.getInstance(utc).apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, timeParts.first)
            set(Calendar.MINUTE, timeParts.second)
            set(Calendar.SECOND, timeParts.third)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Format epoch millis to a display date string (e.g. "Mar 28, 2026").
     */
    fun formatDateOnly(epochMillis: Long): String {
        val sdf = SimpleDateFormat(displayDateFormat, Locale.getDefault())
        sdf.timeZone = utc
        return sdf.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to a display datetime string (e.g. "Mar 28, 2026 2:30 PM").
     */
    fun formatDateTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat(displayDateTimeFormat, Locale.getDefault())
        sdf.timeZone = utc
        return sdf.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to the **wire** datetime format used by the backend:
     * `yyyy-MM-dd HH:mm:ss` in UTC. iOS parity: `CreateTripRequest.departureDate`.
     */
    fun formatApiDateTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = utc
        return sdf.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to a short date pill (e.g. "5/12/26"). iOS parity:
     * `TripCreationView` date pill in the Pickup/Delivery picker.
     */
    fun formatShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("M/d/yy", Locale.getDefault())
        sdf.timeZone = utc
        return sdf.format(Date(epochMillis))
    }

    /**
     * Format epoch millis to a short time pill (e.g. "7:09 AM"). iOS parity:
     * `TripCreationView` time pill.
     */
    fun formatShortTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.timeZone = utc
        return sdf.format(Date(epochMillis))
    }

    /**
     * Parse a time-only string (e.g. "14:30", "2:30 PM", "14:30:00.123456").
     * Returns epoch millis on epoch date (1970-01-01) or null.
     */
    fun parseTimeString(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val trimmed = value.trim()

        for (format in timeFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                sdf.timeZone = utc
                return sdf.parse(trimmed)?.time
            } catch (_: ParseException) {
                // try next format
            }
        }

        // Manual fallback: split by colon
        val parts = trimmed.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].trim().toIntOrNull() ?: return null
            val minute = parts[1].trim().substringBefore(".").toIntOrNull() ?: return null
            val second = if (parts.size >= 3) {
                parts[2].trim().substringBefore(".").toIntOrNull() ?: 0
            } else 0

            if (hour in 0..23 && minute in 0..59 && second in 0..59) {
                val cal = Calendar.getInstance(utc).apply {
                    set(1970, 0, 1, hour, minute, second)
                    set(Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            }
        }

        return null
    }

    // -- internal helpers --

    private fun parseDateOnly(value: String): Long? {
        return try {
            val sdf = SimpleDateFormat(dateOnlyFormat, Locale.US)
            sdf.isLenient = false
            sdf.timeZone = utc
            sdf.parse(value)?.time
        } catch (_: ParseException) {
            null
        }
    }

    private fun parseDateTime(value: String): Long? {
        for (format in dateTimeFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                sdf.timeZone = utc
                return sdf.parse(value)?.time
            } catch (_: ParseException) {
                // try next format
            }
        }
        return null
    }

    /**
     * Returns (hour, minute, second) triple from a time string, or null.
     */
    private fun parseTimeComponents(value: String): Triple<Int, Int, Int>? {
        // Try each time format via SimpleDateFormat
        for (format in timeFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                sdf.timeZone = utc
                val date = sdf.parse(value) ?: continue
                val cal = Calendar.getInstance(utc).apply { time = date }
                return Triple(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND),
                )
            } catch (_: ParseException) {
                // try next
            }
        }

        // Manual colon split fallback
        val parts = value.split(":")
        if (parts.size >= 2) {
            val h = parts[0].trim().toIntOrNull() ?: return null
            val m = parts[1].trim().substringBefore(".").toIntOrNull() ?: return null
            val s = if (parts.size >= 3) {
                parts[2].trim().substringBefore(".").toIntOrNull() ?: 0
            } else 0
            if (h in 0..23 && m in 0..59 && s in 0..59) return Triple(h, m, s)
        }

        return null
    }
}
