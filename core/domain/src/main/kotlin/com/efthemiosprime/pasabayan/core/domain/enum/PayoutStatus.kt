package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PayoutStatus {
    @SerialName("pending") PENDING,
    @SerialName("processing") PROCESSING,
    @SerialName("on_hold") ON_HOLD,
    @SerialName("scheduled") SCHEDULED,
    @SerialName("completed") COMPLETED,
    @SerialName("failed") FAILED,
}
