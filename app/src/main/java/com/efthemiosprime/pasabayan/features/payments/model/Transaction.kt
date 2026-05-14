package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus
import java.util.Locale
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import kotlinx.serialization.json.JsonElement

data class Transaction(
    val id: Int,
    val shipper: TransactionUser? = null,
    val carrier: TransactionUser? = null,
    val deliveryMatchId: Int? = null,
    val amounts: TransactionAmounts? = null,
    val stripe: StripeInfo? = null,
    val transactionStatus: TransactionStatus = TransactionStatus.UNKNOWN,
    val description: String? = null,
    val metadata: Map<String, JsonElement>? = null,
    val refund: RefundInfo? = null,
    val timestamps: TransactionTimestamps = TransactionTimestamps(),
    val clientSecret: String? = null,
    val payoutStatus: PayoutStatus? = null,
    val payoutNotes: String? = null,
    val payoutCompletedAt: String? = null,
    val payout: Payout? = null,
    val tip: TipInfo? = null,
    val customerId: String? = null,
    val ephemeralKey: String? = null,
) {
    val isTerminal: Boolean get() = transactionStatus.isTerminal

    val effectivePayoutStatus: PayoutStatus?
        get() = payout?.status ?: payoutStatus

    val effectivePayoutNotes: String?
        get() = payout?.notes ?: payoutNotes

    val effectivePayoutCompletedAt: String?
        get() = payout?.completedAt ?: payoutCompletedAt ?: timestamps.payoutCompletedAt

    val totalAmount: Double get() = amounts?.total ?: 0.0
    val currency: String get() = amounts?.currency ?: "cad"

    val formattedTotal: String
        get() = String.format(Locale.ROOT, "$%.2f %s", totalAmount, currency.uppercase())
}
