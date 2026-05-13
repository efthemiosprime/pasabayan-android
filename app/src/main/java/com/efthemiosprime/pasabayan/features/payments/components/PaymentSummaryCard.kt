package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter
import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig
import com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts

/**
 * Money breakdown card. Renders base amount → service fee → total (bold) → carrier-receives info line.
 *
 * - [PaymentSummarySource.Prepayment]: fees computed from [StripeConfig]
 * - [PaymentSummarySource.Postpayment]: values read from [TransactionAmounts]
 */
@Composable
fun PaymentSummaryCard(
    source: PaymentSummarySource,
    modifier: Modifier = Modifier,
) {
    val rows = source.compute()
    val currency = rows.currency
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            rows.baseAmount?.let { base ->
                PDetailRow(
                    label = stringResource(R.string.payments_summary_base_amount),
                    value = MoneyFormatter.formatCurrency(base, currency),
                )
            }
            PDetailRow(
                label = stringResource(R.string.payments_summary_service_fee),
                value = MoneyFormatter.formatCurrency(rows.serviceFee, currency),
            )
            PDivider()
            TotalRow(value = MoneyFormatter.formatCurrency(rows.total, currency))
            Text(
                text = stringResource(
                    R.string.payments_summary_carrier_receives,
                    MoneyFormatter.formatCurrency(rows.carrierReceives, currency),
                ),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = PasabayanSpacing.xs),
            )
        }
    }
}

@Composable
private fun TotalRow(value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.payments_summary_total),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, name = "PaymentSummaryCard prepayment light")
@Preview(showBackground = true, name = "PaymentSummaryCard prepayment dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentSummaryCardPrepaymentPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentSummaryCard(
                source = PaymentSummarySource.Prepayment(
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
                ),
            )
        }
    }
}

@Preview(showBackground = true, name = "PaymentSummaryCard postpayment light")
@Preview(showBackground = true, name = "PaymentSummaryCard postpayment dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentSummaryCardPostpaymentPreview() {
    PasabayanTheme {
        Column(modifier = Modifier.padding(PasabayanSpacing.md)) {
            PaymentSummaryCard(
                source = PaymentSummarySource.Postpayment(
                    amounts = TransactionAmounts(
                        total = 165.0,
                        subtotal = 150.0,
                        platformFee = 15.0,
                        carrierReceives = 142.5,
                        currency = "cad",
                        baseAmount = 150.0,
                    ),
                ),
            )
        }
    }
}
