package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.components.PayoutStatusBadge
import com.efthemiosprime.pasabayan.features.payments.components.PaymentSummaryCard
import com.efthemiosprime.pasabayan.features.payments.components.PaymentSummarySource
import com.efthemiosprime.pasabayan.features.payments.components.RefundStatusCard
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts
import com.efthemiosprime.pasabayan.features.payments.model.TransactionTimestamps
import com.efthemiosprime.pasabayan.features.payments.model.TransactionUser
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
    LaunchedEffect(transactionId, filter) {
        viewModel.loadTransaction(transactionId, filter.toRole())
    }
    TransactionDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRequestRefund = { onRequestRefund(transactionId) },
        onAddTip = { onAddTip(transactionId) },
        onCancel = { viewModel.cancelTransaction(transactionId) },
        modifier = modifier,
    )
}

private fun TransactionFilter.toRole(): TransactionRole? = when (this) {
    TransactionFilter.SHIPPER -> TransactionRole.SHIPPER
    TransactionFilter.CARRIER -> TransactionRole.CARRIER
    TransactionFilter.ALL -> null
}

@Composable
private fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onBack: () -> Unit,
    onRequestRefund: () -> Unit,
    onAddTip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailTopBar(transaction = uiState.transaction, onBack = onBack)
        val transaction = uiState.transaction
        if (transaction == null) {
            EmptyOrError(uiState = uiState)
            return
        }
        Column(
            modifier = Modifier
                .padding(PasabayanSpacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            StatusCard(status = transaction.transactionStatus)
            if (uiState.showEarningsCard || uiState.showAmountBreakdown) {
                AmountSection(transaction = transaction, isCarrierViewer = uiState.showEarningsCard)
            }
            ParticipantsCard(transaction = transaction)
            TimelineCard(timestamps = transaction.timestamps)
            if (uiState.showPayoutStatusCard) {
                PayoutCard(transaction = transaction)
            }
            transaction.refund?.let {
                // RefundInfo on Transaction is the legacy embed; RefundStatusCard expects RefundRequest.
                // For now skip RefundStatusCard here; the dedicated refund-detail surface will use it.
            }
            ActionsSection(
                uiState = uiState,
                onAddTip = onAddTip,
                onRequestRefund = onRequestRefund,
                onCancel = onCancel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(transaction: Transaction?, onBack: () -> Unit) {
    PTopBar(
        title = if (transaction == null) {
            stringResource(R.string.payments_transactions_title)
        } else {
            stringResource(R.string.payments_transaction_detail_title, transaction.id)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            }
        },
    )
}

@Composable
private fun EmptyOrError(uiState: TransactionDetailUiState) {
    Text(
        text = uiState.errorMessage ?: stringResource(R.string.payments_transactions_loading),
        modifier = Modifier.padding(PasabayanSpacing.screenPadding),
        style = PasabayanTextStyles.Body.medium,
        color = if (uiState.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun StatusCard(status: TransactionStatus) {
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_transaction_detail_status),
                style = PasabayanTextStyles.Heading.h5,
            )
            Text(
                text = transactionStatusLabel(status),
                style = PasabayanTextStyles.Body.medium,
                color = transactionStatusColor(status),
            )
        }
    }
}

@Composable
private fun AmountSection(transaction: Transaction, isCarrierViewer: Boolean) {
    val amounts = transaction.amounts ?: return
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = if (isCarrierViewer) {
                stringResource(R.string.payments_transaction_detail_earnings_summary)
            } else {
                stringResource(R.string.payments_transaction_detail_amount_summary)
            },
            style = PasabayanTextStyles.Heading.h5,
        )
        PaymentSummaryCard(source = PaymentSummarySource.Postpayment(amounts))
    }
}

@Composable
private fun ParticipantsCard(transaction: Transaction) {
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_transaction_detail_participants),
                style = PasabayanTextStyles.Heading.h5,
            )
            transaction.deliveryMatchId?.let { id ->
                Text(
                    text = stringResource(R.string.payments_transaction_detail_delivery_match, id),
                    style = PasabayanTextStyles.Body.medium,
                )
            }
            Text(
                text = stringResource(
                    R.string.payments_transaction_detail_shipper,
                    transaction.shipper?.name ?: "-",
                ),
                style = PasabayanTextStyles.Body.small,
            )
            Text(
                text = stringResource(
                    R.string.payments_transaction_detail_carrier,
                    transaction.carrier?.name ?: "-",
                ),
                style = PasabayanTextStyles.Body.small,
            )
        }
    }
}

@Composable
private fun TimelineCard(timestamps: TransactionTimestamps) {
    if (!timestamps.hasAny) return
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.payments_transaction_detail_timeline),
                style = PasabayanTextStyles.Heading.h5,
            )
            timestamps.createdAt.takeIf { it.isNotBlank() }?.let {
                TimelineRow(R.string.payments_transaction_detail_created_at, it)
            }
            timestamps.authorizedAt?.let {
                TimelineRow(R.string.payments_transaction_detail_authorized_at, it)
            }
            timestamps.capturedAt?.let {
                TimelineRow(R.string.payments_transaction_detail_captured_at, it)
            }
            timestamps.completedAt?.let {
                TimelineRow(R.string.payments_transaction_detail_completed_at, it)
            }
            timestamps.failedAt?.let {
                TimelineRow(R.string.payments_transaction_detail_failed_at, it)
            }
            timestamps.payoutCompletedAt?.let {
                TimelineRow(R.string.payments_transaction_detail_payout_completed_at, it)
            }
            timestamps.updatedAt.takeIf { it.isNotBlank() && it != timestamps.createdAt }?.let {
                TimelineRow(R.string.payments_transaction_detail_updated_at, it)
            }
        }
    }
}

@Composable
private fun TimelineRow(@Suppress("ResourceType") stringResId: Int, value: String) {
    Text(
        text = stringResource(stringResId, value),
        style = PasabayanTextStyles.Body.small,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PayoutCard(transaction: Transaction) {
    val status = transaction.effectivePayoutStatus ?: return
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_transaction_detail_payout_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            PayoutStatusBadge(status = status)
            transaction.effectivePayoutNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = stringResource(R.string.payments_transaction_detail_payout_notes, notes),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            transaction.effectivePayoutCompletedAt?.takeIf { it.isNotBlank() }?.let { date ->
                Text(
                    text = stringResource(R.string.payments_transaction_detail_payout_completed_at, date),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ActionsSection(
    uiState: TransactionDetailUiState,
    onAddTip: () -> Unit,
    onRequestRefund: () -> Unit,
    onCancel: () -> Unit,
) {
    val tx = uiState.transaction ?: return
    val canCancel = tx.transactionStatus == TransactionStatus.PENDING && uiState.viewerRole != TransactionRole.CARRIER
    if (!uiState.canAddTip && !uiState.canRequestRefund && !canCancel) return
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.payments_transaction_detail_actions),
                style = PasabayanTextStyles.Heading.h5,
            )
            if (uiState.canAddTip) {
                PButton(
                    text = stringResource(R.string.payments_transaction_detail_add_tip),
                    onClick = onAddTip,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (uiState.canRequestRefund) {
                PButton(
                    text = stringResource(R.string.payments_transaction_detail_request_refund),
                    onClick = onRequestRefund,
                    style = PButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (canCancel) {
                PButton(
                    text = stringResource(R.string.payments_transaction_detail_cancel_payment),
                    onClick = onCancel,
                    style = PButtonStyle.Destructive,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// `transactionStatusLabel` / `transactionStatusColor` live in TransactionHistoryScreen.kt
// and are reused here.

@Preview(showBackground = true, name = "TransactionDetail shipper light")
@Preview(showBackground = true, name = "TransactionDetail shipper dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailShipperPreview() {
    PasabayanTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                viewerRole = TransactionRole.SHIPPER,
                transaction = sampleTransaction(TransactionStatus.COMPLETED),
            ),
            onBack = {}, onRequestRefund = {}, onAddTip = {}, onCancel = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionDetail carrier light")
@Composable
private fun TransactionDetailCarrierPreview() {
    PasabayanTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                viewerRole = TransactionRole.CARRIER,
                transaction = sampleTransaction(
                    status = TransactionStatus.COMPLETED,
                    payoutStatus = com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus.COMPLETED,
                ),
            ),
            onBack = {}, onRequestRefund = {}, onAddTip = {}, onCancel = {},
        )
    }
}

private fun sampleTransaction(
    status: TransactionStatus,
    payoutStatus: com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus? = null,
) = Transaction(
    id = 12,
    transactionStatus = status,
    deliveryMatchId = 77,
    description = "Delivery payment",
    shipper = TransactionUser(id = 1, name = "Alice"),
    carrier = TransactionUser(id = 2, name = "Bob"),
    amounts = TransactionAmounts(
        total = 165.0, subtotal = 150.0, platformFee = 15.0,
        carrierReceives = 142.5, tip = 0.0, baseAmount = 150.0,
    ),
    timestamps = TransactionTimestamps(
        createdAt = "2026-03-30T10:00:00Z",
        updatedAt = "2026-03-30T10:05:00Z",
        capturedAt = "2026-03-30T10:02:00Z",
        completedAt = "2026-03-30T10:05:00Z",
    ),
    payoutStatus = payoutStatus,
)
