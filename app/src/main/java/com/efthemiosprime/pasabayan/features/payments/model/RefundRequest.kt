package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus

data class RefundRequest(
    val id: Int,
    val transactionId: Int,
    val status: RefundStatus,
    val amount: Double? = null,
    val reason: RefundReason? = null,
    /** Raw `reason` text from the server — preserved for OTHER/custom values that don't map to a preset enum. */
    val reasonText: String? = null,
    val description: String? = null,
    val adminNotes: String? = null,
    val reviewedAt: String? = null,
    val processedAt: String? = null,
    val createdAt: String? = null,
) {
    val isFullRefund: Boolean get() = amount == null
}
