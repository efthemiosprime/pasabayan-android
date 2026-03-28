package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarrierTripsUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusFilter: TripStatus? = null,
) {
    /** Filtered trips: when filter is null, hides cancelled by default. */
    val filteredTrips: List<Trip>
        get() = when (statusFilter) {
            null -> trips.filter { it.tripStatus != TripStatus.CANCELLED }
            else -> trips.filter { it.tripStatus == statusFilter }
        }

    val statusCounts: Map<TripStatus, Int>
        get() = trips.groupBy { it.tripStatus }.mapValues { it.value.size }
}

@HiltViewModel
class CarrierTripsViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarrierTripsUiState())
    val uiState: StateFlow<CarrierTripsUiState> = _uiState.asStateFlow()

    fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            tripsRepository.loadCarrierTrips().fold(
                onSuccess = { trips ->
                    _uiState.update { it.copy(trips = trips, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load trips")
                    }
                },
            )
        }
    }

    fun refreshTrips() = loadTrips()

    fun setStatusFilter(status: TripStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun deleteTrip(tripId: Int) {
        viewModelScope.launch {
            tripsRepository.deleteTrip(tripId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(trips = state.trips.filter { it.id != tripId })
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to delete trip")
                    }
                },
            )
        }
    }
}
