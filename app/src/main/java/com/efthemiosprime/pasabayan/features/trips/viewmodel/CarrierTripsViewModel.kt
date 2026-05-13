package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
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
    /**
     * Matches loaded per trip id for the details sheet's All / Remaining /
     * Delivered chip counts and the per-match cards. Lazily populated by
     * [CarrierTripsViewModel.loadTripMatches] when a trip details sheet
     * opens. iOS parity: `TripDetailsView.loadTripMatches()` sets
     * `tripMatches` on view appear.
     */
    val tripMatchesByTripId: Map<Int, List<TripMatchPackage>> = emptyMap(),
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
    private companion object {
        val CANCEL_BLOCKING_MATCH_STATUSES = setOf(
            MatchStatus.CONFIRMED,
            MatchStatus.PICKED_UP,
            MatchStatus.IN_TRANSIT,
        )
    }

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

    /**
     * Loads matches for [tripId] into [CarrierTripsUiState.tripMatchesByTripId]
     * so the carrier trip-details sheet can render All / Remaining / Delivered
     * counts and the per-match cards. Idempotent re-entry on failure leaves
     * any previously cached list intact. iOS parity:
     * `TripDetailsView.loadTripMatches()`.
     */
    fun loadTripMatches(tripId: Int) {
        viewModelScope.launch {
            tripsRepository.loadTripMatches(tripId).onSuccess { matches ->
                _uiState.update { state ->
                    state.copy(tripMatchesByTripId = state.tripMatchesByTripId + (tripId to matches))
                }
            }
        }
    }

    fun deleteTrip(tripId: Int) {
        val trip = _uiState.value.trips.firstOrNull { it.id == tripId }
        if (trip != null && trip.tripStatus in setOf(TripStatus.IN_TRANSIT, TripStatus.COMPLETED)) {
            _uiState.update { it.copy(errorMessage = "Failed to delete trip") }
            return
        }
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

    fun cancelTrip(tripId: Int) {
        val currentTrip = _uiState.value.trips.firstOrNull { it.id == tripId } ?: return
        if (!canTransition(currentTrip.tripStatus, TripStatus.CANCELLED)) {
            _uiState.update { it.copy(errorMessage = "Failed to cancel trip") }
            return
        }

        viewModelScope.launch {
            val matchesResult = tripsRepository.loadTripMatches(tripId)
            val hasBlockingMatches = matchesResult.getOrNull()
                ?.any { it.matchStatus in CANCEL_BLOCKING_MATCH_STATUSES } == true
            if (hasBlockingMatches) {
                _uiState.update { it.copy(errorMessage = "Failed to cancel trip") }
                return@launch
            }

            tripsRepository.deleteTrip(tripId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            trips = state.trips.map { trip ->
                                if (trip.id == tripId) trip.copy(tripStatus = TripStatus.CANCELLED) else trip
                            },
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to cancel trip")
                    }
                },
            )
        }
    }

    fun updateTripStatus(tripId: Int, targetStatus: TripStatus) {
        val currentTrip = _uiState.value.trips.firstOrNull { it.id == tripId } ?: return
        if (!canTransition(currentTrip.tripStatus, targetStatus)) {
            _uiState.update { it.copy(errorMessage = "Failed to load trips") }
            return
        }
        viewModelScope.launch {
            tripsRepository.updateTrip(
                id = tripId,
                request = TripUpdateRequestJson(tripStatus = targetStatus.name.lowercase()),
            ).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            trips = state.trips.map { existing ->
                                if (existing.id == updated.id) updated else existing
                            },
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to load trips")
                    }
                },
            )
        }
    }

    /**
     * Suspend variant of [updateTripStatus] that returns the result to the caller. Used by
     * `TripStatusUpdateSheet` to drive the in-sheet isUpdating spinner + iOS-parity 15s timeout
     * fallback (see iOS `TripUpdateTimeoutCoordinator`). On success, the local trips list is
     * mirrored just like the fire-and-forget variant.
     */
    suspend fun suspendUpdateTripStatus(
        tripId: Int,
        targetStatus: TripStatus,
    ): Result<Trip> {
        val currentTrip = _uiState.value.trips.firstOrNull { it.id == tripId }
            ?: return Result.failure(IllegalStateException("Trip not found"))
        if (!canTransition(currentTrip.tripStatus, targetStatus)) {
            return Result.failure(IllegalStateException("Invalid status transition"))
        }
        return tripsRepository.updateTrip(
            id = tripId,
            request = TripUpdateRequestJson(tripStatus = targetStatus.name.lowercase()),
        ).onSuccess { updated ->
            _uiState.update { state ->
                state.copy(
                    trips = state.trips.map { existing ->
                        if (existing.id == updated.id) updated else existing
                    },
                    errorMessage = null,
                )
            }
        }
    }

    fun updateTripDetails(
        tripId: Int,
        availableWeightKg: Double?,
        specialNotes: String?,
    ) {
        viewModelScope.launch {
            tripsRepository.updateTrip(
                id = tripId,
                request = TripUpdateRequestJson(
                    availableWeightKg = availableWeightKg,
                    specialNotes = specialNotes,
                ),
            ).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            trips = state.trips.map { existing ->
                                if (existing.id == updated.id) updated else existing
                            },
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to update trip")
                    }
                },
            )
        }
    }

    private fun canTransition(current: TripStatus, target: TripStatus): Boolean {
        if (target == TripStatus.CANCELLED) {
            return current in setOf(TripStatus.PLANNING, TripStatus.ACTIVE)
        }
        return when (current) {
            TripStatus.PLANNING -> target in setOf(TripStatus.ACTIVE, TripStatus.CANCELLED)
            TripStatus.ACTIVE -> target in setOf(TripStatus.IN_TRANSIT, TripStatus.CANCELLED)
            TripStatus.IN_TRANSIT -> target == TripStatus.COMPLETED
            TripStatus.COMPLETED, TripStatus.CANCELLED -> false
        }
    }
}
