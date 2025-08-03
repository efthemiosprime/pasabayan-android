package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for shipper trip requests
 * Matches iOS ShipperTripRequest structure
 * Used with POST /api/packages/{id}/request-trip/{trip_id}
 */
@Serializable
data class ShipperTripRequest(
    @SerialName("offered_price")
    val offeredPrice: Double,
    val message: String
)