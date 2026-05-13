package com.efthemiosprime.pasabayan.features.trips.model

data class RouteActivitySummary(
    val totalTrips: Int,
    val activeTrips: Int,
    val completedTrips: Int,
    val totalEarnings: Double?,
    val currency: String?,
    /**
     * Count of new packages posted near the carrier's home in the last week. Drives
     * the "Recent" section visibility in the carrier-explore search dropdown
     * ([CarrierExploreDropdownState]). Defaults to 0 when the API didn't return
     * the legacy `carrier.new_packages_this_week_near_home` block.
     */
    val newPackagesThisWeekNearHome: Int = 0,
)
