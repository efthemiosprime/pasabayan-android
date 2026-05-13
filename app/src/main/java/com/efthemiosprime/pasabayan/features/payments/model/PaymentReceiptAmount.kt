package com.efthemiosprime.pasabayan.features.payments.model

data class PaymentReceiptAmount(
    val total: Double,
    val carrierAmount: Double? = null,
    val platformFee: Double? = null,
    val tip: Double? = null,
    val currency: String = "cad",
)
