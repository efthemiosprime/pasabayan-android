package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import com.efthemiosprime.pasabayan.features.onboarding.model.ConsentSelections
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
import com.efthemiosprime.pasabayan.features.onboarding.services.ConsentOnboardingRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConsentOnboardingUiState(
    val selections: ConsentSelections = ConsentSelections(),
    val isSaving: Boolean = false,
)

@HiltViewModel
class ConsentOnboardingViewModel @Inject constructor(
    private val repository: ConsentOnboardingRepository,
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsentOnboardingUiState())
    val uiState: StateFlow<ConsentOnboardingUiState> = _uiState.asStateFlow()

    fun setPushNotifications(enabled: Boolean) {
        _uiState.update { it.copy(selections = it.selections.copy(pushNotifications = enabled)) }
    }

    fun setLocationTracking(enabled: Boolean) {
        _uiState.update { it.copy(selections = it.selections.copy(locationTracking = enabled)) }
    }

    fun setAnalytics(enabled: Boolean) {
        _uiState.update { it.copy(selections = it.selections.copy(analytics = enabled)) }
    }

    fun setMarketingCommunications(enabled: Boolean) {
        _uiState.update { it.copy(selections = it.selections.copy(marketingCommunications = enabled)) }
    }

    /**
     * Persists consent via API; mirrors iOS (always proceeds). On **success**, updates push API opt-in flag.
     */
    fun saveAndContinue(onFinished: (requestedPushNotifications: Boolean) -> Unit) {
        viewModelScope.launch {
            val selections = _uiState.value.selections
            _uiState.update { it.copy(isSaving = true) }
            repository.updateConsentPreferences(selections).fold(
                onSuccess = {
                    onboardingPreferences.setPushNotificationsApiConsentOptIn(selections.pushNotifications)
                },
                onFailure = { },
            )
            _uiState.update { it.copy(isSaving = false) }
            onFinished(selections.pushNotifications)
        }
    }
}
