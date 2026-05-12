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
            _uiState.update { it.copy(isLoadingTemplate = true, errorMessage = null) }
            useCase.loadTemplate(packageId).fold(
                onSuccess = { template ->
                    _uiState.update { it.copy(template = template, isLoadingTemplate = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingTemplate = false,
                            errorMessage = error.message,
                        )
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

    fun clearCreatedTrip() {
        _uiState.update { it.copy(createdTrip = null) }
    }

    fun consumeRequiresPhoneVerification() {
        _uiState.update { it.copy(requiresPhoneVerification = null) }
    }
}
