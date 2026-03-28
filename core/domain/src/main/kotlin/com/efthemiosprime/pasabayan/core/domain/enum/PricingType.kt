package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PricingType {
    @SerialName("per_kg") PER_KG,
    @SerialName("flat") FLAT,
}
