package com.efthemiosprime.pasabayan.features.bookings.model.nested

/**
 * Result of a refund attempt tied to a match cancellation.
 * Parity with iOS `RefundResult` from MatchingModels.swift.
 */
data class RefundResult(
    val processed: Boolean,
    val amount: Double?,
    val transactionId: Int?,
    val error: String?,
)
