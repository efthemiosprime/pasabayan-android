package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteActivitySummaryUiState(
    val summary: RouteActivitySummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RouteActivitySummaryViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RouteActivitySummaryUiState())
    val uiState: StateFlow<RouteActivitySummaryUiState> = _uiState.asStateFlow()

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            tripsRepository.loadRouteActivitySummary().fold(
                onSuccess = { summary ->
                    _uiState.update { it.copy(summary = summary, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            summary = RouteActivitySummary(0, 0, 0, null, null),
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }
}
