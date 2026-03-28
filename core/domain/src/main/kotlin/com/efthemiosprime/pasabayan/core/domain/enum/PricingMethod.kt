package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PricingMethod {
    @SerialName("calculated") CALCULATED,
    @SerialName("manual") MANUAL,
    @SerialName("per_kg") PER_KG,
}
