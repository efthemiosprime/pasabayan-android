package com.efthemiosprime.pasabayan.shared.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Drives cold-start flow: onboarding shell → main (auth / dashboard). Parity with iOS `PasabayanApp`. */
sealed interface OnboardingBootstrapUi {
    data object Loading : OnboardingBootstrapUi
    data object ShowOnboarding : OnboardingBootstrapUi
    data object ShowMain : OnboardingBootstrapUi
}

@HiltViewModel
class RootViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {

    private val _onboardingBootstrap = MutableStateFlow<OnboardingBootstrapUi>(OnboardingBootstrapUi.Loading)
    val onboardingBootstrap: StateFlow<OnboardingBootstrapUi> = _onboardingBootstrap.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = onboardingPreferences.hasCompletedOnboarding()
            _onboardingBootstrap.value =
                if (completed) OnboardingBootstrapUi.ShowMain else OnboardingBootstrapUi.ShowOnboarding
        }
    }

    /**
     * Marks onboarding finished and persists preferred role (iOS `OnboardingScreen.handleComplete` +
     * `RoleViewModel.saveCurrentRoleNow`).
     */
    fun completeOnboardingWithPreferredRole(role: OnboardingRole) {
        viewModelScope.launch {
            onboardingPreferences.setPreferredRoleWire(role.wireValue)
            onboardingPreferences.setHasCompletedOnboarding(true)
            _onboardingBootstrap.value = OnboardingBootstrapUi.ShowMain
        }
    }
}
