package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionStatus {
    @SerialName("pending") PENDING,
    @SerialName("authorized") AUTHORIZED,
    @SerialName("captured") CAPTURED,
    @SerialName("completed") COMPLETED,
    @SerialName("refunded") REFUNDED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("failed") FAILED,
    @SerialName("unknown") UNKNOWN;

    val isTerminal: Boolean
        get() = this in listOf(COMPLETED, REFUNDED, CANCELLED, FAILED)
}
