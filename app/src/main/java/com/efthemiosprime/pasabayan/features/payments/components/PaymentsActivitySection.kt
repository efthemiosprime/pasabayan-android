package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryUiState

@Composable
fun PaymentsActivitySection(
    historyState: TransactionHistoryUiState,
    receiptState: ReceiptListUiState,
    onOpenTransactionList: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    onLoadMoreReceipts: () -> Unit,
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
            )
            Text(
                text = stringResource(
                    R.string.payments_profile_receipts_count,
                    receiptState.receipts.size,
                    receiptState.totalCount,
                ),
                style = PasabayanTextStyles.Body.small,
            )
            PButton(
                text = stringResource(R.string.payments_transactions_open_list),
                onClick = onOpenTransactionList,
                modifier = Modifier.fillMaxWidth(),
            )
            historyState.transactions.take(3).forEach { transaction ->
                Text(
                    text = "${transaction.id} • ${transaction.formattedTotal}",
                    style = PasabayanTextStyles.Body.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = PasabayanSpacing.xs),
                )
                PButton(
                    text = transaction.description ?: stringResource(R.string.payments_transactions_default_description),
                    onClick = { onOpenTransaction(transaction) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (receiptState.hasMore) {
                PButton(
                    text = stringResource(R.string.payments_profile_load_more_receipts),
                    onClick = onLoadMoreReceipts,
                    modifier = Modifier.fillMaxWidth(),
                )
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
                        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 100.0),
                    ),
                ),
            ),
            receiptState = ReceiptListUiState(hasMore = true, totalCount = 2),
            onOpenTransactionList = {},
            onOpenTransaction = {},
            onLoadMoreReceipts = {},
        )
    }
}
