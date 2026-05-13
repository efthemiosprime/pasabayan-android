package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.services.CreateTripFromPackageUseCase
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTripFromPackageUiState(
    val template: TripTemplateData? = null,
    val isLoadingTemplate: Boolean = false,
    val isSavingTrip: Boolean = false,
    val createdTrip: Trip? = null,
    val errorMessage: String? = null,
    /** One-shot: action blocked because the user's phone is not verified. */
    val requiresPhoneVerification: VerifyPhoneReason? = null,
    /**
     * Server-supplied message indicating the carrier already has a compatible trip and should
     * use the request-to-carry flow instead. iOS parity:
     * `CreateTripFromPackageViewModel.shouldShowRequestToCarryAlert` /
     * `CreateTripFromPackageView.swift:86-92`. Non-null → show the alert.
     */
    val requestToCarryMessage: String? = null,
)

@HiltViewModel
class CreateTripFromPackageViewModel @Inject constructor(
    private val useCase: CreateTripFromPackageUseCase,
    private val requirePhoneVerification: RequirePhoneVerificationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateTripFromPackageUiState())
    val uiState: StateFlow<CreateTripFromPackageUiState> = _uiState.asStateFlow()

    fun loadTemplate(packageId: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingTemplate = true, errorMessage = null, requestToCarryMessage = null)
            }
            useCase.loadTemplate(packageId).fold(
                onSuccess = { template ->
                    _uiState.update { it.copy(template = template, isLoadingTemplate = false) }
                },
                onFailure = { error ->
                    val rawMessage = error.message
                    if (rawMessage != null && isRequestToCarryHint(rawMessage)) {
                        _uiState.update {
                            it.copy(
                                isLoadingTemplate = false,
                                requestToCarryMessage = rawMessage,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingTemplate = false,
                                errorMessage = rawMessage,
                            )
                        }
                    }
                },
            )
        }
    }

    fun createTrip(request: CreateTripFromPackageRequest, userId: Long? = null) {
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.CreateTrip) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingTrip = true, errorMessage = null, createdTrip = null) }
            useCase.createTrip(request, userId = userId).fold(
                onSuccess = { trip ->
                    _uiState.update { it.copy(isSavingTrip = false, createdTrip = trip) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSavingTrip = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    /**
     * Clears the one-shot `createdTrip` trigger after the host navigates away. Does NOT reset
     * any of the form fields (those live in the screen's Compose `remember` state). iOS parity:
     * `CreateTripFromPackageView` likewise consumes only the success signal here.
     */
    fun clearCreatedTrip() {
        _uiState.update { it.copy(createdTrip = null) }
    }

    fun consumeRequiresPhoneVerification() {
        _uiState.update { it.copy(requiresPhoneVerification = null) }
    }

    /** Clears the request-to-carry alert after the user resolves it (Request to carry / Cancel). */
    fun consumeRequestToCarryPrompt() {
        _uiState.update { it.copy(requestToCarryMessage = null) }
    }

    private companion object {
        /**
         * iOS parity (`CreateTripFromPackageViewModel.swift:174-191`). Returns true when the
         * server's localized error message hints that the carrier already has a compatible trip
         * and should request-to-carry instead of creating a new one.
         */
        fun isRequestToCarryHint(message: String): Boolean {
            val lower = message.lowercase()
            return lower.contains("request to carry") ||
                lower.contains("compatible trip") ||
                lower.contains("has compatible trips") ||
                (lower.contains("already have") && lower.contains("trip"))
        }
    }
}
