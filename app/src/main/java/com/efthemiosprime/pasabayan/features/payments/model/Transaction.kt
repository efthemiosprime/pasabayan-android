package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus

data class Transaction(
    val id: Int,
    val transactionStatus: TransactionStatus,
    val deliveryMatchId: Int?,
    val description: String?,
    val shipperName: String?,
    val carrierName: String?,
    val totalAmount: Double,
    val subtotal: Double?,
    val platformFee: Double?,
    val carrierReceives: Double?,
    val currency: String,
    val tipAmount: Double?,
    val clientSecret: String?,
    val customerId: String?,
    val ephemeralKey: String?,
    val payoutStatus: String?,
    val createdAt: String?,
    val updatedAt: String?,
) {
    val isTerminal: Boolean get() = transactionStatus.isTerminal

    val formattedTotal: String
        get() = String.format("$%.2f %s", totalAmount, currency.uppercase())
}
