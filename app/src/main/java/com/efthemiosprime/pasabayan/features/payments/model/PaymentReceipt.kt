package com.efthemiosprime.pasabayan.features.payments.model

import android.net.Uri

data class PaymentReceipt(
    val id: Int,
    val receiptNumber: String,
    val receiptUrl: String? = null,
    val date: String,
    val dateFormatted: String? = null,
    val role: String,
    val otherParty: OtherParty? = null,
    val amount: PaymentReceiptAmount,
    val delivery: PaymentReceiptDelivery? = null,
    val status: String,
) {
    val isShipper: Boolean get() = role == "shipper"

    val url: Uri?
        get() = receiptUrl?.let { runCatching { Uri.parse(it) }.getOrNull() }

    /** iOS-parity: shipper sees what they paid; carrier sees what they earned (incl. tip). */
    val displayAmountValue: Double
        get() = if (isShipper) {
            amount.total
        } else {
            (amount.carrierAmount ?: 0.0) + (amount.tip ?: 0.0)
        }

    val displayAmount: String
        get() = String.format("$%.2f %s", displayAmountValue, amount.currency.uppercase())

    val routeDescription: String
        get() {
            val pickup = delivery?.pickupCity
            val drop = delivery?.deliveryCity
            if (pickup != null && drop != null) return "$pickup → $drop"
            return otherParty?.name ?: ""
        }
}
