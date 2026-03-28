package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PackageRequestStatus {
    @SerialName("open") OPEN,
    @SerialName("pending_request") PENDING_REQUEST,
    @SerialName("matched") MATCHED,
    @SerialName("picked_up") PICKED_UP,
    @SerialName("delivered") DELIVERED,
    @SerialName("cancelled") CANCELLED,

    // Legacy values for backward compatibility
    @SerialName("pending") PENDING,
    @SerialName("booked") BOOKED,
    @SerialName("in_transit") IN_TRANSIT,
}
