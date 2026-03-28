package com.efthemiosprime.pasabayan.onboarding

import androidx.compose.runtime.Immutable

@Immutable
sealed class OnboardingStep {
    data object RoleSelection : OnboardingStep()

    data class Journey(
        val role: OnboardingRole,
        val stepIndex: Int,
    ) : OnboardingStep()

    data class Completion(
        val role: OnboardingRole,
    ) : OnboardingStep()
}
