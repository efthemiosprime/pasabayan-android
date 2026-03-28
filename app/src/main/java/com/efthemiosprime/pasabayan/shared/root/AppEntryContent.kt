package com.efthemiosprime.pasabayan.shared.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.features.auth.ui.AuthRoute
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.onboarding.ui.OnboardingRoute

/**
 * Cold-start root: onboarding (first launch) → auth/session UI. Matches iOS `PasabayanApp` ordering.
 */
@Composable
fun AppEntryContent(
    rootViewModel: RootViewModel,
    authViewModel: AuthViewModel,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bootstrap by rootViewModel.onboardingBootstrap.collectAsStateWithLifecycle()
    when (bootstrap) {
        OnboardingBootstrapUi.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                PCircularProgress()
            }
        }
        OnboardingBootstrapUi.ShowOnboarding -> {
            OnboardingRoute(
                onCompleteOnboarding = { role ->
                    rootViewModel.completeOnboardingWithPreferredRole(role)
                },
                modifier = modifier,
            )
        }
        OnboardingBootstrapUi.ShowMain -> {
            AuthRoute(
                viewModel = authViewModel,
                onLaunchGoogleSignIn = onLaunchGoogleSignIn,
                modifier = modifier,
            )
        }
    }
}
