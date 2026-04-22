package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
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
