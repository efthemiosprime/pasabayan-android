package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InitiatedBy {
    @SerialName("shipper") SHIPPER,
    @SerialName("carrier") CARRIER,
    @SerialName("unknown") UNKNOWN,
}
