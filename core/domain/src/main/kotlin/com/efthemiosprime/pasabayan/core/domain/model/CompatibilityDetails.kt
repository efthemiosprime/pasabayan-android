package com.efthemiosprime.pasabayan.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompatibilityDetails(
    @SerialName("route_match") val routeMatch: String? = null,
    @SerialName("capacity_sufficient") val capacitySufficient: Boolean = false,
    @SerialName("date_compatible") val dateCompatible: Boolean = false,
    @SerialName("price_compatible") val priceCompatible: Boolean = false,
    @SerialName("weight_usage_percentage") val weightUsagePercentage: Double = 0.0,
    @SerialName("space_usage_percentage") val spaceUsagePercentage: Double = 0.0,
)
