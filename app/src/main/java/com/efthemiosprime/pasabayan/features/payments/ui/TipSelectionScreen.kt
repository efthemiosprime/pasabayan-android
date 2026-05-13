package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TippingUiState

/**
 * Tip-selection surface (full screen). Composes preset chips + custom amount field.
 * Host owns the Stripe PaymentSheet presentation when [TippingUiState.paymentSheetReady] flips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipSelectionScreen(
    state: TippingUiState,
    carrierName: String,
    onPresetTip: (Double) -> Unit,
    onCustomTip: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.payments_tip_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(PasabayanSpacing.screenPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            TipHeader(carrierName = carrierName)
            PresetChipsSection(
                selected = state.selectedTipAmount,
                onPresetTip = onPresetTip,
            )
            POutlinedTextField(
                value = state.customTipAmount,
                onValueChange = onCustomTip,
                label = { Text(stringResource(R.string.payments_tip_custom_label)) },
                placeholder = { Text(stringResource(R.string.payments_tip_custom_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = state.errorMessage != null,
                supportingText = state.errorMessage?.let { msg ->
                    { Text(msg, color = MaterialTheme.colorScheme.error) }
                },
            )
            state.errorMessage?.takeIf { state.customTipAmount.isBlank() && !state.isValidTip }?.let {
                Text(
                    text = stringResource(
                        R.string.payments_tip_invalid,
                        MoneyFormatter.formatCurrency(TippingUiState.MIN_TIP),
                        MoneyFormatter.formatCurrency(TippingUiState.MAX_TIP),
                    ),
                    style = PasabayanTextStyles.Body.small,
                    color = PasabayanColors.Error,
                )
            }
            if (state.showSuccess) {
                Text(
                    text = stringResource(R.string.payments_tip_success),
                    style = PasabayanTextStyles.Body.medium,
                    color = PasabayanColors.Success,
                )
            }
            PButton(
                text = stringResource(
                    R.string.payments_tip_submit,
                    MoneyFormatter.formatCurrency(state.effectiveTipAmount.coerceAtLeast(0.0)),
                ),
                onClick = onSubmit,
                isLoading = state.isLoading,
                enabled = state.isValidTip && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.payments_tip_skip),
                onClick = onSkip,
                style = PButtonStyle.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TipHeader(carrierName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = PasabayanColors.BadgePink,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.payments_tip_subtitle, carrierName),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PasabayanSpacing.xs),
        )
    }
}

@Composable
private fun PresetChipsSection(
    selected: Double,
    onPresetTip: (Double) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.payments_tip_preset_label),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            TippingUiState.PRESET_TIPS.forEach { amount ->
                PFilterChip(
                    label = MoneyFormatter.formatCurrency(amount),
                    selected = selected == amount,
                    onClick = { onPresetTip(amount) },
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "TipSelectionScreen idle light")
@Preview(showBackground = true, name = "TipSelectionScreen idle dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TipSelectionScreenIdlePreview() {
    PasabayanTheme {
        TipSelectionScreen(
            state = TippingUiState(),
            carrierName = "John",
            onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "TipSelectionScreen preset selected")
@Composable
private fun TipSelectionScreenPresetPreview() {
    PasabayanTheme {
        TipSelectionScreen(
            state = TippingUiState(selectedTipAmount = 5.0),
            carrierName = "John",
            onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "TipSelectionScreen success")
@Composable
private fun TipSelectionScreenSuccessPreview() {
    PasabayanTheme {
        TipSelectionScreen(
            state = TippingUiState(selectedTipAmount = 5.0, showSuccess = true),
            carrierName = "John",
            onPresetTip = {}, onCustomTip = {}, onSubmit = {}, onSkip = {}, onBack = {},
        )
    }
}
