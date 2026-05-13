package com.efthemiosprime.pasabayan.features.payments.model

data class StripeInfo(
    val paymentIntentId: String? = null,
    val transferId: String? = null,
    val chargeId: String? = null,
    val refundId: String? = null,
)
