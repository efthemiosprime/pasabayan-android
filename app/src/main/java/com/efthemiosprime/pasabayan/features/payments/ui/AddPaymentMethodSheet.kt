package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.features.payments.viewmodel.AddCardFlowState

/**
 * Modal bottom sheet that introduces the add-card flow. The actual Stripe
 * `PaymentSheet.presentForSetupIntent(...)` is invoked from the host composable
 * once the VM's [AddCardFlowState] reaches `Ready` — this sheet only surfaces
 * the *preparation* UI (Stripe lock branding, requirements list, Continue CTA).
 *
 * Wiring contract:
 * - [onContinue] should call `paymentMethodsViewModel.prepareAddPaymentMethod()`.
 *   The host watches `state.addCardFlowState` and presents the Stripe sheet when Ready.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodSheet(
    flowState: AddCardFlowState,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        AddPaymentMethodSheetContent(
            flowState = flowState,
            onContinue = onContinue,
        )
    }
}

@Composable
private fun AddPaymentMethodSheetContent(
    flowState: AddCardFlowState,
    onContinue: () -> Unit,
) {
    val isPreparing = flowState is AddCardFlowState.Preparing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        StripeLockHeader()

        Text(
            text = stringResource(R.string.payments_add_card_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.payments_add_card_subtitle),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RequirementsCard()

        PButton(
            text = stringResource(R.string.payments_add_card_continue),
            onClick = onContinue,
            isLoading = isPreparing,
            enabled = !isPreparing,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isPreparing) {
            Text(
                text = stringResource(R.string.payments_add_card_preparing),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        (flowState as? AddCardFlowState.Failed)?.let { failed ->
            Text(
                text = failed.message,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.Error,
            )
        }

        Text(
            text = stringResource(R.string.payments_add_card_powered_by),
            style = PasabayanTextStyles.Caption.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StripeLockHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = PasabayanColors.Success,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.payments_methods_secure_notice),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequirementsCard() {
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_add_card_requirements_title),
                style = PasabayanTextStyles.Body.medium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            NumberedStep(index = 1, label = stringResource(R.string.payments_add_card_step_1))
            NumberedStep(index = 2, label = stringResource(R.string.payments_add_card_step_2))
            NumberedStep(index = 3, label = stringResource(R.string.payments_add_card_step_3))
            NumberedStep(index = 4, label = stringResource(R.string.payments_add_card_step_4))
        }
    }
}

@Composable
private fun NumberedStep(index: Int, label: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = "$index.",
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 1.dp),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

// Previews render the sheet *content* without the modal scrim (M3 modal sheets
// don't render cleanly inside @Preview).

@Preview(showBackground = true, name = "AddPaymentMethodSheet idle light")
@Preview(showBackground = true, name = "AddPaymentMethodSheet idle dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddPaymentMethodSheetIdlePreview() {
    PasabayanTheme {
        AddPaymentMethodSheetContent(flowState = AddCardFlowState.Idle, onContinue = {})
    }
}

@Preview(showBackground = true, name = "AddPaymentMethodSheet preparing")
@Composable
private fun AddPaymentMethodSheetPreparingPreview() {
    PasabayanTheme {
        AddPaymentMethodSheetContent(flowState = AddCardFlowState.Preparing, onContinue = {})
    }
}

@Preview(showBackground = true, name = "AddPaymentMethodSheet failed")
@Composable
private fun AddPaymentMethodSheetFailedPreview() {
    PasabayanTheme {
        AddPaymentMethodSheetContent(
            flowState = AddCardFlowState.Failed("Stripe is unavailable. Please try again."),
            onContinue = {},
        )
    }
}
