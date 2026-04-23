package com.efthemiosprime.pasabayan.features.dashboard.ui

internal class CreateTripFromPackageRouteActions(
    private val dismissRoute: () -> Unit,
    private val refreshCarrierTrips: () -> Unit,
    private val refreshPackages: () -> Unit,
) {
    fun onClose() {
        dismissRoute()
    }

    fun onTripCreated(@Suppress("UNUSED_PARAMETER") tripId: Int) {
        // Trip id is already consumed by the feature ViewModel; dashboard only coordinates route + refresh.
        dismissRoute()
        refreshCarrierTrips()
        refreshPackages()
    }
}
