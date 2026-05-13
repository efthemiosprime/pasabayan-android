package com.efthemiosprime.pasabayan.features.bookings.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TrackingMathTest {

    // -- distanceKm --

    @Test
    fun `distanceKm Manila to Quezon City is about 12 km`() {
        // Manila City Hall ≈ (14.5995, 120.9842), QC Hall ≈ (14.6760, 121.0437)
        val km = TrackingMath.distanceKm(14.5995, 120.9842, 14.6760, 121.0437)!!
        assertTrue("expected 10–14 km, got $km", km in 10.0..14.0)
    }

    @Test
    fun `distanceKm returns null when any coord is null`() {
        assertNull(TrackingMath.distanceKm(null, 120.9842, 14.6760, 121.0437))
        assertNull(TrackingMath.distanceKm(14.5995, null, 14.6760, 121.0437))
        assertNull(TrackingMath.distanceKm(14.5995, 120.9842, null, 121.0437))
        assertNull(TrackingMath.distanceKm(14.5995, 120.9842, 14.6760, null))
    }

    @Test
    fun `distanceKm same point is zero`() {
        val km = TrackingMath.distanceKm(14.5995, 120.9842, 14.5995, 120.9842)!!
        assertEquals(0.0, km, 0.001)
    }

    // -- etaMinutes --

    @Test
    fun `etaMinutes for 40 km at 40 kmh is 60`() {
        assertEquals(60, TrackingMath.etaMinutes(40.0))
    }

    @Test
    fun `etaMinutes for 10 km at 40 kmh is 15`() {
        assertEquals(15, TrackingMath.etaMinutes(10.0))
    }

    @Test
    fun `etaMinutes for very short distance floors to 1`() {
        assertEquals(1, TrackingMath.etaMinutes(0.1))
        assertEquals(1, TrackingMath.etaMinutes(0.0))
    }

    @Test
    fun `etaMinutes returns null when distance is null`() {
        assertNull(TrackingMath.etaMinutes(null))
    }

    // -- progress --

    @Test
    fun `progress is zero when total is zero or null`() {
        assertEquals(0.0, TrackingMath.progress(5.0, 0.0), 0.001)
        assertEquals(0.0, TrackingMath.progress(5.0, null), 0.001)
        assertEquals(0.0, TrackingMath.progress(null, 10.0), 0.001)
    }

    @Test
    fun `progress at start is zero, midpoint half, end one`() {
        assertEquals(0.0, TrackingMath.progress(10.0, 10.0), 0.001)
        assertEquals(0.5, TrackingMath.progress(5.0, 10.0), 0.001)
        assertEquals(1.0, TrackingMath.progress(0.0, 10.0), 0.001)
    }

    @Test
    fun `progress clamps to one when remaining is negative`() {
        assertEquals(1.0, TrackingMath.progress(-1.0, 10.0), 0.001)
    }

    // -- isStale --

    @Test
    fun `isStale uses server flag when present`() {
        val now = Instant.parse("2026-05-13T10:30:00Z")
        // Server says stale even though timestamp is fresh
        assertTrue(TrackingMath.isStale("2026-05-13T10:29:00Z", now, serverIsStale = true))
        // Server says fresh even though timestamp is ancient — server wins
        assertFalse(TrackingMath.isStale("2020-01-01T00:00:00Z", now, serverIsStale = false))
    }

    @Test
    fun `isStale falls through to 10-minute window when server flag is null`() {
        val now = Instant.parse("2026-05-13T10:30:00Z")
        assertFalse(TrackingMath.isStale("2026-05-13T10:25:00Z", now, serverIsStale = null)) // 5 min
        assertFalse(TrackingMath.isStale("2026-05-13T10:20:00.001Z", now, serverIsStale = null)) // ~9m59.999s
        assertTrue(TrackingMath.isStale("2026-05-13T10:19:59Z", now, serverIsStale = null)) // 10m+1s
    }

    @Test
    fun `isStale is true when timestamp is null, blank, or unparseable`() {
        val now = Instant.parse("2026-05-13T10:30:00Z")
        assertTrue(TrackingMath.isStale(null, now, serverIsStale = null))
        assertTrue(TrackingMath.isStale("  ", now, serverIsStale = null))
        assertTrue(TrackingMath.isStale("not-a-date", now, serverIsStale = null))
    }
}
