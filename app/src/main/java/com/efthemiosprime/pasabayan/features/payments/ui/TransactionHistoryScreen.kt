package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryViewModel

enum class TransactionFilter(val apiRole: String?) {
    ALL(null),
    SHIPPER("shipper"),
    CARRIER("carrier"),
}

@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    onOpenTransaction: (Transaction, TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    LaunchedEffect(selectedFilter) {
        viewModel.loadTransactions(selectedFilter.apiRole)
    }

    TransactionHistoryContent(
        uiState = uiState,
        selectedFilter = selectedFilter,
        onFilterChange = { selectedFilter = it },
        onRefresh = { viewModel.refreshTransactions(selectedFilter.apiRole) },
        onBack = onBack,
        onOpenTransaction = { onOpenTransaction(it, selectedFilter) },
        modifier = modifier,
    )
}

@Composable
private fun TransactionHistoryContent(
    uiState: TransactionHistoryUiState,
    selectedFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PTopBar(
            title = stringResource(R.string.payments_transactions_title),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                }
            },
        )
        Column(
            modifier = Modifier.padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            TransactionFilterRow(
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange,
            )
            when {
                uiState.isLoading -> {
                    Text(
                        text = stringResource(R.string.payments_transactions_loading),
                        style = PasabayanTextStyles.Body.medium,
                    )
                }

                uiState.transactions.isEmpty() -> {
                    PCard {
                        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                            Text(
                                text = stringResource(R.string.payments_transactions_empty_title),
                                style = PasabayanTextStyles.Heading.h5,
                            )
                            Text(
                                text = stringResource(R.string.payments_transactions_empty_description),
                                style = PasabayanTextStyles.Body.small,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    ) {
                        items(uiState.transactions, key = { it.id }) { transaction ->
                            TransactionRowCard(
                                transaction = transaction,
                                onClick = { onOpenTransaction(transaction) },
                            )
                        }
                    }
                }
            }
            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            PButton(
                text = stringResource(R.string.payments_profile_refresh),
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TransactionFilterRow(
    selectedFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        TransactionFilter.entries.forEach { filter ->
            val text = when (filter) {
                TransactionFilter.ALL -> stringResource(R.string.payments_transactions_filter_all)
                TransactionFilter.SHIPPER -> stringResource(R.string.payments_transactions_filter_shipper)
                TransactionFilter.CARRIER -> stringResource(R.string.payments_transactions_filter_carrier)
            }
            PButton(
                text = text,
                onClick = { onFilterChange(filter) },
                modifier = Modifier.weight(1f),
                enabled = selectedFilter != filter,
            )
        }
    }
}

@Composable
private fun TransactionRowCard(
    transaction: Transaction,
    onClick: () -> Unit,
) {
    PCard(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = transactionStatusLabel(transaction.transactionStatus),
                style = PasabayanTextStyles.Body.small,
                color = transactionStatusColor(transaction.transactionStatus),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: stringResource(R.string.payments_transactions_default_description),
                    style = PasabayanTextStyles.Body.medium,
                )
                if (transaction.createdAt != null) {
                    Text(
                        text = transaction.createdAt,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = transaction.formattedTotal,
                style = PasabayanTextStyles.Body.medium,
            )
        }
    }
}

@Composable
internal fun transactionStatusLabel(status: TransactionStatus): String = when (status) {
    TransactionStatus.PENDING -> stringResource(R.string.payments_transaction_status_pending)
    TransactionStatus.AUTHORIZED -> stringResource(R.string.payments_transaction_status_authorized)
    TransactionStatus.CAPTURED -> stringResource(R.string.payments_transaction_status_captured)
    TransactionStatus.COMPLETED -> stringResource(R.string.payments_transaction_status_completed)
    TransactionStatus.REFUNDED -> stringResource(R.string.payments_transaction_status_refunded)
    TransactionStatus.CANCELLED -> stringResource(R.string.payments_transaction_status_cancelled)
    TransactionStatus.FAILED -> stringResource(R.string.payments_transaction_status_failed)
    TransactionStatus.UNKNOWN -> stringResource(R.string.payments_transaction_status_unknown)
}

@Composable
internal fun transactionStatusColor(status: TransactionStatus) = when (status) {
    TransactionStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    TransactionStatus.AUTHORIZED -> MaterialTheme.colorScheme.primary
    TransactionStatus.CAPTURED -> MaterialTheme.colorScheme.primary
    TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
    TransactionStatus.REFUNDED -> MaterialTheme.colorScheme.secondary
    TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.error
    TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
    TransactionStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Preview(showBackground = true, name = "TransactionHistory light")
@Preview(showBackground = true, name = "TransactionHistory dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionHistoryContentPreview() {
    PasabayanTheme {
        TransactionHistoryContent(
            uiState = TransactionHistoryUiState(
                transactions = listOf(
                    Transaction(
                        id = 1,
                        transactionStatus = TransactionStatus.COMPLETED,
                        deliveryMatchId = 10,
                        description = "Delivery Toronto to Montreal",
                        shipperName = "Shipper",
                        carrierName = "Carrier",
                        totalAmount = 120.0,
                        subtotal = 100.0,
                        platformFee = 20.0,
                        carrierReceives = 95.0,
                        currency = "cad",
                        tipAmount = 5.0,
                        clientSecret = null,
                        customerId = null,
                        ephemeralKey = null,
                        payoutStatus = "completed",
                        createdAt = "2026-03-30T12:00:00Z",
                        updatedAt = "2026-03-30T12:10:00Z",
                    ),
                ),
            ),
            selectedFilter = TransactionFilter.ALL,
            onFilterChange = {},
            onRefresh = {},
            onBack = {},
            onOpenTransaction = {},
        )
    }
}
