package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookingType {
    @SerialName("space_only") SPACE_ONLY,
    @SerialName("full_service") FULL_SERVICE,
    @SerialName("passenger") PASSENGER,
}
