package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
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
import com.efthemiosprime.pasabayan.features.payments.viewmodel.RefundUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TippingUiState

@Composable
fun TippingRefundSection(
    tippingState: TippingUiState,
    refundState: RefundUiState,
    onAddTip: (Int, Double) -> Unit,
    onRequestRefund: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tipTransactionId by remember { mutableStateOf("0") }
    var tipAmount by remember { mutableStateOf("0.0") }
    var refundTransactionId by remember { mutableStateOf("0") }
    var refundReason by remember { mutableStateOf("") }

    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_tipping_refund_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = tipTransactionId,
                onValueChange = { tipTransactionId = it },
                label = { Text(stringResource(R.string.payments_profile_transaction_id)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = tipAmount,
                onValueChange = { tipAmount = it },
                label = { Text(stringResource(R.string.payments_profile_tip_amount)) },
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.payments_profile_add_tip),
                onClick = {
                    val id = tipTransactionId.toIntOrNull() ?: return@PButton
                    val amount = tipAmount.toDoubleOrNull() ?: return@PButton
                    onAddTip(id, amount)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = refundTransactionId,
                onValueChange = { refundTransactionId = it },
                label = { Text(stringResource(R.string.payments_profile_refund_transaction_id)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = refundReason,
                onValueChange = { refundReason = it },
                label = { Text(stringResource(R.string.payments_profile_refund_reason)) },
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.payments_profile_request_refund),
                onClick = {
                    val id = refundTransactionId.toIntOrNull() ?: return@PButton
                    if (refundReason.isBlank()) return@PButton
                    onRequestRefund(id, refundReason)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = tippingState.errorMessage ?: refundState.errorMessage ?: stringResource(R.string.payments_profile_idle),
                style = PasabayanTextStyles.Body.small,
            )
        }
    }
}

@Preview(showBackground = true, name = "TippingRefund light")
@Preview(showBackground = true, name = "TippingRefund dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TippingRefundSectionPreview() {
    PasabayanTheme {
        TippingRefundSection(
            tippingState = TippingUiState(),
            refundState = RefundUiState(),
            onAddTip = { _, _ -> },
            onRequestRefund = { _, _ -> },
        )
    }
}
