package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryUiState

@Composable
fun PaymentsActivitySection(
    historyState: TransactionHistoryUiState,
    receiptState: ReceiptListUiState,
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
            historyState = TransactionHistoryUiState(transactions = emptyList()),
            receiptState = ReceiptListUiState(hasMore = true, totalCount = 2),
            onLoadMoreReceipts = {},
        )
    }
}
