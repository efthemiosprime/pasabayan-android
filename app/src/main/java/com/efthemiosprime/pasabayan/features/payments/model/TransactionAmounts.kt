package com.efthemiosprime.pasabayan.features.payments.model

import kotlin.math.max

data class TransactionAmounts(
    val total: Double,
    val subtotal: Double? = null,
    val platformFee: Double = 0.0,
    val carrierReceives: Double = 0.0,
    val currency: String = "cad",
    val tip: Double = 0.0,
    val tax: Double? = null,
    val taxBreakdown: String? = null,
    val carrierTotal: Double = carrierReceives + tip,
    val baseAmount: Double? = null,
) {
    val hasTwoSidedBreakdown: Boolean get() = baseAmount != null

    val senderFee: Double
        get() {
            val base = baseAmount ?: return 0.0
            return max(0.0, total - base - (tax ?: 0.0))
        }

    val carrierFee: Double
        get() {
            val base = baseAmount ?: return 0.0
            return max(0.0, base - carrierReceives)
        }
}
