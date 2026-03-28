package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TripStatus {
    @SerialName("planning") PLANNING,
    @SerialName("active") ACTIVE,
    @SerialName("in_transit") IN_TRANSIT,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED,
}
