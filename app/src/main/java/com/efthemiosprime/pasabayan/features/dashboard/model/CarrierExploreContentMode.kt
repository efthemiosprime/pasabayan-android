package com.efthemiosprime.pasabayan.features.dashboard.model

/**
 * What the carrier-explore screen is currently showing in its main content area.
 * iOS parity: `CarrierExploreContentMode` in `CarrierHomeContent.swift`.
 *
 * The dropdown's "Recent" / "Popular" taps switch the mode here; tapping a popular
 * route narrows further to [PopularDestination]. Search submit moves to [Search].
 * `Back to Nearby` returns to [Nearby] from any other state.
 */
sealed class CarrierExploreContentMode {
    data object Nearby : CarrierExploreContentMode()
    data object Recent : CarrierExploreContentMode()
    data object PopularList : CarrierExploreContentMode()
    data class PopularDestination(val displayName: String) : CarrierExploreContentMode()
    data class Search(val query: String) : CarrierExploreContentMode()

    /**
     * The "browse" mode that drives the underlying `/packages/available` fetch.
     * iOS parity: `browseModeFromExploreMode` in `CarrierHomeContent.swift` —
     * `.popularList` collapses back to `.nearby` for the API call (the popular
     * route picker doesn't itself filter packages; only [PopularDestination] does).
     */
    val browseMode: CarrierBrowseContentMode
        get() = when (this) {
            Nearby -> CarrierBrowseContentMode.Nearby
            Recent -> CarrierBrowseContentMode.Recent
            PopularList -> CarrierBrowseContentMode.Nearby
            is PopularDestination -> CarrierBrowseContentMode.Destination(displayName)
            is Search -> CarrierBrowseContentMode.Search(query)
        }
}

/**
 * Mode the package list itself uses — maps to API params. iOS parity:
 * `CarrierBrowseContentMode` in `CarrierHomeContent.swift`.
 */
sealed class CarrierBrowseContentMode {
    data object Nearby : CarrierBrowseContentMode()
    data object Recent : CarrierBrowseContentMode()
    data class Destination(val displayName: String) : CarrierBrowseContentMode()
    data class Search(val query: String) : CarrierBrowseContentMode()
}
