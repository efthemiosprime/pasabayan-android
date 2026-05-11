package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectStatusJson
import com.efthemiosprime.pasabayan.features.payments.model.canPayout
import com.efthemiosprime.pasabayan.features.payments.model.hasAccount
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectUiState

/**
 * Summary card shown in the payments hub. Taps route into the dedicated
 * [com.efthemiosprime.pasabayan.features.payments.ui.PayoutSetupScreen] for the full flow.
 */
@Composable
fun StripeConnectSection(
    connectState: StripeConnectUiState,
    onOpenPayoutSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = connectState.status
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_connect_section),
                style = PasabayanTextStyles.Heading.h5,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                Icon(
                    imageVector = statusIcon(status),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = statusColor(status),
                )
                Text(
                    text = stringResource(statusLabel(status)),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PButton(
                text = stringResource(ctaLabel(status)),
                onClick = onOpenPayoutSetup,
            )
        }
    }
}

private fun statusIcon(status: StripeConnectStatusJson?) = when {
    status?.canPayout == true -> Icons.Filled.CheckCircle
    status?.hasAccount == true -> Icons.Filled.Schedule
    else -> Icons.Filled.CreditCard
}

private fun statusColor(status: StripeConnectStatusJson?) = when {
    status?.canPayout == true -> PasabayanColors.Success
    status?.hasAccount == true -> PasabayanColors.Warning
    else -> PasabayanColors.Info
}

private fun statusLabel(status: StripeConnectStatusJson?) = when {
    status?.canPayout == true -> R.string.payments_payout_status_active
    status?.hasAccount == true -> R.string.payments_payout_status_incomplete
    else -> R.string.payments_profile_connect_not_ready
}

private fun ctaLabel(status: StripeConnectStatusJson?) = when {
    status?.canPayout == true -> R.string.payments_payout_view_dashboard
    status?.hasAccount == true -> R.string.payments_payout_continue_setup
    else -> R.string.payments_payout_setup
}

@Preview(showBackground = true, name = "StripeConnect not setup")
@Preview(showBackground = true, name = "StripeConnect not setup dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StripeConnectSectionNotSetupPreview() {
    PasabayanTheme {
        StripeConnectSection(
            connectState = StripeConnectUiState(status = StripeConnectStatusJson()),
            onOpenPayoutSetup = {},
        )
    }
}

@Preview(showBackground = true, name = "StripeConnect active")
@Preview(showBackground = true, name = "StripeConnect active dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StripeConnectSectionActivePreview() {
    PasabayanTheme {
        StripeConnectSection(
            connectState = StripeConnectUiState(
                status = StripeConnectStatusJson(
                    hasStripeAccount = true,
                    onboardingComplete = true,
                    chargesEnabled = true,
                    payoutsEnabled = true,
                    canReceivePayouts = true,
                ),
            ),
            onOpenPayoutSetup = {},
        )
    }
}
