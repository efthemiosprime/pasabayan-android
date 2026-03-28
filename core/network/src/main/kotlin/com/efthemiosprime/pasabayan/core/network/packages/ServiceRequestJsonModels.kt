package com.efthemiosprime.pasabayan.core.network.packages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateServiceRequestBodyJson(
    @SerialName("service_type") val serviceType: String,
    @SerialName("shopping_list") val shoppingList: List<ShoppingItemJson>,
    @SerialName("delivery_city") val deliveryCity: String,
    @SerialName("delivery_address") val deliveryAddress: String,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("store_address") val storeAddress: String? = null,
    @SerialName("store_lat") val storeLat: Double? = null,
    @SerialName("store_lng") val storeLng: Double? = null,
    @SerialName("estimated_cost") val estimatedCost: Double? = null,
    @SerialName("max_price_budget") val maxPriceBudget: Double? = null,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String? = null,
    @SerialName("urgency_level") val urgencyLevel: String? = null,
    @SerialName("delivery_lat") val deliveryLat: Double? = null,
    @SerialName("delivery_lng") val deliveryLng: Double? = null,
    val direction: String? = null,
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("recipient_phone") val recipientPhone: String? = null,
)

@Serializable
data class ShoppingItemJson(
    val item: String,
    val quantity: String = "",
    val notes: String? = null,
)
