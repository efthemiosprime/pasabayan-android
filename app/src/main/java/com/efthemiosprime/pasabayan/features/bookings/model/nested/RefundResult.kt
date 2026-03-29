package com.efthemiosprime.pasabayan.features.bookings.model.nested

data class RefundResult(
    val refunded: Boolean = false,
    val amount: Double? = null,
    val currency: String? = null,
    val reason: String? = null,
)
