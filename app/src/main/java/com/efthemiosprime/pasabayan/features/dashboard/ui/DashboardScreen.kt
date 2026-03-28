package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle

/**
 * Post–consent-onboarding shell (iOS `DashboardView` placeholder until role tabs ship).
 */
@Composable
fun DashboardScreen(
    userName: String,
    isBusy: Boolean,
    onLogout: () -> Unit,
    apiBaseUrl: String,
    showApiFooter: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
            Text(
                text = stringResource(R.string.auth_signed_in_as, userName),
                style = PasabayanTextStyles.Body.large,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            PButton(
                text = stringResource(R.string.auth_sign_out),
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                style = PButtonStyle.Secondary,
                enabled = !isBusy,
            )
            if (showApiFooter) {
                Spacer(modifier = Modifier.height(PasabayanSpacing.xxl))
                Text(
                    text = stringResource(R.string.auth_api_footer, apiBaseUrl),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
