package com.efthemiosprime.pasabayan.features.trips.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TripDateTimePickerMergeTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun calOf(year: Int, month0: Int, day: Int, hour: Int = 0, minute: Int = 0): Long {
        return Calendar.getInstance(utc).apply {
            clear()
            set(year, month0, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `mergeDateWithTime preserves existing time when present`() {
        val previous = calOf(2026, Calendar.MAY, 12, hour = 7, minute = 9)
        val newDay = calOf(2026, Calendar.JUNE, 1, hour = 15, minute = 45)
        val merged = mergeDateWithTime(dayMillis = newDay, previousMillis = previous)
        assertEquals(calOf(2026, Calendar.JUNE, 1, hour = 7, minute = 9), merged)
    }

    @Test
    fun `mergeDateWithTime seeds default 9 AM when no previous time`() {
        val newDay = calOf(2026, Calendar.MAY, 12)
        val merged = mergeDateWithTime(dayMillis = newDay, previousMillis = null)
        assertEquals(calOf(2026, Calendar.MAY, 12, hour = 9, minute = 0), merged)
    }

    @Test
    fun `mergeTimeWithDate replaces only the time component`() {
        val previous = calOf(2026, Calendar.MAY, 12, hour = 7, minute = 9)
        val merged = mergeTimeWithDate(hour = 17, minute = 30, previousMillis = previous)
        assertEquals(calOf(2026, Calendar.MAY, 12, hour = 17, minute = 30), merged)
    }

    @Test
    fun `mergeTimeWithDate falls back to today when previous is null`() {
        val before = System.currentTimeMillis()
        val merged = mergeTimeWithDate(hour = 12, minute = 0, previousMillis = null)
        val after = System.currentTimeMillis()
        // Result should sit on or near today (within a day window of now in UTC).
        val cal = Calendar.getInstance(utc).apply { timeInMillis = merged }
        val nowCal = Calendar.getInstance(utc).apply { timeInMillis = (before + after) / 2 }
        assertEquals(nowCal.get(Calendar.YEAR), cal.get(Calendar.YEAR))
        assertEquals(nowCal.get(Calendar.MONTH), cal.get(Calendar.MONTH))
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
    }
}
