package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter
import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentFlowStatus
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentUiState

/**
 * Composite CTA: `PaymentSummaryCard` + status row + `PButton` (or paid-badge) + error message.
 * State-driven via [PaymentUiState] so call-sites only pass state + summary source + onPay.
 */
@Composable
fun PaymentButton(
    state: PaymentUiState,
    source: PaymentSummarySource,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = source.compute()
    val totalFormatted = MoneyFormatter.formatCurrency(rows.total, rows.currency)
    val alreadyPaid = isAlreadyPaid(state)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PaymentSummaryCard(source = source)

        paymentButtonStatusText(state)?.let { text ->
            Text(
                text = text,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (alreadyPaid) {
            AlreadyPaidBadge(modifier = Modifier.fillMaxWidth())
        } else {
            PButton(
                text = stringResource(R.string.payments_pay_button, totalFormatted),
                onClick = onPay,
                isLoading = state.isProcessing,
                enabled = state.isSheetReady && !state.isProcessing,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.Error,
            )
        }
    }
}

@Composable
private fun AlreadyPaidBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                PasabayanColors.Success.copy(alpha = 0.15f),
                RoundedCornerShape(PasabayanRadius.button),
            )
            .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = PasabayanColors.Success,
        )
        Text(
            text = stringResource(R.string.payments_paid_badge),
            style = PasabayanTextStyles.Body.medium,
            color = PasabayanColors.Success,
        )
    }
}

internal fun isAlreadyPaid(state: PaymentUiState): Boolean {
    val txStatus = state.transaction?.transactionStatus
    if (txStatus == TransactionStatus.CAPTURED || txStatus == TransactionStatus.COMPLETED) return true
    return state.flowStatus == PaymentFlowStatus.ALREADY_PAID ||
        state.flowStatus == PaymentFlowStatus.PAYMENT_SUCCESS
}

@Composable
private fun paymentButtonStatusText(state: PaymentUiState): String? = when {
    state.isProcessing -> stringResource(R.string.payments_profile_status_processing)
    state.flowStatus == PaymentFlowStatus.SHEET_READY -> stringResource(R.string.payments_profile_status_sheet_ready)
    state.flowStatus == PaymentFlowStatus.PAYMENT_CANCELED -> stringResource(R.string.payments_profile_status_canceled)
    state.flowStatus == PaymentFlowStatus.PAYMENT_SUCCESS -> stringResource(R.string.payments_profile_status_paid)
    state.flowStatus == PaymentFlowStatus.ALREADY_PAID -> stringResource(R.string.payments_profile_status_already_paid)
    else -> null
}

private val sampleSource = PaymentSummarySource.Prepayment(
    baseAmount = 150.0,
    config = StripeConfig(
        mode = "sandbox",
        publicKey = "pk_test",
        currency = "cad",
        minDeliveryPrice = StripeConfig.DEFAULT_MIN_DELIVERY_PRICE,
        senderFeePercentage = StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
        carrierFeePercentage = StripeConfig.DEFAULT_CARRIER_FEE_PERCENTAGE,
        platformFeePercentage = StripeConfig.DEFAULT_SENDER_FEE_PERCENTAGE,
    ),
)

@Preview(showBackground = true, name = "PaymentButton idle light")
@Preview(showBackground = true, name = "PaymentButton idle dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentButtonIdlePreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentButton(
                state = PaymentUiState(isSheetReady = true, flowStatus = PaymentFlowStatus.SHEET_READY),
                source = sampleSource,
                onPay = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentButton loading light")
@Preview(showBackground = true, name = "PaymentButton loading dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentButtonLoadingPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentButton(
                state = PaymentUiState(isProcessing = true, isSheetReady = true),
                source = sampleSource,
                onPay = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentButton error light")
@Preview(showBackground = true, name = "PaymentButton error dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentButtonErrorPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentButton(
                state = PaymentUiState(errorMessage = "Card declined", isSheetReady = false),
                source = sampleSource,
                onPay = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentButton already-paid light")
@Preview(showBackground = true, name = "PaymentButton already-paid dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentButtonAlreadyPaidPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentButton(
                state = PaymentUiState(flowStatus = PaymentFlowStatus.ALREADY_PAID),
                source = sampleSource,
                onPay = {},
            )
        }
    }
}
