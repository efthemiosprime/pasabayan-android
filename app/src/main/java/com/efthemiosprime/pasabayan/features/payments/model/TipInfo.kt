package com.efthemiosprime.pasabayan.features.payments.model

data class TipInfo(
    val amount: Double,
    val paidAt: String? = null,
    val stripePaymentIntentId: String? = null,
)
