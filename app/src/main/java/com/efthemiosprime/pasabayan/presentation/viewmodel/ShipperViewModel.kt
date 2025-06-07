package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.ui.screens.dashboard.state.DashboardUiState

/**
 * ShipperViewModel - Functional state management for shipper operations
 * Following functional programming patterns with immutable state
 */
class ShipperViewModel : ViewModel() {
    
    // Composition of PackageViewModel for package operations
    val packageViewModel = PackageViewModel()
    
    // Internal state for user data
    private val _user = MutableStateFlow<User?>(null)
    
    // Computed UI state following functional patterns
    private val _uiState = combine(
        _user,
        packageViewModel.myPackageRequests,
        packageViewModel.isLoading
    ) { user, packages, isLoading ->
        DashboardUiState(
            user = user,
            isLoading = isLoading,
            activePackagesCount = packages.count { it.status.name in listOf("MATCHED", "BOOKED", "IN_TRANSIT") },
            deliveredCount = packages.count { it.status.name == "DELIVERED" },
            pendingCount = packages.count { it.status.name == "PENDING" },
            totalRequests = packages.size,
            recentPackages = packages.take(3),
            recentTrips = emptyList() // Shippers don't have trips
        )
    }
    
    val uiState: StateFlow<DashboardUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
    
    /**
     * Pure function to update user
     */
    fun updateUser(user: User) {
        _user.value = user
    }
    
    /**
     * Initialize with mock user data
     */
    init {
        _user.value = User(
            id = 1,
            name = "John Shipper",
            email = "john@example.com",
            avatar = null
        )
    }
} 