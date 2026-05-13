package com.efthemiosprime.pasabayan.features.payments.model

data class RefundInfo(
    val amount: Double? = null,
    val reason: String? = null,
    val refundedAt: String? = null,
)
