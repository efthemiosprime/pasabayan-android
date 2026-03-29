package com.efthemiosprime.pasabayan.features.auth.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.network.BuildConfig as NetworkBuildConfig
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableCardHost
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.ui.MainTabScreen
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthScreenState
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.features.auth.viewmodel.CitySetupPhase
import com.efthemiosprime.pasabayan.features.auth.viewmodel.ConsentSetupPhase
import com.efthemiosprime.pasabayan.features.auth.viewmodel.SessionUiState
import com.efthemiosprime.pasabayan.features.onboarding.ui.CityOnboardingRoute
import com.efthemiosprime.pasabayan.features.onboarding.ui.ConsentOnboardingRoute

/** iOS `Color(red: 24/255, green: 119/255, blue: 242/255)` — Facebook brand. */
private val FacebookBlue = Color(0xFF1877F2)

/** Google logo red — used for filled “Sign in with Google” style (high-contrast CTA). */
private val GoogleBrandRed = Color(0xFFEA4335)

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as ComponentActivity

    when (state.session) {
        is SessionUiState.SignedIn -> {
            when (state.citySetupPhase) {
                CitySetupPhase.NeedsSetup -> {
                    CityOnboardingRoute(
                        onFinished = { viewModel.markCityOnboardingComplete() },
                        modifier = modifier,
                    )
                }
                CitySetupPhase.Complete,
                null,
                -> {
                    when (state.consentSetupPhase) {
                        ConsentSetupPhase.NeedsSetup -> {
                            ConsentOnboardingRoute(
                                authViewModel = viewModel,
                                modifier = modifier,
                            )
                        }
                        ConsentSetupPhase.Complete,
                        null,
                        -> {
                            AuthScreen(
                                state = state,
                                apiBaseUrl = NetworkBuildConfig.API_BASE_URL,
                                showApiFooter = NetworkBuildConfig.DEBUG,
                                onSignInWithGoogle = onLaunchGoogleSignIn,
                                onSignInWithFacebook = { viewModel.signInWithFacebook(activity) },
                                onLogout = { viewModel.logout() },
                                onConsumeDidJustCompleteConsent = { viewModel.consumeDidJustCompleteConsent() },
                                modifier = modifier,
                            )
                        }
                    }
                }
            }
        }
        SessionUiState.Checking,
        SessionUiState.SignedOut,
        -> {
            AuthScreen(
                state = state,
                apiBaseUrl = NetworkBuildConfig.API_BASE_URL,
                showApiFooter = NetworkBuildConfig.DEBUG,
                onSignInWithGoogle = onLaunchGoogleSignIn,
                onSignInWithFacebook = { viewModel.signInWithFacebook(activity) },
                onLogout = { viewModel.logout() },
                onConsumeDidJustCompleteConsent = { viewModel.consumeDidJustCompleteConsent() },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun AuthScreen(
    state: AuthScreenState,
    apiBaseUrl: String,
    showApiFooter: Boolean,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
    onLogout: () -> Unit,
    onConsumeDidJustCompleteConsent: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (val s = state.session) {
            is SessionUiState.SignedIn -> {
                PExpandableCardHost {
                    MainTabScreen(
                        user = s.user,
                        onLogout = onLogout,
                    )
                }
            }
            SessionUiState.Checking,
            SessionUiState.SignedOut,
            -> {
                MarketingAuthScrollContent(
                    session = state.session,
                    transientError = state.transientError,
                    isBusy = state.isBusy,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onSignInWithFacebook = onSignInWithFacebook,
                    apiBaseUrl = apiBaseUrl,
                    showApiFooter = showApiFooter,
                )
            }
        }

        if (state.isBusy) {
            AuthLoadingOverlay(
                message = stringResource(R.string.auth_signing_in),
            )
        }
    }
}

@Composable
private fun MarketingAuthScrollContent(
    session: SessionUiState,
    transientError: String?,
    isBusy: Boolean,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
    apiBaseUrl: String,
    showApiFooter: Boolean,
) {
    val scroll = rememberScrollState()
    // Do not use fillMaxSize + verticalScroll on the same Column — unbounded height breaks measure (preview/runtime).
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = PasabayanSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        AuthHeroHeader()

        Spacer(modifier = Modifier.height(40.dp))

        AuthFeatureRows()

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            transientError?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            when (session) {
                SessionUiState.Checking -> {
                    if (!isBusy) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                SessionUiState.SignedOut -> {
                    GoogleSignInRow(
                        onClick = onSignInWithGoogle,
                        enabled = !isBusy,
                    )
                    FacebookSignInRow(
                        onClick = onSignInWithFacebook,
                        enabled = !isBusy,
                    )
                }
                is SessionUiState.SignedIn -> Unit
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (showApiFooter) {
            Text(
                text = stringResource(R.string.auth_api_footer, apiBaseUrl),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = PasabayanSpacing.lg),
            )
        }
    }
}

@Composable
private fun AuthPasabayanLogo(
    modifier: Modifier = Modifier,
    tint: Color,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(PasabayanRadius.sm))
                .background(tint.copy(alpha = 0.12f)),
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_pasabayan_logo),
            contentDescription = null,
            modifier = modifier,
            colorFilter = ColorFilter.tint(tint),
        )
    }
}

@Composable
private fun AuthGoogleLogoMark(
    modifier: Modifier = Modifier,
    /** When set (e.g. white on red button), tints the vector to a single color. */
    tint: Color? = null,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (tint != null) tint.copy(alpha = 0.35f) else Color(0xFFE0E0E0),
                ),
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_google_logo),
            contentDescription = null,
            modifier = modifier,
            colorFilter = tint?.let { ColorFilter.tint(it) },
        )
    }
}

@Composable
private fun AuthHeroHeader() {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AuthPasabayanLogo(
            modifier = Modifier.size(width = 96.dp, height = 110.dp),
            tint = onSurface,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = PasabayanTextStyles.Heading.h1,
                color = onSurface,
            )
            Text(
                text = stringResource(R.string.auth_tagline),
                style = PasabayanTextStyles.Body.regular,
                color = onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AuthFeatureRows() {
    val dark = isSystemInDarkTheme()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val rowBg = if (dark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthFeatureRow(
            icon = Icons.Filled.LocationOn,
            title = stringResource(R.string.auth_feature_tracking_title),
            description = stringResource(R.string.auth_feature_tracking_description),
            rowBackground = rowBg,
            contentColor = onSurface,
        )
        AuthFeatureRow(
            icon = Icons.Filled.VerifiedUser,
            title = stringResource(R.string.auth_feature_secure_title),
            description = stringResource(R.string.auth_feature_secure_description),
            rowBackground = rowBg,
            contentColor = onSurface,
        )
        AuthFeatureRow(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.auth_feature_fast_title),
            description = stringResource(R.string.auth_feature_fast_description),
            rowBackground = rowBg,
            contentColor = onSurface,
        )
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    rowBackground: Color,
    contentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PasabayanRadius.md))
            .background(rowBackground)
            .padding(PasabayanSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = PasabayanTextStyles.Heading.h5,
                color = contentColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = PasabayanTextStyles.Caption.regular,
                color = contentColor.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun GoogleSignInRow(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val fill = if (enabled) GoogleBrandRed else GoogleBrandRed.copy(alpha = 0.38f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(PasabayanRadius.sm))
            .background(fill)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(PasabayanRadius.sm))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = PasabayanSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AuthGoogleLogoMark(
            modifier = Modifier.size(20.dp),
            tint = Color.White,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.auth_sign_in_google),
            style = PasabayanTextStyles.Body.medium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun FacebookSignInRow(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val fill = if (enabled) FacebookBlue else FacebookBlue.copy(alpha = 0.38f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(PasabayanRadius.md))
            .background(fill)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(PasabayanRadius.md))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = PasabayanSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "f",
                color = FacebookBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.auth_sign_in_facebook),
            style = PasabayanTextStyles.Body.medium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AuthLoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color.White,
                strokeWidth = 3.dp,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            Text(
                text = message,
                style = PasabayanTextStyles.Body.medium,
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true, name = "Auth — signed out", heightDp = 900, widthDp = 411)
@Composable
private fun AuthScreenSignedOutPreview() {
    PasabayanTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                state = AuthScreenState(session = SessionUiState.SignedOut),
                apiBaseUrl = "https://api.pasabayan.com/api",
                showApiFooter = true,
                onSignInWithGoogle = {},
                onSignInWithFacebook = {},
                onLogout = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Auth — busy", heightDp = 900, widthDp = 411)
@Composable
private fun AuthScreenBusyPreview() {
    PasabayanTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                state = AuthScreenState(
                    session = SessionUiState.SignedOut,
                    isBusy = true,
                ),
                apiBaseUrl = "https://api.pasabayan.com/api",
                showApiFooter = false,
                onSignInWithGoogle = {},
                onSignInWithFacebook = {},
                onLogout = {},
            )
        }
    }
}
