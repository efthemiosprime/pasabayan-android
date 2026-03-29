package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.bookings.components.PickupCodeView
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.LiveTrackingViewModel

@Composable
fun GeneratePickupCodeScreen(
    matchId: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveTrackingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(matchId) { viewModel.generatePickupCode(matchId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.bookings_generate_pickup_code_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.bookings_generate_pickup_code_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PasabayanSpacing.sm),
        )

        if (state.isGeneratingCode) {
            PCircularProgress(modifier = Modifier.padding(top = PasabayanSpacing.xxl))
        }

        state.pickupCode?.let { code ->
            PickupCodeView(
                code = code,
                expiresAt = null,
                modifier = Modifier.padding(top = PasabayanSpacing.lg),
            )
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = PasabayanSpacing.md),
            )
        }

        PButton(
            text = stringResource(R.string.bookings_success_done),
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.xxl),
        )
    }
}

@Preview(showBackground = true, name = "GeneratePickup — light")
@Preview(showBackground = true, name = "GeneratePickup — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GeneratePickupPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Generate Pickup Code", style = PasabayanTextStyles.Heading.h4)
            PickupCodeView(code = "123456", expiresAt = "Expires: 6:00 PM")
        }
    }
}
