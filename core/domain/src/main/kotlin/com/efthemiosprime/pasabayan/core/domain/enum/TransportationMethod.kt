package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransportationMethod {
    @SerialName("none") NONE,
    @SerialName("flight") FLIGHT,
    @SerialName("bus") BUS,
    @SerialName("car") CAR,
    @SerialName("truck") TRUCK,
    @SerialName("van") VAN,
    @SerialName("motorcycle") MOTORCYCLE,
    @SerialName("ship") SHIP,
    @SerialName("train") TRAIN,
    @SerialName("other") OTHER;

    val isLandTransport: Boolean
        get() = when (this) {
            BUS, CAR, TRUCK, VAN, MOTORCYCLE, TRAIN -> true
            NONE, FLIGHT, SHIP, OTHER -> false
        }

    val defaultPricingType: PricingType
        get() = if (isLandTransport) PricingType.FLAT else PricingType.PER_KG

    val icon: String
        get() = when (this) {
            NONE -> ""
            FLIGHT -> "✈️"
            BUS -> "🚌"
            CAR -> "🚗"
            TRUCK -> "🚛"
            VAN -> "🚐"
            MOTORCYCLE -> "🏍️"
            SHIP -> "🚢"
            TRAIN -> "🚂"
            OTHER -> "📦"
        }
}
