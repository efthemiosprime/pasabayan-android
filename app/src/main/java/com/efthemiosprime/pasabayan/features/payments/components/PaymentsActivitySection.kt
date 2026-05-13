package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryUiState

/**
 * Profile-hub activity summary: recent transactions + receipts counts + CTAs into
 * [com.efthemiosprime.pasabayan.features.payments.ui.TransactionHistoryScreen] /
 * [com.efthemiosprime.pasabayan.features.payments.ui.ReceiptListScreen].
 *
 * Each summary row uses [MoneyFormatter] and surfaces [TipBadge] when the transaction
 * carries a tip.
 */
@Composable
fun PaymentsActivitySection(
    historyState: TransactionHistoryUiState,
    receiptState: ReceiptListUiState,
    onOpenTransactionList: () -> Unit,
    onOpenReceipts: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_profile_activity_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = stringResource(
                    R.string.payments_profile_transactions_count,
                    historyState.transactions.size,
                ),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.payments_profile_receipts_count,
                    receiptState.receipts.size,
                    receiptState.totalCount,
                ),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            historyState.transactions.take(3).forEach { tx ->
                TransactionPreviewRow(transaction = tx, onClick = { onOpenTransaction(tx) })
            }

            if (historyState.transactions.isNotEmpty()) PDivider()

            PButton(
                text = stringResource(R.string.payments_transactions_open_list),
                onClick = onOpenTransactionList,
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.payments_receipts_title),
                onClick = onOpenReceipts,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TransactionPreviewRow(transaction: Transaction, onClick: () -> Unit) {
    val amounts = transaction.amounts
    val total = amounts?.let { MoneyFormatter.formatCurrency(it.total, it.currency) }
        ?: transaction.formattedTotal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description
                    ?: stringResource(R.string.payments_transactions_default_description),
                style = PasabayanTextStyles.Body.medium,
            )
            Text(
                text = "#${transaction.id}",
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = total,
                style = PasabayanTextStyles.Body.medium,
                fontWeight = FontWeight.SemiBold,
            )
            val tipAmount = amounts?.tip ?: 0.0
            if (tipAmount > 0.0) {
                TipBadge(amount = tipAmount, currency = amounts!!.currency)
            }
        }
    }
}

@Preview(showBackground = true, name = "PaymentsActivity light")
@Preview(showBackground = true, name = "PaymentsActivity dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentsActivitySectionPreview() {
    PasabayanTheme {
        PaymentsActivitySection(
            historyState = TransactionHistoryUiState(
                transactions = listOf(
                    Transaction(
                        id = 9,
                        transactionStatus = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus.COMPLETED,
                        deliveryMatchId = 22,
                        description = "Delivery payment",
                        shipper = com.efthemiosprime.pasabayan.features.payments.model.TransactionUser(id = 1, name = "Shipper"),
                        carrier = com.efthemiosprime.pasabayan.features.payments.model.TransactionUser(id = 2, name = "Carrier"),
                        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 100.0, tip = 5.0),
                    ),
                    Transaction(
                        id = 10,
                        transactionStatus = com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus.PENDING,
                        deliveryMatchId = 23,
                        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 65.0),
                    ),
                ),
            ),
            receiptState = ReceiptListUiState(hasMore = true, totalCount = 2),
            onOpenTransactionList = {},
            onOpenReceipts = {},
            onOpenTransaction = {},
        )
    }
}
