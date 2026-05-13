package com.efthemiosprime.pasabayan.features.bookings.model

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure-Kotlin distance / ETA / staleness helpers for the live tracking sheet.
 * Parity with iOS `LiveTrackingViewModel.calculateDistance/calculateETA` —
 * `distance/1000` (CLLocation Haversine), 40 km/h average city speed, ETA in
 * whole minutes with a 1-minute floor, server's `is_stale` flag short-circuits
 * the 10-minute local window.
 *
 * No Android dependencies — testable as plain JVM.
 */
object TrackingMath {

    /** Average city speed used for ETA, km/h. Parity with iOS. */
    const val AVERAGE_SPEED_KMH: Double = 40.0

    /** Minutes after which a missing-or-empty server flag still counts as stale. */
    val LOCAL_STALE_WINDOW: Duration = Duration.ofMinutes(10)

    private const val EARTH_RADIUS_KM = 6_371.0088

    /**
     * Great-circle distance between two coordinates in kilometres, via the
     * Haversine formula. Returns `null` when any coordinate is null. The
     * result is the same shape as `CLLocation.distance(from:) / 1000` in
     * iOS; differences vs. Apple's geodesic implementation are well below the
     * 40 km/h ETA resolution.
     */
    fun distanceKm(
        lat1: Double?,
        lng1: Double?,
        lat2: Double?,
        lng2: Double?,
    ): Double? {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return null
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * ETA in whole minutes at [AVERAGE_SPEED_KMH], with a 1-minute floor.
     * Parity with iOS `Int((remainingDistance / 40.0) * 60)` clamped to `max(1, …)`.
     * Returns `null` only when the distance is null (i.e. a coordinate was
     * missing) — a zero distance still yields 1 minute, mirroring iOS.
     */
    fun etaMinutes(distanceKm: Double?): Int? {
        val km = distanceKm ?: return null
        val raw = (km / AVERAGE_SPEED_KMH * 60).toInt()
        return maxOf(1, raw)
    }

    /**
     * Delivery progress in `[0, 1]`. Returns 0 when totals haven't been
     * captured yet, mirroring iOS `1 - remaining/total` with clamp.
     */
    fun progress(remainingKm: Double?, totalKm: Double?): Double {
        val remaining = remainingKm ?: return 0.0
        val total = totalKm ?: return 0.0
        if (total <= 0.0) return 0.0
        val raw = 1.0 - (remaining / total)
        return raw.coerceIn(0.0, 1.0)
    }

    /**
     * True when the last update is older than [LOCAL_STALE_WINDOW]. The
     * server-supplied `is_stale` flag (passed as [serverIsStale]) wins when
     * present — only fall through to the timestamp comparison when the
     * server flag is null. Unparseable timestamps yield `true` so the UI
     * surfaces the warning rather than silently trusting old data.
     */
    fun isStale(
        lastUpdatedAt: String?,
        now: Instant,
        serverIsStale: Boolean?,
    ): Boolean {
        if (serverIsStale != null) return serverIsStale
        val raw = lastUpdatedAt?.takeIf { it.isNotBlank() } ?: return true
        val parsed = runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
            ?: return true
        return Duration.between(parsed, now) > LOCAL_STALE_WINDOW
    }
}
