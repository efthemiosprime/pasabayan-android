package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceType {
    @SerialName("delivery") DELIVERY,
    @SerialName("grocery_shopping") GROCERY_SHOPPING,
    @SerialName("food_delivery") FOOD_DELIVERY,
    @SerialName("pharmacy_pickup") PHARMACY_PICKUP,
    @SerialName("general_errand") GENERAL_ERRAND;

    /** All non-delivery service types require car or motorcycle. */
    val requiresCarOrMotorcycle: Boolean
        get() = this != DELIVERY
}
