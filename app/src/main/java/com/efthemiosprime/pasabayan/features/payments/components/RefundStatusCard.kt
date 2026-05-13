package com.efthemiosprime.pasabayan.features.payments.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeVariant
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus
import com.efthemiosprime.pasabayan.features.payments.model.MoneyFormatter
import com.efthemiosprime.pasabayan.features.payments.model.RefundRequest
import com.efthemiosprime.pasabayan.features.payments.model.refundReasonDisplayText

/**
 * Refund detail card: header (title + status badge), amount, reason, admin notes,
 * submission date, status message. Status messages mirror iOS copy per spec §1255.
 */
@Composable
fun RefundStatusCard(
    request: RefundRequest,
    modifier: Modifier = Modifier,
    currency: String = MoneyFormatter.DEFAULT_CURRENCY,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.payments_refund_status_title),
                    style = PasabayanTextStyles.Heading.h5,
                    modifier = Modifier.weight(1f),
                )
                RefundStatusBadge(status = request.status, variant = StatusBadgeVariant.Compact)
            }

            PDetailRow(
                label = stringResource(R.string.payments_refund_status_amount_label),
                value = if (request.isFullRefund) {
                    stringResource(R.string.payments_refund_status_amount_full)
                } else {
                    MoneyFormatter.formatCurrency(request.amount ?: 0.0, currency)
                },
            )

            PDetailRow(
                label = stringResource(R.string.payments_refund_status_reason_label),
                value = reasonDisplay(request),
            )

            request.adminNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                PDetailRow(
                    label = stringResource(R.string.payments_refund_status_admin_notes_label),
                    value = notes,
                )
            }

            request.createdAt?.takeIf { it.isNotBlank() }?.let { date ->
                PDetailRow(
                    label = stringResource(R.string.payments_refund_status_submitted_label),
                    value = date,
                )
            }

            Text(
                text = statusMessage(request.status),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = PasabayanSpacing.xs),
            )
        }
    }
}

@Composable
private fun reasonDisplay(request: RefundRequest): String {
    val preset = request.reason?.let { refundReasonDisplayText(it) }
    return preset
        ?: request.reasonText?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.payments_refund_status_reason_unspecified)
}

@Composable
private fun statusMessage(status: RefundStatus): String = stringResource(
    when (status) {
        RefundStatus.PENDING -> R.string.payments_refund_status_message_pending
        RefundStatus.APPROVED -> R.string.payments_refund_status_message_approved
        RefundStatus.REJECTED -> R.string.payments_refund_status_message_rejected
        RefundStatus.PROCESSED -> R.string.payments_refund_status_message_processed
    },
)

private val pendingFull = RefundRequest(
    id = 1, transactionId = 100, status = RefundStatus.PENDING,
    amount = null,
    reason = com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason.DAMAGED,
    createdAt = "2026-05-12T10:00:00Z",
)

private val approvedPartial = RefundRequest(
    id = 2, transactionId = 100, status = RefundStatus.APPROVED,
    amount = 25.50,
    reason = com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason.LATE_DELIVERY,
    adminNotes = "Approved per Tier-2 policy.",
    createdAt = "2026-05-11T14:30:00Z",
)

private val rejectedWithNotes = RefundRequest(
    id = 3, transactionId = 100, status = RefundStatus.REJECTED,
    amount = null,
    reasonText = "Driver took longer than expected",
    adminNotes = "Delivery completed within 15 minutes of agreed window.",
    createdAt = "2026-05-10T09:00:00Z",
)

private val processedFull = RefundRequest(
    id = 4, transactionId = 100, status = RefundStatus.PROCESSED,
    amount = null,
    reason = com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason.NOT_DELIVERED,
    createdAt = "2026-05-08T18:45:00Z",
)

@Preview(showBackground = true, name = "RefundStatusCard light")
@Preview(showBackground = true, name = "RefundStatusCard dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RefundStatusCardPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            RefundStatusCard(request = pendingFull)
            RefundStatusCard(request = approvedPartial)
            RefundStatusCard(request = rejectedWithNotes)
            RefundStatusCard(request = processedFull)
        }
    }
}
