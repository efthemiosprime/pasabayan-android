package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplatesStore
import com.efthemiosprime.pasabayan.features.trips.services.TripTutorialStore
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import com.efthemiosprime.pasabayan.features.trips.services.UsualTransportStore
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TripCreationUiState(
    val isSubmitting: Boolean = false,
    val createdTrip: Trip? = null,
    val errorMessage: String? = null,
    val shouldShowSaveRoutePrompt: Boolean = false,
    val shouldShowTutorial: Boolean = false,
    /** One-shot: action blocked because the user's phone is not verified. */
    val requiresPhoneVerification: VerifyPhoneReason? = null,
)

@HiltViewModel
class TripCreationViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
    private val usualTransportStore: UsualTransportStore,
    private val savedRouteTemplatesStore: SavedRouteTemplatesStore,
    private val tripTutorialStore: TripTutorialStore,
    private val requirePhoneVerification: RequirePhoneVerificationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripCreationUiState())
    val uiState: StateFlow<TripCreationUiState> = _uiState.asStateFlow()

    fun initializeTutorial(userId: Long) {
        _uiState.update {
            it.copy(shouldShowTutorial = !tripTutorialStore.hasSeenTutorial(userId))
        }
    }

    fun dismissTutorial(userId: Long) {
        tripTutorialStore.markTutorialSeen(userId)
        _uiState.update { it.copy(shouldShowTutorial = false) }
    }

    fun createTrip(request: CreateTripRequestJson) {
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.CreateTrip) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    createdTrip = null,
                )
            }
            tripsRepository.createTrip(request).fold(
                onSuccess = { trip ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdTrip = trip,
                            shouldShowSaveRoutePrompt = true,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun completeSuccessFlow(
        userId: Long,
        transportationMethod: TransportationMethod,
        saveRouteTemplate: SavedRouteTemplate?,
        saveRoute: Boolean,
    ) {
        if (transportationMethod != TransportationMethod.NONE) {
            usualTransportStore.set(userId.toInt(), transportationMethod)
        }
        if (saveRoute && saveRouteTemplate != null) {
            savedRouteTemplatesStore.save(saveRouteTemplate)
        }
        _uiState.update {
            it.copy(
                createdTrip = null,
                shouldShowSaveRoutePrompt = false,
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeRequiresPhoneVerification() {
        _uiState.update { it.copy(requiresPhoneVerification = null) }
    }
}
