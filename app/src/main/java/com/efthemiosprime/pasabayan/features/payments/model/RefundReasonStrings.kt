package com.efthemiosprime.pasabayan.features.payments.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason

/**
 * Localized display label for a [RefundReason] preset.
 *
 * The wire / server contract still uses [RefundReason.displayText] (English) — that field
 * is consumed by [PaymentMappers] (to recover the enum from a server-sent string) and by
 * [RefundViewModel] (to send the request body). Don't replace those call sites with this
 * helper; only use it for UI rendering.
 */
@Composable
fun refundReasonDisplayText(reason: RefundReason): String = stringResource(
    when (reason) {
        RefundReason.DAMAGED -> R.string.payments_refund_reason_damaged
        RefundReason.NOT_DELIVERED -> R.string.payments_refund_reason_not_delivered
        RefundReason.WRONG_ITEM -> R.string.payments_refund_reason_wrong_item
        RefundReason.LATE_DELIVERY -> R.string.payments_refund_reason_late_delivery
        RefundReason.PARTIAL_DELIVERY -> R.string.payments_refund_reason_partial_delivery
        RefundReason.OTHER -> R.string.payments_refund_reason_other
    },
)
