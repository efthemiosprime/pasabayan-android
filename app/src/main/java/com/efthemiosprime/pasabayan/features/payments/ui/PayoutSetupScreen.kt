package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.payments.model.StripeConnectStatus
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel

/**
 * Carrier-only Stripe Connect setup screen. iOS parity: `PayoutSetupView.swift`.
 *
 * States:
 *  - **Loading** — initial fetch
 *  - **Not setup** — no Stripe account: requirements list + "Set Up Payouts"
 *  - **Partial setup** — account exists but onboarding incomplete: "Continue Setup"
 *  - **Complete** — `canPayout`: payments/payouts checkmarks + "View Dashboard"
 */
@Composable
fun PayoutSetupScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StripeConnectViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasLoadedOnce) {
            hasLoadedOnce = true
            viewModel.loadStatus()
        }
    }

    // Onboarding: launch Custom Tab once, treat any return to the screen as completion.
    var awaitingOnboardingReturn by remember { mutableStateOf(false) }
    LaunchedEffect(state.showOnboarding, state.onboardingUrl) {
        val url = state.onboardingUrl
        if (state.showOnboarding && url != null) {
            val launched = launchCustomTab(context, url)
            awaitingOnboardingReturn = launched
            if (!launched) {
                viewModel.handleOnboardingReturn()
            }
        }
    }

    // Dashboard: same — fire-and-forget, refresh on next foreground.
    LaunchedEffect(state.showDashboard, state.dashboardUrl) {
        val url = state.dashboardUrl
        if (state.showDashboard && url != null) {
            launchCustomTab(context, url)
            viewModel.handleDashboardDismiss()
        }
    }

    // When the user returns from the Custom Tab, the composition resumes — refresh status once.
    LaunchedEffect(awaitingOnboardingReturn) {
        if (awaitingOnboardingReturn && !state.showOnboarding) {
            awaitingOnboardingReturn = false
            viewModel.handleOnboardingReturn()
        }
    }

    PayoutSetupContent(
        state = state,
        onClose = onClose,
        onSetupOrContinue = viewModel::startOnboarding,
        onOpenDashboard = viewModel::openDashboard,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PayoutSetupContent(
    state: StripeConnectUiState,
    onClose: () -> Unit,
    onSetupOrContinue: () -> Unit,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.payments_payout_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.payments_payout_close),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (state.isLoading && state.status == null) {
                LoadingView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(PasabayanSpacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xl),
                ) {
                    StatusHeader(state.status)
                    InfoCard(state.status)
                    ActionButton(
                        status = state.status,
                        isLoading = state.isLoading,
                        onSetupOrContinue = onSetupOrContinue,
                        onOpenDashboard = onOpenDashboard,
                    )
                    state.errorMessage?.let { ErrorView(message = it) }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.payments_payout_loading_status),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusHeader(status: StripeConnectStatus?) {
    val tone = headerTone(status)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PasabayanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(tone.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tone.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = tone.color,
            )
        }
        Text(
            text = stringResource(tone.titleRes),
            style = PasabayanTextStyles.Heading.h4.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(tone.descriptionRes),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InfoCard(status: StripeConnectStatus?) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        if (status?.canPayout == true) {
            CompletedInfoContent(status)
        } else {
            SetupInfoContent()
        }
    }
}

@Composable
private fun SetupInfoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Text(
            text = stringResource(R.string.payments_payout_what_you_need),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            InfoRow(icon = Icons.Filled.Verified, text = stringResource(R.string.payments_payout_need_id))
            InfoRow(icon = Icons.Filled.AccountBalance, text = stringResource(R.string.payments_payout_need_bank))
            InfoRow(icon = Icons.Filled.Home, text = stringResource(R.string.payments_payout_need_address))
        }
        PDivider()
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PasabayanColors.Success,
            )
            Text(
                text = stringResource(R.string.payments_payout_secure_verification),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompletedInfoContent(status: StripeConnectStatus) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = PasabayanColors.Success,
            )
            Text(
                text = stringResource(R.string.payments_payout_account_active),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            StatusRow(
                label = stringResource(R.string.payments_payout_status_payments),
                enabled = status.chargesEnabled,
            )
            StatusRow(
                label = stringResource(R.string.payments_payout_status_payouts),
                enabled = status.payoutsEnabled,
            )
        }
        status.onboardedAt?.takeIf { it.isNotBlank() }?.let { onboardedAt ->
            Text(
                text = stringResource(R.string.payments_payout_connected_on, formatOnboardedAt(onboardedAt)),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusRow(label: String, enabled: Boolean) {
    val color = if (enabled) PasabayanColors.Success else PasabayanColors.Error
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color,
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                if (enabled) R.string.payments_payout_enabled else R.string.payments_payout_disabled,
            ),
            style = PasabayanTextStyles.Caption.regular,
            color = color,
        )
    }
}

@Composable
private fun ActionButton(
    status: StripeConnectStatus?,
    isLoading: Boolean,
    onSetupOrContinue: () -> Unit,
    onOpenDashboard: () -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        if (status?.canPayout == true) {
            PButton(
                text = stringResource(R.string.payments_payout_view_dashboard),
                onClick = onOpenDashboard,
                style = PButtonStyle.Submit,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            val labelRes = if (status?.hasAccount == true) {
                R.string.payments_payout_continue_setup
            } else {
                R.string.payments_payout_setup
            }
            PButton(
                text = stringResource(labelRes),
                onClick = onSetupOrContinue,
                style = PButtonStyle.Submit,
                icon = Icons.Filled.CreditCard,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PasabayanSpacing.sm))
            .background(PasabayanColors.Error.copy(alpha = 0.1f))
            .padding(PasabayanSpacing.md),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = PasabayanColors.Error,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Caption.regular,
            color = PasabayanColors.Error,
        )
    }
}

private data class HeaderTone(
    val icon: ImageVector,
    val color: Color,
    val titleRes: Int,
    val descriptionRes: Int,
)

@Composable
private fun headerTone(status: StripeConnectStatus?): HeaderTone {
    val primary = MaterialTheme.colorScheme.primary
    return when {
        status == null -> HeaderTone(
            icon = Icons.Filled.HelpOutline,
            color = PasabayanColors.BadgeGray,
            titleRes = R.string.payments_payout_status_loading,
            descriptionRes = R.string.payments_payout_checking_status,
        )
        status.canPayout -> HeaderTone(
            icon = Icons.Filled.CheckCircle,
            color = PasabayanColors.Success,
            titleRes = R.string.payments_payout_status_active,
            descriptionRes = R.string.payments_payout_description_active,
        )
        status.hasAccount -> HeaderTone(
            icon = Icons.Filled.Schedule,
            color = PasabayanColors.Warning,
            titleRes = R.string.payments_payout_status_incomplete,
            descriptionRes = R.string.payments_payout_description_incomplete,
        )
        else -> HeaderTone(
            icon = Icons.Filled.CreditCard,
            color = primary,
            titleRes = R.string.payments_payout_setup,
            descriptionRes = R.string.payments_payout_description_new,
        )
    }
}

/**
 * Best-effort ISO-8601 → display date. Falls back to the raw string when parsing fails so the
 * user still sees something useful (matches iOS DateFormatting fallback behavior).
 */
private fun formatOnboardedAt(raw: String): String {
    return try {
        val instant = java.time.Instant.parse(raw)
        java.time.format.DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.LONG)
            .withZone(java.time.ZoneId.systemDefault())
            .format(instant)
    } catch (e: Exception) {
        raw
    }
}

// --- Previews ---

@Preview(name = "PayoutSetup loading light", showBackground = true)
@Preview(name = "PayoutSetup loading dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PayoutSetupLoadingPreview() {
    PasabayanTheme {
        PayoutSetupContent(
            state = StripeConnectUiState(isLoading = true),
            onClose = {},
            onSetupOrContinue = {},
            onOpenDashboard = {},
        )
    }
}

@Preview(name = "PayoutSetup not setup light", showBackground = true)
@Preview(name = "PayoutSetup not setup dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PayoutSetupNotSetupPreview() {
    PasabayanTheme {
        PayoutSetupContent(
            state = StripeConnectUiState(
                status = StripeConnectStatus(hasStripeAccount = false),
            ),
            onClose = {},
            onSetupOrContinue = {},
            onOpenDashboard = {},
        )
    }
}

@Preview(name = "PayoutSetup partial light", showBackground = true)
@Preview(name = "PayoutSetup partial dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PayoutSetupPartialPreview() {
    PasabayanTheme {
        PayoutSetupContent(
            state = StripeConnectUiState(
                status = StripeConnectStatus(
                    hasStripeAccount = true,
                    onboardingComplete = false,
                    chargesEnabled = false,
                    payoutsEnabled = false,
                    canReceivePayouts = false,
                ),
            ),
            onClose = {},
            onSetupOrContinue = {},
            onOpenDashboard = {},
        )
    }
}

@Preview(name = "PayoutSetup complete light", showBackground = true)
@Preview(name = "PayoutSetup complete dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PayoutSetupCompletePreview() {
    PasabayanTheme {
        PayoutSetupContent(
            state = StripeConnectUiState(
                status = StripeConnectStatus(
                    hasStripeAccount = true,
                    onboardingComplete = true,
                    chargesEnabled = true,
                    payoutsEnabled = true,
                    canReceivePayouts = true,
                    onboardedAt = "2026-04-12T10:00:00Z",
                ),
            ),
            onClose = {},
            onSetupOrContinue = {},
            onOpenDashboard = {},
        )
    }
}

@Preview(name = "PayoutSetup error light", showBackground = true)
@Composable
private fun PayoutSetupErrorPreview() {
    PasabayanTheme {
        PayoutSetupContent(
            state = StripeConnectUiState(
                status = StripeConnectStatus(hasStripeAccount = false),
                errorMessage = "Stripe Connect is only available for carriers",
            ),
            onClose = {},
            onSetupOrContinue = {},
            onOpenDashboard = {},
        )
    }
}
