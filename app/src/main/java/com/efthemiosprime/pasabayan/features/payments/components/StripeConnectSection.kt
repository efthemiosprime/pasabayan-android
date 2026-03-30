package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectUiState
import androidx.compose.material3.Text

@Composable
fun StripeConnectSection(
    connectState: StripeConnectUiState,
    onStartOnboarding: () -> Unit,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_connect_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = if (connectState.status?.onboardingComplete == true) {
                    stringResource(R.string.payments_profile_connect_ready)
                } else {
                    stringResource(R.string.payments_profile_connect_not_ready)
                },
                style = PasabayanTextStyles.Body.small,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PButton(
                    text = stringResource(R.string.payments_profile_start_onboarding),
                    onClick = onStartOnboarding,
                    modifier = Modifier.weight(1f),
                )
                PButton(
                    text = stringResource(R.string.payments_profile_open_dashboard),
                    onClick = onOpenDashboard,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "StripeConnect light")
@Preview(showBackground = true, name = "StripeConnect dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StripeConnectSectionPreview() {
    PasabayanTheme {
        StripeConnectSection(
            connectState = StripeConnectUiState(),
            onStartOnboarding = {},
            onOpenDashboard = {},
        )
    }
}
