package com.efthemiosprime.pasabayan.features.shipper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarrier
import com.efthemiosprime.pasabayan.features.shipper.services.ShipperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbyCarriersUiState(
    val carriers: List<NearbyCarrier> = emptyList(),
    val homeCityName: String? = null,
    val radiusKm: Double? = null,
    val isHomeCitySet: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
) {
    /**
     * iOS parity (`ShipperHomeContent.carriersWithCompletedTrips`): only
     * carriers that have completed at least one delivery are surfaced in
     * the "Top Carriers" chip row.
     */
    val carriersWithCompletedDeliveries: List<NearbyCarrier>
        get() = carriers.filter { it.completedDeliveries > 0 }
}

/**
 * Drives the "Top Carriers" horizontal-scroll section on shipper-explore.
 * Parity with iOS `NearbyCarriersViewModel.swift`.
 */
@HiltViewModel
class NearbyCarriersViewModel @Inject constructor(
    private val shipperRepository: ShipperRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyCarriersUiState())
    val uiState: StateFlow<NearbyCarriersUiState> = _uiState.asStateFlow()

    init {
        // iOS parity: `ShipperHomeContent` calls `nearbyCarriersViewModel.resetForNewSession()`
        // from `.onChange(of: roleViewModel.currentRole)`. On Android we react to the
        // session-level user transition instead — equivalent on logout, broader on user
        // swap, no-op on no change. `drop(1)` skips the StateFlow's initial replay so
        // we don't reset on first composition.
        viewModelScope.launch {
            authRepository.currentUser()
                .map { it?.id }
                .distinctUntilChanged()
                .drop(1)
                .collect { resetForNewSession() }
        }
    }

    /** Idempotent first-load entry point. Safe to call from `LaunchedEffect(Unit)`. */
    fun loadNearbyCarriersIfNeeded() {
        if (_uiState.value.hasLoaded) return
        loadNearbyCarriers()
    }

    /** Force-refresh — drops the cached `hasLoaded` flag and re-fetches. */
    fun loadNearbyCarriers() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            shipperRepository.fetchNearbyCarriers().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            carriers = result.carriers,
                            homeCityName = result.homeCityName,
                            radiusKm = result.radiusKm,
                            isHomeCitySet = result.isHomeCitySet,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            carriers = emptyList(),
                            errorMessage = e.message,
                        )
                    }
                },
            )
        }
    }

    /**
     * Call on logout / role switch so the next active session re-fetches
     * instead of showing the previous user's carriers.
     */
    fun resetForNewSession() {
        _uiState.value = NearbyCarriersUiState()
    }
}
