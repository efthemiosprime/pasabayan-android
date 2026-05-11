package com.efthemiosprime.pasabayan.features.locations.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeCityMatcherTest {

    // Canonical sample points (approximate decimal degrees)
    private val toronto = CandidateCity(id = 1, name = "Toronto", latitude = 43.65107, longitude = -79.347015)
    private val ottawa = CandidateCity(id = 2, name = "Ottawa", latitude = 45.4215, longitude = -75.6972)
    private val montreal = CandidateCity(id = 3, name = "Montreal", latitude = 45.5017, longitude = -73.5673)
    private val mississauga = CandidateCity(id = 4, name = "Mississauga", latitude = 43.5890, longitude = -79.6441)
    private val all = listOf(toronto, ottawa, montreal, mississauga)

    @Test
    fun `haversine returns expected ballpark distance between Toronto and Ottawa`() {
        val distance = HomeCityMatcher.haversineKm(
            GeoPoint(toronto.latitude, toronto.longitude),
            ottawa,
        )
        // ~351 km — allow 5 km tolerance for rounding
        assertTrue("expected ~351km, got $distance", distance in 346.0..356.0)
    }

    @Test
    fun `match returns closest city within radius`() {
        // Point near Mississauga
        val match = HomeCityMatcher.match(
            gps = GeoPoint(43.6, -79.6),
            candidates = all,
        )!!
        assertEquals(mississauga.id, match.city.id)
        assertTrue(match.distanceKm < 10.0)
    }

    @Test
    fun `match returns null when no candidate within radius`() {
        // Vancouver coordinates — far from any of the eastern candidates
        val match = HomeCityMatcher.match(
            gps = GeoPoint(49.2827, -123.1207),
            candidates = all,
        )
        assertNull(match)
    }

    @Test
    fun `match prefers name match before distance check`() {
        // GPS is right next to Toronto, but the profile already says "Ottawa".
        val match = HomeCityMatcher.match(
            gps = GeoPoint(toronto.latitude, toronto.longitude),
            candidates = all,
            profileCityName = "Ottawa",
        )!!
        assertEquals(ottawa.id, match.city.id)
        // Distance attached even though we matched by name, so callers can log.
        assertTrue(match.distanceKm > 0.0)
    }

    @Test
    fun `match name lookup is case-insensitive and trims whitespace`() {
        val match = HomeCityMatcher.match(
            gps = GeoPoint(toronto.latitude, toronto.longitude),
            candidates = all,
            profileCityName = "  montreal  ",
        )
        assertEquals(montreal.id, match!!.city.id)
    }

    @Test
    fun `match falls through to distance when profile city name unknown`() {
        val match = HomeCityMatcher.match(
            gps = GeoPoint(toronto.latitude, toronto.longitude),
            candidates = all,
            profileCityName = "Unknown",
        )
        assertEquals(toronto.id, match!!.city.id)
    }

    @Test
    fun `match respects custom radius`() {
        // 75 km radius keeps Mississauga in but Ottawa still out
        val match = HomeCityMatcher.match(
            gps = GeoPoint(toronto.latitude, toronto.longitude),
            candidates = all,
            maxRadiusKm = 75.0,
        )!!
        assertTrue(match.city.id == toronto.id || match.city.id == mississauga.id)
    }

    @Test
    fun `match ties break by lower id`() {
        // Two artificial candidates at the exact same coordinates.
        val a = CandidateCity(id = 5, name = "Twin-A", latitude = 0.0, longitude = 0.0)
        val b = CandidateCity(id = 4, name = "Twin-B", latitude = 0.0, longitude = 0.0)
        val match = HomeCityMatcher.match(
            gps = GeoPoint(0.0, 0.0),
            candidates = listOf(a, b),
        )!!
        assertEquals(4, match.city.id)
    }

    @Test
    fun `match returns null for empty candidate list`() {
        val match = HomeCityMatcher.match(GeoPoint(0.0, 0.0), emptyList())
        assertNull(match)
    }
}
