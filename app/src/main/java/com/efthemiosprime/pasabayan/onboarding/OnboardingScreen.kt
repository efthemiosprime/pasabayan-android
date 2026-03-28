package com.efthemiosprime.pasabayan.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme

/**
 * Full onboarding flow — parity with iOS `OnboardingScreen` + `OnboardingViewModel`.
 * Decorative “assets” use Material [androidx.compose.material.icons] as SF Symbol stand-ins.
 */
@Composable
fun OnboardingRoute(
    onCompleteOnboarding: (OnboardingRole) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val step by viewModel.step.collectAsStateWithLifecycle()
    val hasCarrier by viewModel.hasViewedCarrierJourney.collectAsStateWithLifecycle()
    val hasSender by viewModel.hasViewedSenderJourney.collectAsStateWithLifecycle()

    when (val s = step) {
        OnboardingStep.RoleSelection -> {
            OnboardingRoleSelectionScreen(
                hasViewedCarrier = hasCarrier,
                hasViewedSender = hasSender,
                onSelectCarrier = { viewModel.selectRole(OnboardingRole.Carrier) },
                onSelectSender = { viewModel.selectRole(OnboardingRole.Shipper) },
                modifier = modifier,
            )
        }
        is OnboardingStep.Journey -> {
            val defs = JourneyStepDefinitions.stepsFor(s.role)
            val def = defs[s.stepIndex]
            val isLast = s.stepIndex >= defs.lastIndex
            OnboardingJourneyScreen(
                definition = def,
                role = s.role,
                stepIndex = s.stepIndex,
                totalSteps = viewModel.totalJourneySteps,
                isLastStep = isLast,
                onBack = { viewModel.previousStep() },
                onSkip = { viewModel.skipToCompletion() },
                onNext = { viewModel.nextStep() },
                onSeeSummary = { viewModel.skipToCompletion() },
                onSkipToApp = { viewModel.skipToCompletion() },
                modifier = modifier,
            )
        }
        is OnboardingStep.Completion -> {
            val oppositeViewed = when (s.role) {
                OnboardingRole.Carrier -> hasSender
                OnboardingRole.Shipper -> hasCarrier
            }
            OnboardingCompletionScreen(
                completedRole = s.role,
                hasViewedOppositeJourney = oppositeViewed,
                hasViewedBothJourneys = hasCarrier && hasSender,
                onContinueToApp = { onCompleteOnboarding(s.role) },
                onExploreOtherRole = { viewModel.exploreOtherRole(s.role) },
                modifier = modifier,
            )
        }
    }
}

private class OnboardingRolePreviewStateProvider : PreviewParameterProvider<Pair<Boolean, Boolean>> {
    override val values = sequenceOf(
        false to false,
        false to true,
        true to true,
    )
}

@Preview(showBackground = true, name = "Role selection", widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingRoleSelectionPreview(
    @PreviewParameter(OnboardingRolePreviewStateProvider::class) flags: Pair<Boolean, Boolean>,
) {
    PasabayanTheme {
        OnboardingRoleSelectionScreen(
            hasViewedCarrier = flags.first,
            hasViewedSender = flags.second,
            onSelectCarrier = {},
            onSelectSender = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
