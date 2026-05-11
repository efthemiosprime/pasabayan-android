package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchingUiState(
    val matches: List<DeliveryMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusFilter: MatchStatus? = null,
    val lastCancelRefund: RefundResult? = null,
    val lastCancelConversationId: Int? = null,
) {
    val filteredMatches: List<DeliveryMatch>
        get() = when (statusFilter) {
            null -> matches
            else -> matches.filter { it.matchStatus == statusFilter }
        }
}

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadMatches(role: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingsRepository.loadMatches(role = role).fold(
                onSuccess = { matches ->
                    _uiState.update { it.copy(matches = matches, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load matches")
                    }
                },
            )
        }
    }

    fun refreshMatches(role: String? = null) = loadMatches(role)

    fun filterByStatus(status: MatchStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun acceptMatch(matchId: Int, isCarrier: Boolean) {
        viewModelScope.launch {
            val result = if (isCarrier) {
                bookingsRepository.carrierAcceptShipperRequest(matchId)
            } else {
                bookingsRepository.shipperAccept(matchId)
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to accept") }
                },
            )
        }
    }

    fun declineMatch(matchId: Int, isCarrier: Boolean) {
        viewModelScope.launch {
            val result = if (isCarrier) {
                bookingsRepository.carrierDeclineShipperRequest(matchId)
            } else {
                bookingsRepository.shipperDecline(matchId)
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to decline") }
                },
            )
        }
    }

    fun confirmMatch(matchId: Int) {
        viewModelScope.launch {
            bookingsRepository.confirmMatch(matchId).fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to confirm") }
                },
            )
        }
    }

    fun cancelMatch(matchId: Int) {
        viewModelScope.launch {
            bookingsRepository.cancelMatch(matchId).fold(
                onSuccess = { result ->
                    _uiState.update { state ->
                        state.copy(
                            matches = state.matches.map { if (it.id == matchId) result.match else it },
                            lastCancelRefund = result.refund,
                            lastCancelConversationId = result.chatConversationId,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to cancel") }
                },
            )
        }
    }

    fun clearCancelArtifacts() {
        _uiState.update { it.copy(lastCancelRefund = null, lastCancelConversationId = null) }
    }

    fun updateMatchStatus(matchId: Int, newStatus: MatchStatus) {
        viewModelScope.launch {
            val result = when (newStatus) {
                MatchStatus.PICKED_UP -> bookingsRepository.markPickedUp(matchId)
                MatchStatus.IN_TRANSIT -> bookingsRepository.markInTransit(matchId)
                MatchStatus.DELIVERED -> bookingsRepository.markDelivered(matchId)
                else -> return@launch
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update status") }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
