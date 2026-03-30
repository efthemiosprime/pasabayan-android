package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionDetailUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionDetailViewModel

@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    filter: TransactionFilter,
    onBack: () -> Unit,
    onRequestRefund: (Int) -> Unit,
    onAddTip: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    TransactionDetailContent(
        uiState = uiState,
        filter = filter,
        onBack = onBack,
        onRequestRefund = onRequestRefund,
        onAddTip = onAddTip,
        onCancel = { viewModel.cancelTransaction(transactionId) },
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    filter: TransactionFilter,
    onBack: () -> Unit,
    onRequestRefund: (Int) -> Unit,
    onAddTip: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transaction = uiState.transaction
    Column(modifier = modifier) {
        PTopBar(
            title = if (transaction == null) {
                stringResource(R.string.payments_transactions_title)
            } else {
                stringResource(R.string.payments_transaction_detail_title, transaction.id)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                }
            },
        )
        if (transaction == null) {
            Text(
                text = uiState.errorMessage ?: stringResource(R.string.payments_transactions_loading),
                modifier = Modifier.padding(PasabayanSpacing.screenPadding),
                style = PasabayanTextStyles.Body.medium,
                color = if (uiState.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            return
        }
        val isCarrier = filter == TransactionFilter.CARRIER
        val canCancel = !isCarrier && transaction.transactionStatus == TransactionStatus.PENDING
        val canTip = !isCarrier &&
            (transaction.transactionStatus == TransactionStatus.COMPLETED || transaction.transactionStatus == TransactionStatus.CAPTURED) &&
            (transaction.tipAmount ?: 0.0) <= 0.0
        val canRefund = !isCarrier &&
            (transaction.transactionStatus == TransactionStatus.CAPTURED || transaction.transactionStatus == TransactionStatus.COMPLETED)

        Column(
            modifier = Modifier.padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            PCard {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.payments_transaction_detail_status),
                        style = PasabayanTextStyles.Heading.h5,
                    )
                    Text(
                        text = transactionStatusLabel(transaction.transactionStatus),
                        style = PasabayanTextStyles.Body.medium,
                        color = transactionStatusColor(transaction.transactionStatus),
                    )
                }
            }
            PCard {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = if (isCarrier) {
                            stringResource(R.string.payments_transaction_detail_earnings_summary)
                        } else {
                            stringResource(R.string.payments_transaction_detail_amount_summary)
                        },
                        style = PasabayanTextStyles.Heading.h5,
                    )
                    AmountRow(
                        label = stringResource(R.string.payments_transaction_detail_total_paid),
                        value = transaction.formattedTotal,
                    )
                    if (transaction.platformFee != null) {
                        AmountRow(
                            label = stringResource(R.string.payments_transaction_detail_platform_fee),
                            value = String.format("$%.2f %s", transaction.platformFee, transaction.currency.uppercase()),
                        )
                    }
                    if (transaction.carrierReceives != null) {
                        AmountRow(
                            label = stringResource(R.string.payments_transaction_detail_carrier_receives),
                            value = String.format("$%.2f %s", transaction.carrierReceives, transaction.currency.uppercase()),
                        )
                    }
                    if (transaction.tipAmount != null && transaction.tipAmount > 0) {
                        AmountRow(
                            label = stringResource(R.string.payments_transaction_detail_tip),
                            value = String.format("$%.2f %s", transaction.tipAmount, transaction.currency.uppercase()),
                        )
                    }
                }
            }
            PCard {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.payments_transaction_detail_transaction_info),
                        style = PasabayanTextStyles.Heading.h5,
                    )
                    if (transaction.deliveryMatchId != null) {
                        Text(
                            text = stringResource(
                                R.string.payments_transaction_detail_delivery_match,
                                transaction.deliveryMatchId,
                            ),
                            style = PasabayanTextStyles.Body.medium,
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.payments_transaction_detail_shipper,
                            transaction.shipperName ?: "-",
                        ),
                        style = PasabayanTextStyles.Body.small,
                    )
                    Text(
                        text = stringResource(
                            R.string.payments_transaction_detail_carrier,
                            transaction.carrierName ?: "-",
                        ),
                        style = PasabayanTextStyles.Body.small,
                    )
                    if (transaction.createdAt != null) {
                        Text(
                            text = stringResource(
                                R.string.payments_transaction_detail_created_at,
                                transaction.createdAt,
                            ),
                            style = PasabayanTextStyles.Body.small,
                        )
                    }
                    if (transaction.updatedAt != null) {
                        Text(
                            text = stringResource(
                                R.string.payments_transaction_detail_updated_at,
                                transaction.updatedAt,
                            ),
                            style = PasabayanTextStyles.Body.small,
                        )
                    }
                }
            }
            if (canTip || canRefund || canCancel) {
                PCard {
                    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                        Text(
                            text = stringResource(R.string.payments_transaction_detail_actions),
                            style = PasabayanTextStyles.Heading.h5,
                        )
                        if (canTip) {
                            PButton(
                                text = stringResource(R.string.payments_transaction_detail_add_tip),
                                onClick = { onAddTip(transaction.id) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (canRefund) {
                            PButton(
                                text = stringResource(R.string.payments_transaction_detail_request_refund),
                                onClick = { onRequestRefund(transaction.id) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (canCancel) {
                            PButton(
                                text = stringResource(R.string.payments_transaction_detail_cancel_payment),
                                onClick = onCancel,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage,
                                style = PasabayanTextStyles.Body.small,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    value: String,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = PasabayanTextStyles.Body.medium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true, name = "TransactionDetail light")
@Preview(showBackground = true, name = "TransactionDetail dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailContentPreview() {
    PasabayanTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                transaction = Transaction(
                    id = 12,
                    transactionStatus = TransactionStatus.COMPLETED,
                    deliveryMatchId = 77,
                    description = "Delivery payment",
                    shipperName = "Alice",
                    carrierName = "Bob",
                    totalAmount = 155.0,
                    subtotal = 140.0,
                    platformFee = 15.0,
                    carrierReceives = 132.0,
                    currency = "cad",
                    tipAmount = null,
                    clientSecret = null,
                    customerId = null,
                    ephemeralKey = null,
                    payoutStatus = "completed",
                    createdAt = "2026-03-30T10:00:00Z",
                    updatedAt = "2026-03-30T10:05:00Z",
                ),
            ),
            filter = TransactionFilter.SHIPPER,
            onBack = {},
            onRequestRefund = {},
            onAddTip = {},
            onCancel = {},
        )
    }
}
