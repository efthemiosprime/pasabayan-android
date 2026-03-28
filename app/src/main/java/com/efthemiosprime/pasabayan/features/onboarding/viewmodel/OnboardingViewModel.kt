package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.efthemiosprime.pasabayan.features.onboarding.model.JourneyStepDefinitions
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingRole
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingStep
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: OnboardingPreferences,
) : ViewModel() {

    private val _step = MutableStateFlow<OnboardingStep>(OnboardingStep.RoleSelection)
    val step: StateFlow<OnboardingStep> = _step.asStateFlow()

    private val _hasViewedCarrierJourney = MutableStateFlow(false)
    val hasViewedCarrierJourney: StateFlow<Boolean> = _hasViewedCarrierJourney.asStateFlow()

    private val _hasViewedSenderJourney = MutableStateFlow(false)
    val hasViewedSenderJourney: StateFlow<Boolean> = _hasViewedSenderJourney.asStateFlow()

    val totalJourneySteps: Int = 4

    init {
        viewModelScope.launch {
            _hasViewedCarrierJourney.value = preferences.hasViewedCarrierJourney()
            _hasViewedSenderJourney.value = preferences.hasViewedSenderJourney()
        }
    }

    fun selectRole(role: OnboardingRole) {
        _step.value = OnboardingStep.Journey(role, stepIndex = 0)
    }

    fun nextStep() {
        when (val s = _step.value) {
            is OnboardingStep.Journey -> {
                val defs = JourneyStepDefinitions.stepsFor(s.role)
                if (s.stepIndex < defs.lastIndex) {
                    _step.value = OnboardingStep.Journey(s.role, s.stepIndex + 1)
                } else {
                    goToCompletion(s.role)
                }
            }
            else -> Unit
        }
    }

    fun previousStep() {
        when (val s = _step.value) {
            is OnboardingStep.Journey -> {
                if (s.stepIndex > 0) {
                    _step.value = OnboardingStep.Journey(s.role, s.stepIndex - 1)
                } else {
                    _step.value = OnboardingStep.RoleSelection
                }
            }
            else -> Unit
        }
    }

    fun skipToCompletion() {
        when (val s = _step.value) {
            is OnboardingStep.Journey -> goToCompletion(s.role)
            else -> Unit
        }
    }

    fun exploreOtherRole(completedRole: OnboardingRole) {
        val other = completedRole.opposite
        _step.value = OnboardingStep.Journey(other, stepIndex = 0)
    }

    private fun goToCompletion(role: OnboardingRole) {
        viewModelScope.launch {
            when (role) {
                OnboardingRole.Carrier -> {
                    preferences.setHasViewedCarrierJourney(true)
                    _hasViewedCarrierJourney.value = true
                }
                OnboardingRole.Shipper -> {
                    preferences.setHasViewedSenderJourney(true)
                    _hasViewedSenderJourney.value = true
                }
            }
            _step.value = OnboardingStep.Completion(role)
        }
    }
}
