package com.efthemiosprime.pasabayan.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.network.BuildConfig as NetworkBuildConfig

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as ComponentActivity

    AuthScreen(
        state = state,
        apiBaseUrl = NetworkBuildConfig.API_BASE_URL,
        onSignInWithGoogle = onLaunchGoogleSignIn,
        onSignInWithFacebook = { viewModel.signInWithFacebook(activity) },
        onLogout = { viewModel.logout() },
        modifier = modifier,
    )
}

@Composable
fun AuthScreen(
    state: AuthScreenState,
    apiBaseUrl: String,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.auth_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(PasabayanSpacing.sectionSpacing))

        when (val s = state.session) {
            SessionUiState.Checking -> {
                CircularProgressIndicator()
            }
            SessionUiState.SignedOut -> {
                SignedOutContent(
                    state = state,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onSignInWithFacebook = onSignInWithFacebook,
                )
            }
            is SessionUiState.SignedIn -> {
                Text(
                    text = stringResource(R.string.auth_signed_in_as, s.user.name),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
                OutlinedButton(
                    onClick = onLogout,
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.auth_sign_out))
                }
            }
        }

        if (state.isBusy && state.session !is SessionUiState.Checking) {
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(PasabayanSpacing.xxl))
        Text(
            text = stringResource(R.string.auth_api_footer, apiBaseUrl),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SignedOutContent(
    state: AuthScreenState,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
) {
    state.transientError?.let { err ->
        Text(
            text = err,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = PasabayanSpacing.lg),
        )
    }

    Button(
        onClick = onSignInWithGoogle,
        enabled = !state.isBusy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.auth_sign_in_google))
    }
    Spacer(modifier = Modifier.height(PasabayanSpacing.itemSpacing))
    Button(
        onClick = onSignInWithFacebook,
        enabled = !state.isBusy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.auth_sign_in_facebook))
    }
}
