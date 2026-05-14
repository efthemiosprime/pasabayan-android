package com.efthemiosprime.pasabayan.shared.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.features.auth.ui.AuthRoute
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.features.auth.viewmodel.SessionUiState
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.onboarding.ui.OnboardingRoute

/**
 * Cold-start root: onboarding (first launch) → auth/session UI. Matches iOS `PasabayanApp` ordering.
 *
 * Splash unification: `OnboardingBootstrapUi.Loading` (DataStore read) and
 * `SessionUiState.Checking` (token validation + /auth/me round-trip) both
 * render the same neutral splash so users with a valid session see a single
 * smooth spinner → MainTabScreen, instead of the two consecutive spinners
 * the previous wiring produced.
 */
@Composable
fun AppEntryContent(
    rootViewModel: RootViewModel,
    authViewModel: AuthViewModel,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bootstrap by rootViewModel.onboardingBootstrap.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    when (bootstrap) {
        OnboardingBootstrapUi.Loading -> Splash(modifier)
        OnboardingBootstrapUi.ShowOnboarding -> {
            OnboardingRoute(
                onCompleteOnboarding = { role ->
                    rootViewModel.completeOnboardingWithPreferredRole(role)
                },
                modifier = modifier,
            )
        }
        OnboardingBootstrapUi.ShowMain -> when (authState.session) {
            SessionUiState.Checking -> Splash(modifier)
            else -> AuthRoute(
                viewModel = authViewModel,
                onLaunchGoogleSignIn = onLaunchGoogleSignIn,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun Splash(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        PCircularProgress()
    }
}
