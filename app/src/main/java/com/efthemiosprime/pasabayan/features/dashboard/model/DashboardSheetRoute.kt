package com.efthemiosprime.pasabayan.features.dashboard.model

sealed interface DashboardSheetRoute {
    data object TripFilter : DashboardSheetRoute

    data class CreateTripFromPackage(val packageId: Int) : DashboardSheetRoute

    data class EditTrip(val tripId: Int) : DashboardSheetRoute

    data class PackageDetail(val packageId: Int) : DashboardSheetRoute

    /** Carrier-side detail view for an available package (iOS `CarrierPackageDetailSheet`). */
    data class CarrierPackageDetail(val packageId: Int) : DashboardSheetRoute

    data class EditPackage(val packageId: Int) : DashboardSheetRoute

    /**
     * Shipper-side discovery: trips compatible with a specific package request.
     * iOS parity: `CompatibleTripsView` opened from PackageDetail's compatible-trips CTA.
     */
    data class CompatibleTripsForPackage(val packageId: Int) : DashboardSheetRoute
}
