package com.efthemiosprime.pasabayan.features.locations.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Coordinate pair used by matching helpers. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Catalog city candidate for distance / name matching. */
data class CandidateCity(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

/** Result of [HomeCityMatcher.match] — includes the matched city and distance for logging. */
data class HomeCityMatch(
    val city: CandidateCity,
    val distanceKm: Double,
)

/**
 * Pure home-city matcher per spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Home city detection".
 *
 * Resolution order:
 *  1. **Name match** — when [profileCityName] is non-blank and matches a candidate by name
 *     (case-insensitive). Returns that city even if it's outside the radius.
 *  2. **Distance match** — closest candidate within [maxRadiusKm]; ties broken by smaller id.
 *
 * Returns `null` when no candidate qualifies.
 */
object HomeCityMatcher {

    const val DEFAULT_MAX_RADIUS_KM: Double = 50.0
    private const val EARTH_RADIUS_KM: Double = 6371.0088

    fun match(
        gps: GeoPoint,
        candidates: List<CandidateCity>,
        profileCityName: String? = null,
        maxRadiusKm: Double = DEFAULT_MAX_RADIUS_KM,
    ): HomeCityMatch? {
        if (candidates.isEmpty()) return null

        // Pass 1 — name match (cheap, exact).
        val trimmed = profileCityName?.trim().orEmpty()
        if (trimmed.isNotEmpty()) {
            candidates.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }?.let { city ->
                return HomeCityMatch(city, distanceKm = haversineKm(gps, city))
            }
        }

        // Pass 2 — closest within radius. Stable order: distance asc, id asc.
        return candidates
            .map { city -> HomeCityMatch(city, haversineKm(gps, city)) }
            .filter { it.distanceKm <= maxRadiusKm }
            .minWithOrNull(
                compareBy<HomeCityMatch>({ it.distanceKm }, { it.city.id }),
            )
    }

    fun haversineKm(a: GeoPoint, b: CandidateCity): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val deltaLat = Math.toRadians(b.latitude - a.latitude)
        val deltaLng = Math.toRadians(b.longitude - a.longitude)

        val h = sin(deltaLat / 2.0) * sin(deltaLat / 2.0) +
            cos(lat1) * cos(lat2) * sin(deltaLng / 2.0) * sin(deltaLng / 2.0)
        val c = 2.0 * atan2(sqrt(h), sqrt(1.0 - h))
        return EARTH_RADIUS_KM * c
    }
}
