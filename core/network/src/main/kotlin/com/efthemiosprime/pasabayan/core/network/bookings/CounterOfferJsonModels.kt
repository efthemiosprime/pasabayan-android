package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShipperCounterOfferRequestJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = true,
)

@Serializable
data class CarrierCounterOfferRequestJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = true,
)

@Serializable
data class CounterOfferResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)
