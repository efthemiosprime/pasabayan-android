package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentFlowStatus
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentUiState
import androidx.compose.material3.Text

@Composable
fun PaymentProcessingSection(
    paymentState: PaymentUiState,
    statusText: String,
    onCreatePayment: (Int, Double) -> Unit,
    onPresentPaymentSheet: () -> Unit,
    onConfirmCapture: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var paymentMatchId by remember { mutableStateOf("0") }
    var paymentAmount by remember { mutableStateOf("0.0") }

    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_payment_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = paymentMatchId,
                onValueChange = { paymentMatchId = it },
                label = { Text(stringResource(R.string.payments_profile_delivery_match_id)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = paymentAmount,
                onValueChange = { paymentAmount = it },
                label = { Text(stringResource(R.string.payments_profile_amount)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PButton(
                    text = stringResource(R.string.payments_profile_create_payment),
                    onClick = {
                        val id = paymentMatchId.toIntOrNull() ?: return@PButton
                        val amount = paymentAmount.toDoubleOrNull() ?: return@PButton
                        onCreatePayment(id, amount)
                    },
                    modifier = Modifier.weight(1f),
                )
                PButton(
                    text = stringResource(R.string.payments_profile_present_sheet),
                    onClick = onPresentPaymentSheet,
                    enabled = paymentState.isSheetReady,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PButton(
                    text = stringResource(R.string.payments_profile_confirm_capture),
                    onClick = {
                        val id = paymentMatchId.toIntOrNull() ?: return@PButton
                        onConfirmCapture(id)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = paymentState.errorMessage ?: statusText,
                style = PasabayanTextStyles.Body.small,
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentProcessing light")
@Preview(showBackground = true, name = "PaymentProcessing dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentProcessingSectionPreview() {
    PasabayanTheme {
        PaymentProcessingSection(
            paymentState = PaymentUiState(
                flowStatus = PaymentFlowStatus.SHEET_READY,
                isSheetReady = true,
            ),
            statusText = "",
            onCreatePayment = { _, _ -> },
            onPresentPaymentSheet = {},
            onConfirmCapture = {},
        )
    }
}
