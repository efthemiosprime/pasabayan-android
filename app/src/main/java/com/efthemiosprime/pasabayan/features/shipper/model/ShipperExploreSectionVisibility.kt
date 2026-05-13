package com.efthemiosprime.pasabayan.features.shipper.model

/**
 * Pure visibility logic for the shipper-explore search-context sections — Top Carriers
 * vs Recent Searches. iOS parity: `ShipperExploreSectionVisibility` in
 * `ShipperExplorePureHelpers.swift`.
 *
 * Both sections only surface when the user is in the "default browsing" state
 * (no search text, no popular destination selected). Top Carriers takes precedence
 * — Recent Searches is the fallback when there are no top carriers but the user
 * has saved searches.
 */
object ShipperExploreSectionVisibility {

    fun shouldShowTopCarriers(
        searchText: String,
        selectedPopularDestination: String?,
        carriersWithCompletedTripsCount: Int,
    ): Boolean =
        searchText.trim().isEmpty() &&
            selectedPopularDestination.isNullOrEmpty() &&
            carriersWithCompletedTripsCount > 0

    fun shouldShowRecentSearches(
        searchText: String,
        selectedPopularDestination: String?,
        carriersWithCompletedTripsCount: Int,
        recentSearchesCount: Int,
    ): Boolean =
        searchText.trim().isEmpty() &&
            selectedPopularDestination.isNullOrEmpty() &&
            carriersWithCompletedTripsCount == 0 &&
            recentSearchesCount > 0
}
