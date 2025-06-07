package com.efthemiosprime.pasabayan.ui.screens.dashboard.state

import androidx.compose.runtime.Immutable
import com.efthemiosprime.pasabayan.data.model.*

/**
 * Immutable Dashboard UI State following functional programming patterns
 * Phase 4: Pure state transformations with copy() methods
 */
@Immutable
data class DashboardUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activePackagesCount: Int = 0,
    val deliveredCount: Int = 0,
    val pendingCount: Int = 0,
    val totalRequests: Int = 0,
    val recentPackages: List<PackageRequest> = emptyList(),
    val recentTrips: List<Trip> = emptyList(),
    val activeTripsCount: Int = 0,
    val activeBookingsCount: Int = 0,
    val totalEarnings: String = "$0.00",
    val averageRatingText: String = "0.0"
) {
    // Pure state transformation functions
    fun updateUser(newUser: User): DashboardUiState = copy(user = newUser)
    fun updateLoading(loading: Boolean): DashboardUiState = copy(isLoading = loading)
    fun updateError(error: String?): DashboardUiState = copy(errorMessage = error)
    fun updatePackageStats(
        active: Int,
        delivered: Int,
        pending: Int,
        total: Int
    ): DashboardUiState = copy(
        activePackagesCount = active,
        deliveredCount = delivered,
        pendingCount = pending,
        totalRequests = total
    )
    fun updateCarrierStats(
        activeTrips: Int,
        activeBookings: Int,
        earnings: String,
        rating: String
    ): DashboardUiState = copy(
        activeTripsCount = activeTrips,
        activeBookingsCount = activeBookings,
        totalEarnings = earnings,
        averageRatingText = rating
    )
    fun updateRecentData(
        packages: List<PackageRequest>,
        trips: List<Trip>
    ): DashboardUiState = copy(
        recentPackages = packages,
        recentTrips = trips
    )
}

/**
 * Configuration for empty state components
 */
@Immutable
data class EmptyStateConfig(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val actionText: String? = null,
    val onAction: (() -> Unit)? = null
) 