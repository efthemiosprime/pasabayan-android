package com.efthemiosprime.pasabayan.features.dashboard.model

sealed interface DashboardSheetRoute {
    data object TripFilter : DashboardSheetRoute

    data class CreateTripFromPackage(val packageId: Int) : DashboardSheetRoute

    data class EditTrip(val tripId: Int) : DashboardSheetRoute

    data class PackageDetail(val packageId: Int) : DashboardSheetRoute

    data class EditPackage(val packageId: Int) : DashboardSheetRoute
}
