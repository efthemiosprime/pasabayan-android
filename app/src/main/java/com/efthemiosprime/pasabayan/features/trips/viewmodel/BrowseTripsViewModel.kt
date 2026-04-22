package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
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
    val popularRoutes: List<PopularRoute> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingPopularRoutes: Boolean = false,
    val errorMessage: String? = null,
    val popularRoutesErrorMessage: String? = null,
    val hasLoadedTrips: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val filter: TripFilter = TripFilter(),
)

@HiltViewModel
class BrowseTripsViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseTripsUiState())
    val uiState: StateFlow<BrowseTripsUiState> = _uiState.asStateFlow()

    fun loadAvailableTrips(reset: Boolean = true) {
        viewModelScope.launch {
            val current = _uiState.value
            val requestFilter = if (reset) current.filter.copy(page = 1) else current.filter
            _uiState.update {
                it.copy(
                    filter = requestFilter,
                    isLoading = reset,
                    isLoadingMore = !reset,
                    errorMessage = null,
                )
            }
            tripsRepository.loadAvailableTrips(requestFilter).fold(
                onSuccess = { trips ->
                    // Client-side filter: remove non-bookable statuses
                    val filtered = trips.filter { it.tripStatus in BOOKABLE_STATUSES }
                    _uiState.update {
                        val merged = if (reset) {
                            filtered
                        } else {
                            (it.availableTrips + filtered).distinctBy { trip -> trip.id }
                        }
                        it.copy(
                            availableTrips = merged,
                            isLoading = false,
                            isLoadingMore = false,
                            hasLoadedTrips = true,
                            currentPage = requestFilter.page,
                            hasMore = filtered.isNotEmpty(),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            hasLoadedTrips = true,
                            errorMessage = e.message ?: "Failed to load trips",
                        )
                    }
                },
            )
        }
    }

    fun refreshTrips() = loadAvailableTrips(reset = true)

    fun loadMoreTrips() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        _uiState.update {
            it.copy(
                filter = it.filter.copy(page = it.currentPage + 1),
            )
        }
        loadAvailableTrips(reset = false)
    }

    fun loadPopularRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPopularRoutes = true, popularRoutesErrorMessage = null) }
            tripsRepository.loadPopularPackageRoutes().fold(
                onSuccess = { routes ->
                    _uiState.update {
                        it.copy(
                            popularRoutes = routes,
                            isLoadingPopularRoutes = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingPopularRoutes = false,
                            popularRoutesErrorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchText = text)) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filter = TripFilter(),
                availableTrips = emptyList(),
                currentPage = 1,
                hasMore = true,
            )
        }
    }

    fun applyFilterAndFetch() {
        _uiState.update { it.copy(filter = it.filter.copy(page = 1)) }
        loadAvailableTrips(reset = true)
    }
}
