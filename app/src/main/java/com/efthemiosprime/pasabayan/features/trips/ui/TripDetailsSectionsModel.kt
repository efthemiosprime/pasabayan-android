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
 * Valid next statuses a carrier can move the trip to from [current]. Mirrors iOS
 * `TripStatusUpdateSheet.statusPickerOptions` (Swift :228) — `CANCELLED` is now exposed from
 * `PLANNING` and `ACTIVE` so the sheet can drive cancellation alongside other transitions; the
 * destructive flow is gated by a confirmation alert in the sheet. Later forward states only
 * progress linearly; terminal states stay empty.
 */
internal fun nextStatusOptions(current: TripStatus): List<TripStatus> = when (current) {
    TripStatus.PLANNING -> listOf(TripStatus.ACTIVE, TripStatus.CANCELLED)
    TripStatus.ACTIVE -> listOf(TripStatus.IN_TRANSIT, TripStatus.CANCELLED)
    TripStatus.IN_TRANSIT -> listOf(TripStatus.COMPLETED)
    TripStatus.COMPLETED, TripStatus.CANCELLED -> emptyList()
}

/**
 * Verification state of a pickup / delivery code on a match. Mirrors iOS `CodeState` in
 * `TripDetailsView.swift`.
 */
internal sealed interface CodeState {
    data object Requested : CodeState
    data class Verified(val dateText: String?) : CodeState
}

/**
 * Pickup-code state for a match, derived in the same priority order as iOS:
 *  1. Non-blank `pickedUpAt` → [CodeState.Verified] with that date text.
 *  2. Match status `PICKED_UP` / `IN_TRANSIT` / `DELIVERED` → [CodeState.Verified] without
 *     a date (server hasn't surfaced the timestamp yet, but the status implies verification).
 *  3. Otherwise → [CodeState.Requested].
 */
internal fun pickupCodeState(
    match: TripMatchPackage,
    formatDate: (String) -> String? = ::defaultFormatDate,
): CodeState {
    match.pickedUpAt?.takeIf { it.isNotBlank() }?.let {
        return CodeState.Verified(dateText = formatDate(it))
    }
    return when (match.matchStatus) {
        MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT, MatchStatus.DELIVERED ->
            CodeState.Verified(dateText = null)
        else -> CodeState.Requested
    }
}

/**
 * Delivery-code state — same shape as [pickupCodeState] but driven by `deliveredAt`:
 *  1. Non-blank `deliveredAt` → [CodeState.Verified] with that date text.
 *  2. Match status `DELIVERED` → [CodeState.Verified] without a date.
 *  3. Otherwise → [CodeState.Requested].
 */
internal fun deliveryCodeState(
    match: TripMatchPackage,
    formatDate: (String) -> String? = ::defaultFormatDate,
): CodeState {
    match.deliveredAt?.takeIf { it.isNotBlank() }?.let {
        return CodeState.Verified(dateText = formatDate(it))
    }
    return when (match.matchStatus) {
        MatchStatus.DELIVERED -> CodeState.Verified(dateText = null)
        else -> CodeState.Requested
    }
}

private fun defaultFormatDate(iso: String): String? =
    com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing.parseApiDateTime(iso)
        ?.let { com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing.formatDateOnly(it) }
