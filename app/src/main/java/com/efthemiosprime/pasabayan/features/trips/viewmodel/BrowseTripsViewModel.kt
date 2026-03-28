package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val BOOKABLE_STATUSES = setOf(TripStatus.PLANNING, TripStatus.ACTIVE)

data class BrowseTripsUiState(
    val availableTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoadedTrips: Boolean = false,
    val filter: TripFilter = TripFilter(),
)

@HiltViewModel
class BrowseTripsViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseTripsUiState())
    val uiState: StateFlow<BrowseTripsUiState> = _uiState.asStateFlow()

    fun loadAvailableTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            tripsRepository.loadAvailableTrips(_uiState.value.filter).fold(
                onSuccess = { trips ->
                    // Client-side filter: remove non-bookable statuses
                    val filtered = trips.filter { it.tripStatus in BOOKABLE_STATUSES }
                    _uiState.update {
                        it.copy(
                            availableTrips = filtered,
                            isLoading = false,
                            hasLoadedTrips = true,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedTrips = true,
                            errorMessage = e.message ?: "Failed to load trips",
                        )
                    }
                },
            )
        }
    }

    fun refreshTrips() = loadAvailableTrips()

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchText = text)) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(filter = TripFilter()) }
    }

    fun applyFilterAndFetch() {
        loadAvailableTrips()
    }
}
