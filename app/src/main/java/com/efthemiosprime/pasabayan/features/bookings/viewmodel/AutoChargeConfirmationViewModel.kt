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

data class AutoChargeUiState(
    val isProcessing: Boolean = false,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AutoChargeConfirmationViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoChargeUiState())
    val uiState: StateFlow<AutoChargeUiState> = _uiState.asStateFlow()

    fun confirmAutoCharge(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            bookingsRepository.confirmMatch(matchId).fold(
                onSuccess = { _ ->
                    _uiState.update { it.copy(isProcessing = false, isConfirmed = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, errorMessage = e.message ?: "Auto-charge failed")
                    }
                },
            )
        }
    }

    fun clearState() {
        _uiState.update { AutoChargeUiState() }
    }
}
