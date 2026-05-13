package com.efthemiosprime.pasabayan.features.payments.components

import com.efthemiosprime.pasabayan.features.payments.model.StripeConfig
import com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts

/**
 * Source for [PaymentSummaryCard]. Two modes share the same row layout:
 * - [Prepayment] computes fees from [StripeConfig] given a base amount (pre-charge UI)
 * - [Postpayment] reads server-confirmed values from [TransactionAmounts] (post-charge UI)
 */
sealed interface PaymentSummarySource {
    data class Prepayment(val baseAmount: Double, val config: StripeConfig) : PaymentSummarySource
    data class Postpayment(val amounts: TransactionAmounts) : PaymentSummarySource
}

/** Pure data for the summary rows. JVM-testable; the composable formats with [MoneyFormatter]. */
data class PaymentSummaryComputation(
    val baseAmount: Double?,
    val serviceFee: Double,
    val total: Double,
    val carrierReceives: Double,
    val currency: String,
)

fun PaymentSummarySource.compute(): PaymentSummaryComputation = when (this) {
    is PaymentSummarySource.Prepayment -> PaymentSummaryComputation(
        baseAmount = baseAmount,
        serviceFee = config.serviceFeeAmount(baseAmount),
        total = config.totalToPay(baseAmount),
        carrierReceives = config.carrierReceives(baseAmount),
        currency = config.currency,
    )
    is PaymentSummarySource.Postpayment -> PaymentSummaryComputation(
        baseAmount = amounts.baseAmount ?: amounts.subtotal,
        serviceFee = amounts.platformFee,
        total = amounts.total,
        carrierReceives = amounts.carrierReceives,
        currency = amounts.currency,
    )
}
