package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.bookings.CarrierLocationDataJson

/**
 * Domain snapshot of a carrier's current location for a tracked match.
 * Parity with iOS `CarrierLocationResponse` (`BookingModels.swift:1028`).
 *
 *  - `carrierLat` / `carrierLng` are non-null only after the carrier has
 *    started broadcasting (server returns `current_location` then).
 *  - `isStale` is the **server's** staleness flag (true when its
 *    last-seen-at is older than the backend window). The VM may also apply
 *    a local 10-minute window via [com.efthemiosprime.pasabayan.features.bookings.model.TrackingMath].
 *  - `deliveryLat` / `deliveryLng` come from String wire fields converted
 *    via `toDoubleOrNull` in [toDomain]; an unparseable value becomes null.
 */
data class CarrierLocationSnapshot(
    val matchId: Int?,
    val carrier: UserSummary?,
    val carrierLat: Double?,
    val carrierLng: Double?,
    val lastUpdatedAt: String?,
    val isStale: Boolean?,
    val deliveryAddress: String?,
    val deliveryCity: String?,
    val deliveryLat: Double?,
    val deliveryLng: Double?,
    val matchStatus: MatchStatus?,
) {
    /** True when both carrier coordinates are present and non-zero. */
    val hasCarrierLocation: Boolean
        get() {
            val lat = carrierLat ?: return false
            val lng = carrierLng ?: return false
            return lat != 0.0 || lng != 0.0
        }

    /** True when both delivery coordinates resolved to non-zero doubles. */
    val hasDeliveryLocation: Boolean
        get() {
            val lat = deliveryLat ?: return false
            val lng = deliveryLng ?: return false
            return lat != 0.0 || lng != 0.0
        }
}

fun CarrierLocationDataJson.toDomain(): CarrierLocationSnapshot = CarrierLocationSnapshot(
    matchId = matchId,
    carrier = carrier,
    carrierLat = currentLocation?.latitude,
    carrierLng = currentLocation?.longitude,
    lastUpdatedAt = currentLocation?.lastUpdatedAt,
    isStale = currentLocation?.isStale,
    deliveryAddress = deliveryAddress?.address,
    deliveryCity = deliveryAddress?.city,
    deliveryLat = deliveryAddress?.latitude?.toDoubleOrNull(),
    deliveryLng = deliveryAddress?.longitude?.toDoubleOrNull(),
    matchStatus = matchStatus,
)
