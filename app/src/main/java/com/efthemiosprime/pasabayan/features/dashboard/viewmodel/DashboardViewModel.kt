package com.efthemiosprime.pasabayan.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.model.DashboardSheetRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DashboardUiState(
    val currentRole: UserRole = UserRole.SHIPPER,
    val selectedTabIndex: Int = 0,
    val selectedTripId: Int? = null,
    val selectedPackageId: Int? = null,
    val activeSheetRoute: DashboardSheetRoute? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Whether [initializeRole] has run for this VM instance. Prevents the role from
     * being reset back to the user's default every time `MainTabScreen` re-enters
     * composition (e.g. after `PExpandableCardHost` dismisses an expanded card),
     * which would overwrite any subsequent [switchRole] selection.
     */
    private var roleInitialized: Boolean = false

    /**
     * Initialize the role based on the signed-in user's active roles. **Idempotent** —
     * runs once per VM instance; subsequent calls are no-ops so user-driven role
     * switches survive composition cycles.
     *
     * Prefer carrier if active, otherwise shipper.
     */
    fun initializeRole(user: AuthUser) {
        if (roleInitialized) return
        val role = when {
            user.isActiveCarrier -> UserRole.CARRIER
            user.isActiveShipper -> UserRole.SHIPPER
            else -> UserRole.SHIPPER
        }
        _uiState.update { it.copy(currentRole = role) }
        roleInitialized = true
    }

    fun switchRole() {
        _uiState.update { state ->
            val newRole = when (state.currentRole) {
                UserRole.CARRIER -> UserRole.SHIPPER
                UserRole.SHIPPER -> UserRole.CARRIER
            }
            state.copy(currentRole = newRole, selectedTabIndex = 0)
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun selectTrip(tripId: Int) {
        _uiState.update { it.copy(selectedTripId = tripId, selectedPackageId = null) }
    }

    fun selectPackage(packageId: Int) {
        _uiState.update { it.copy(selectedPackageId = packageId, selectedTripId = null) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTripId = null, selectedPackageId = null) }
    }

    fun openTripFilterSheet() {
        _uiState.update { it.copy(activeSheetRoute = DashboardSheetRoute.TripFilter) }
    }

    fun openCreateTripFromPackageSheet(packageId: Int) {
        _uiState.update {
            it.copy(activeSheetRoute = DashboardSheetRoute.CreateTripFromPackage(packageId = packageId))
        }
    }

    fun openEditTripSheet(tripId: Int) {
        _uiState.update {
            it.copy(activeSheetRoute = DashboardSheetRoute.EditTrip(tripId = tripId))
        }
    }

    fun openPackageDetailSheet(packageId: Int) {
        _uiState.update {
            it.copy(activeSheetRoute = DashboardSheetRoute.PackageDetail(packageId = packageId))
        }
    }

    fun openCarrierPackageDetailSheet(packageId: Int) {
        _uiState.update {
            it.copy(activeSheetRoute = DashboardSheetRoute.CarrierPackageDetail(packageId = packageId))
        }
    }

    fun openEditPackageSheet(packageId: Int) {
        _uiState.update {
            it.copy(activeSheetRoute = DashboardSheetRoute.EditPackage(packageId = packageId))
        }
    }

    fun dismissActiveSheetRoute() {
        _uiState.update { it.copy(activeSheetRoute = null) }
    }
}
