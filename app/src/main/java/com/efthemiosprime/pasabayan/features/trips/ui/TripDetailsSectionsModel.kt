package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripPackageProgressMetrics
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripPackagesFilter

internal fun filterTripMatches(
    matches: List<TripMatchPackage>,
    filter: TripPackagesFilter,
): List<TripMatchPackage> = when (filter) {
    TripPackagesFilter.ALL -> matches
    TripPackagesFilter.REMAINING -> matches.filter { it.matchStatus != MatchStatus.DELIVERED }
    TripPackagesFilter.DELIVERED -> matches.filter { it.matchStatus == MatchStatus.DELIVERED }
}

internal fun toProgressMetrics(
    matches: List<TripMatchPackage>,
    arrivalDateText: String,
): TripPackageProgressMetrics? {
    if (matches.isEmpty()) return null
    val deliveredCount = matches.count { it.matchStatus == MatchStatus.DELIVERED }
    val activeCount = matches.count {
        it.matchStatus in setOf(MatchStatus.CONFIRMED, MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT)
    }
    return TripPackageProgressMetrics(
        totalMatches = matches.size,
        deliveredMatches = deliveredCount,
        activeMatches = activeCount,
        arrivalDateText = arrivalDateText,
    )
}

/**
 * Match statuses that, per iOS `TripDetailsView.canCancelTrip` (`hasBlockingMatches`), block
 * cancellation because the carrier has already committed to or started carrying the package.
 */
internal val BLOCKING_CANCEL_STATUSES: Set<MatchStatus> = setOf(
    MatchStatus.CONFIRMED,
    MatchStatus.PICKED_UP,
    MatchStatus.IN_TRANSIT,
)

/** True when at least one match has a status that blocks trip cancellation. */
internal fun hasBlockingMatches(matches: List<TripMatchPackage>): Boolean =
    matches.any { it.matchStatus in BLOCKING_CANCEL_STATUSES }

/**
 * True when every match on the trip has been delivered. Drives the iOS
 * `shouldShowActionsSection` rule that hides Edit/Cancel once the trip is effectively done.
 * Returns `false` for empty match lists (an empty trip still allows edit/cancel).
 */
internal fun allPackagesDelivered(matches: List<TripMatchPackage>): Boolean =
    matches.isNotEmpty() && matches.all { it.matchStatus == MatchStatus.DELIVERED }

/**
 * Valid next statuses a carrier can advance the trip to from [current]. Mirrors iOS
 * `CarrierViewModel` transition rules (excludes [TripStatus.CANCELLED] — that's the cancel
 * button's responsibility). Returns an empty list for terminal states.
 */
internal fun nextStatusOptions(current: TripStatus): List<TripStatus> = when (current) {
    TripStatus.PLANNING -> listOf(TripStatus.ACTIVE)
    TripStatus.ACTIVE -> listOf(TripStatus.IN_TRANSIT)
    TripStatus.IN_TRANSIT -> listOf(TripStatus.COMPLETED)
    TripStatus.COMPLETED, TripStatus.CANCELLED -> emptyList()
}
