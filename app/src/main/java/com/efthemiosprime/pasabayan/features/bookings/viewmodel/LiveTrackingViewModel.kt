package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTrackingUiState(
    val pickupCode: String? = null,
    val deliveryCode: String? = null,
    val isGeneratingCode: Boolean = false,
    val codeVerified: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTrackingUiState())
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    fun generatePickupCode(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, errorMessage = null) }
            bookingsRepository.generatePickupCode(matchId).fold(
                onSuccess = { code ->
                    _uiState.update { it.copy(pickupCode = code, isGeneratingCode = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isGeneratingCode = false, errorMessage = e.message ?: "Failed to generate code")
                    }
                },
            )
        }
    }

    fun generateDeliveryCode(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, errorMessage = null) }
            bookingsRepository.generateDeliveryCode(matchId).fold(
                onSuccess = { code ->
                    _uiState.update { it.copy(deliveryCode = code, isGeneratingCode = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isGeneratingCode = false, errorMessage = e.message ?: "Failed to generate code")
                    }
                },
            )
        }
    }

    fun confirmPickupWithCode(matchId: Int, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, codeVerified = false) }
            bookingsRepository.confirmPickupWithCode(matchId, code).fold(
                onSuccess = { _uiState.update { it.copy(codeVerified = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Invalid code") }
                },
            )
        }
    }

    fun confirmDeliveryWithCode(matchId: Int, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, codeVerified = false) }
            bookingsRepository.confirmDeliveryWithCode(matchId, code).fold(
                onSuccess = { _uiState.update { it.copy(codeVerified = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Invalid code") }
                },
            )
        }
    }

    fun clearState() {
        _uiState.update {
            LiveTrackingUiState()
        }
    }
}
