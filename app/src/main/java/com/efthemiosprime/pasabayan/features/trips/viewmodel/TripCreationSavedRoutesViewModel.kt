package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplatesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TripCreationSavedRoutesUiState(
    val savedRoutes: List<SavedRouteTemplate> = emptyList(),
)

@HiltViewModel
class TripCreationSavedRoutesViewModel @Inject constructor(
    private val savedRouteTemplatesStore: SavedRouteTemplatesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripCreationSavedRoutesUiState())
    val uiState: StateFlow<TripCreationSavedRoutesUiState> = _uiState.asStateFlow()

    init {
        refreshSavedRoutes()
    }

    fun refreshSavedRoutes() {
        _uiState.update {
            it.copy(savedRoutes = savedRouteTemplatesStore.getAll())
        }
    }
}
