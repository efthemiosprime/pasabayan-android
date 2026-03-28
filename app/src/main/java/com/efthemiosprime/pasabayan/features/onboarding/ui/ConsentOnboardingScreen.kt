package com.efthemiosprime.pasabayan.features.onboarding.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.features.onboarding.model.ConsentSelections
import com.efthemiosprime.pasabayan.features.onboarding.viewmodel.ConsentOnboardingUiState
import com.efthemiosprime.pasabayan.features.onboarding.viewmodel.ConsentOnboardingViewModel
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant

@Composable
fun ConsentOnboardingRoute(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    consentViewModel: ConsentOnboardingViewModel = hiltViewModel(),
) {
    val state by consentViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    ConsentOnboardingScreen(
        state = state,
        modifier = modifier,
        onPushChanged = consentViewModel::setPushNotifications,
        onLocationChanged = consentViewModel::setLocationTracking,
        onAnalyticsChanged = consentViewModel::setAnalytics,
        onMarketingChanged = consentViewModel::setMarketingCommunications,
        onContinue = {
            consentViewModel.saveAndContinue { wantsPush ->
                authViewModel.markConsentOnboardingComplete()
                if (wantsPush && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    ) {
                        PackageManager.PERMISSION_GRANTED -> Unit
                        else -> notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        },
    )
}

@Composable
fun ConsentOnboardingScreen(
    state: ConsentOnboardingUiState,
    onPushChanged: (Boolean) -> Unit,
    onLocationChanged: (Boolean) -> Unit,
    onAnalyticsChanged: (Boolean) -> Unit,
    onMarketingChanged: (Boolean) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(horizontal = PasabayanSpacing.screenPadding),
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PrivacyTip,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            Text(
                text = stringResource(R.string.onboarding_consent_title),
                style = PasabayanTextStyles.Heading.h3,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
            Text(
                text = stringResource(R.string.onboarding_consent_description),
                style = PasabayanTextStyles.Body.medium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            ConsentToggleRow(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.onboarding_consent_pushnotifications),
                description = stringResource(R.string.onboarding_consent_pushnotificationsdescription),
                checked = state.selections.pushNotifications,
                onCheckedChange = onPushChanged,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
            ConsentToggleRow(
                icon = Icons.Default.LocationOn,
                title = stringResource(R.string.onboarding_consent_locationtracking),
                description = stringResource(R.string.onboarding_consent_locationtrackingdescription),
                checked = state.selections.locationTracking,
                onCheckedChange = onLocationChanged,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
            ConsentToggleRow(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.onboarding_consent_analytics),
                description = stringResource(R.string.onboarding_consent_analyticsdescription),
                checked = state.selections.analytics,
                onCheckedChange = onAnalyticsChanged,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
            ConsentToggleRow(
                icon = Icons.Default.Campaign,
                title = stringResource(R.string.onboarding_consent_marketing),
                description = stringResource(R.string.onboarding_consent_marketingdescription),
                checked = state.selections.marketingCommunications,
                onCheckedChange = onMarketingChanged,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            Text(
                text = stringResource(R.string.onboarding_consent_changeanytime),
                style = PasabayanTextStyles.Caption.regular,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
        }
        PButton(
            text = stringResource(R.string.onboarding_consent_continue),
            onClick = onContinue,
            style = PButtonStyle.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PasabayanSpacing.screenPadding)
                .padding(bottom = PasabayanSpacing.xl),
            enabled = !state.isSaving,
            isLoading = state.isSaving,
        )
    }
}

@Composable
private fun ConsentToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    PCard(variant = PCardVariant.Primary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(PasabayanSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = PasabayanTextStyles.Body.medium,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = PasabayanTextStyles.Caption.regular,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.primary,
                    checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f),
                ),
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "Consent — defaults — light",
    widthDp = 360,
    heightDp = 780,
)
@Preview(
    showBackground = true,
    name = "Consent — defaults — dark",
    widthDp = 360,
    heightDp = 780,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConsentOnboardingScreenPreview() {
    PasabayanTheme {
        ConsentOnboardingScreen(
            state = ConsentOnboardingUiState(
                selections = ConsentSelections(),
                isSaving = false,
            ),
            onPushChanged = {},
            onLocationChanged = {},
            onAnalyticsChanged = {},
            onMarketingChanged = {},
            onContinue = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Consent — mixed + saving — light",
    widthDp = 360,
    heightDp = 780,
)
@Preview(
    showBackground = true,
    name = "Consent — mixed + saving — dark",
    widthDp = 360,
    heightDp = 780,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConsentOnboardingScreenMixedPreview() {
    PasabayanTheme {
        ConsentOnboardingScreen(
            state = ConsentOnboardingUiState(
                selections = ConsentSelections(
                    pushNotifications = true,
                    locationTracking = false,
                    analytics = true,
                    marketingCommunications = false,
                ),
                isSaving = true,
            ),
            onPushChanged = {},
            onLocationChanged = {},
            onAnalyticsChanged = {},
            onMarketingChanged = {},
            onContinue = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
