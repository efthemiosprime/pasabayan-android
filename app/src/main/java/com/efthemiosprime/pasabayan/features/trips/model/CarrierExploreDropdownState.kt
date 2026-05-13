package com.efthemiosprime.pasabayan.features.trips.model

/**
 * Pure logic for the Carrier-Explore search dropdown — decides when "Recent" /
 * "Popular" sections appear under the search field. Mirrors iOS
 * `CarrierExploreDropdownState` (`Pasabayan/Features/RouteActivity/Models/`).
 *
 * Kept as a Kotlin `object` of pure functions so the carrier-explore screen and
 * unit tests share one source of truth.
 */
object CarrierExploreDropdownState {

    /** "Recent" surfaces when the API reports ≥ this many new packages this week near home. */
    const val RECENT_THRESHOLD: Int = 5

    /** "Popular" surfaces when the popular-routes API returned ≥ this many entries. */
    const val POPULAR_THRESHOLD: Int = 1

    fun showRecentOption(newPackagesThisWeekNearHome: Int): Boolean =
        newPackagesThisWeekNearHome >= RECENT_THRESHOLD

    fun showPopularOption(popularRoutesCount: Int): Boolean =
        popularRoutesCount >= POPULAR_THRESHOLD

    /**
     * The dropdown only appears while the user is actively focused on an empty
     * search field AND at least one section qualifies. iOS parity:
     * `isSearchFocused && isSearchEmpty && (showRecentOption || showPopularOption)`.
     */
    fun shouldShowDropdown(
        isSearchFocused: Boolean,
        isSearchEmpty: Boolean,
        showRecentOption: Boolean,
        showPopularOption: Boolean,
    ): Boolean =
        isSearchFocused && isSearchEmpty && (showRecentOption || showPopularOption)
}
