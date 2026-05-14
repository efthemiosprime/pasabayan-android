package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.bookings.model.CarrierOnboardingPrompt
import com.efthemiosprime.pasabayan.features.payments.ui.launchCustomTab
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel

/**
 * Sheet shown to a carrier when the server returns
 * `carrier_onboarding_required: true` on a match-action call. Offers a
 * "Complete payout setup" CTA that drives `StripeConnectViewModel.startOnboarding`
 * (launches Stripe's hosted onboarding in Chrome Custom Tabs), then dismisses
 * once the carrier's status reports `isOnboarded == true` so the caller can
 * retry the original action via [CarrierOnboardingPrompt.action].
 *
 * iOS parity: `Features/Bookings/Views/Components/Shared/CarrierOnboardingRequiredSheet.swift`.
 */
@Composable
fun CarrierOnboardingRequiredSheet(
    prompt: CarrierOnboardingPrompt,
    onCompletedOnboarding: (CarrierOnboardingPrompt) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StripeConnectViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(prompt.id) { viewModel.loadStatus() }

    // Launch Stripe onboarding in Chrome Custom Tabs whenever the VM produces a URL.
    LaunchedEffect(state.showOnboarding, state.onboardingUrl) {
        val url = state.onboardingUrl
        if (state.showOnboarding && !url.isNullOrBlank()) {
            launchCustomTab(context, url)
            // Mirror iOS' `handleOnboardingReturn` — the VM force-refreshes status when
            // the user returns. We do the same so the isOnboarded watcher below fires.
            viewModel.handleOnboardingReturn()
        }
    }

    // Once onboarding completes, dismiss and hand the original action back to the host.
    LaunchedEffect(state.status?.isOnboarded) {
        if (state.status?.isOnboarded == true) {
            onCompletedOnboarding(prompt)
        }
    }

    CarrierOnboardingRequiredSheetContent(
        prompt = prompt,
        state = state,
        onStartOnboarding = viewModel::startOnboarding,
        onCancel = onDismiss,
        modifier = modifier,
    )
}

@Composable
internal fun CarrierOnboardingRequiredSheetContent(
    prompt: CarrierOnboardingPrompt,
    state: StripeConnectUiState,
    onStartOnboarding: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.bookings_carrier_onboarding_title),
                style = PasabayanTextStyles.Heading.h4,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            PButton(
                text = stringResource(R.string.bookings_carrier_onboarding_close),
                onClick = onCancel,
                style = PButtonStyle.Tertiary,
                size = PButtonSize.Small,
            )
        }

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PasabayanColors.Info.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = PasabayanColors.Info,
                modifier = Modifier.size(44.dp),
            )
        }

        Text(
            text = prompt.message.ifBlank {
                stringResource(R.string.bookings_carrier_onboarding_default_message)
            },
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        state.errorMessage?.let { error ->
            Text(
                text = error,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        if (state.isLoading && state.status == null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PCircularProgress()
                Text(
                    text = stringResource(R.string.bookings_carrier_onboarding_loading),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PButton(
            text = stringResource(R.string.bookings_carrier_onboarding_cta),
            onClick = onStartOnboarding,
            size = PButtonSize.Large,
            isLoading = state.isLoading && state.status != null,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        PButton(
            text = stringResource(R.string.bookings_carrier_onboarding_cancel),
            onClick = onCancel,
            style = PButtonStyle.Tertiary,
            size = PButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// -- Previews -----------------------------------------------------------------

private fun previewPrompt(): CarrierOnboardingPrompt = CarrierOnboardingPrompt(
    message = "Carrier must complete Stripe onboarding before match can be confirmed.",
    action = CarrierOnboardingPrompt.Action.AcceptShipperRequest(matchId = 42),
)

@Preview(showBackground = true, name = "CarrierOnboarding — idle")
@Preview(showBackground = true, name = "CarrierOnboarding — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierOnboardingRequiredSheetIdlePreview() {
    PasabayanTheme {
        CarrierOnboardingRequiredSheetContent(
            prompt = previewPrompt(),
            state = StripeConnectUiState(),
            onStartOnboarding = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, name = "CarrierOnboarding — loading")
@Composable
private fun CarrierOnboardingRequiredSheetLoadingPreview() {
    PasabayanTheme {
        CarrierOnboardingRequiredSheetContent(
            prompt = previewPrompt(),
            state = StripeConnectUiState(isLoading = true),
            onStartOnboarding = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, name = "CarrierOnboarding — error")
@Composable
private fun CarrierOnboardingRequiredSheetErrorPreview() {
    PasabayanTheme {
        CarrierOnboardingRequiredSheetContent(
            prompt = previewPrompt(),
            state = StripeConnectUiState(errorMessage = "Could not reach Stripe. Try again."),
            onStartOnboarding = {},
            onCancel = {},
        )
    }
}
