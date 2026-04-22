package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripPackageProgressMetrics
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TripPackageProgressState {
    data object Loading : TripPackageProgressState
    data object Empty : TripPackageProgressState
    data class Loaded(val metrics: TripPackageProgressMetrics) : TripPackageProgressState
    data class Error(val message: String) : TripPackageProgressState
}

@HiltViewModel
class TripPackageProgressViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<TripPackageProgressState>(TripPackageProgressState.Loading)
    val state: StateFlow<TripPackageProgressState> = _state.asStateFlow()

    fun load(tripId: Int, arrivalDateText: String) {
        viewModelScope.launch {
            _state.value = TripPackageProgressState.Loading
            tripsRepository.loadTripMatches(tripId).fold(
                onSuccess = { matches ->
                    if (matches.isEmpty()) {
                        _state.value = TripPackageProgressState.Empty
                        return@fold
                    }
                    val deliveredCount = matches.count { it.matchStatus == MatchStatus.DELIVERED }
                    val pickupCount = matches.count {
                        it.matchStatus in setOf(
                            MatchStatus.CONFIRMED,
                            MatchStatus.PICKED_UP,
                            MatchStatus.IN_TRANSIT,
                        )
                    }
                    val metrics = TripPackageProgressMetrics(
                        totalMatches = matches.size,
                        deliveredMatches = deliveredCount,
                        activeMatches = pickupCount,
                        arrivalDateText = arrivalDateText,
                    )
                    _state.update { TripPackageProgressState.Loaded(metrics) }
                },
                onFailure = { error ->
                    _state.value = TripPackageProgressState.Error(
                        message = error.message.orEmpty(),
                    )
                },
            )
        }
    }
}
