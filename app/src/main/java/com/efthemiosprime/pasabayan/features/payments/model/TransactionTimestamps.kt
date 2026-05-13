package com.efthemiosprime.pasabayan.features.payments.model

data class TransactionTimestamps(
    val createdAt: String = "",
    val updatedAt: String = createdAt,
    val authorizedAt: String? = null,
    val capturedAt: String? = null,
    val completedAt: String? = null,
    val failedAt: String? = null,
    val payoutCompletedAt: String? = null,
) {
    val hasAny: Boolean
        get() = createdAt.isNotEmpty() ||
            updatedAt.isNotEmpty() ||
            authorizedAt != null ||
            capturedAt != null ||
            completedAt != null ||
            failedAt != null ||
            payoutCompletedAt != null
}
