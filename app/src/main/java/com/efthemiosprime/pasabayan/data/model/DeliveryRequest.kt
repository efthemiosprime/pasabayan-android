package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeliveryRequest(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("item_description") val itemDescription: String,
    @SerialName("item_weight") val itemWeight: Double? = null,
    @SerialName("item_dimensions") val itemDimensions: String? = null,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("pickup_latitude") val pickupLatitude: Double,
    @SerialName("pickup_longitude") val pickupLongitude: Double,
    @SerialName("delivery_address") val deliveryAddress: String,
    @SerialName("delivery_latitude") val deliveryLatitude: Double,
    @SerialName("delivery_longitude") val deliveryLongitude: Double,
    @SerialName("pickup_contact_name") val pickupContactName: String,
    @SerialName("pickup_contact_phone") val pickupContactPhone: String,
    @SerialName("delivery_contact_name") val deliveryContactName: String,
    @SerialName("delivery_contact_phone") val deliveryContactPhone: String,
    @SerialName("requested_pickup_time") val requestedPickupTime: String? = null,
    @SerialName("requested_delivery_time") val requestedDeliveryTime: String? = null,
    @SerialName("special_instructions") val specialInstructions: String? = null,
    @SerialName("delivery_fee") val deliveryFee: Double,
    val status: DeliveryStatus,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("picked_up_at") val pickedUpAt: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null
)

@Serializable
enum class DeliveryStatus(val value: String) {
    @SerialName("pending")
    PENDING("pending"),
    
    @SerialName("accepted")
    ACCEPTED("accepted"),
    
    @SerialName("picked_up")
    PICKED_UP("picked_up"),
    
    @SerialName("in_transit")
    IN_TRANSIT("in_transit"),
    
    @SerialName("delivered")
    DELIVERED("delivered"),
    
    @SerialName("cancelled")
    CANCELLED("cancelled");

    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            ACCEPTED -> "Accepted"
            PICKED_UP -> "Picked Up"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
}

@Serializable
data class CreateDeliveryRequest(
    @SerialName("item_description") val itemDescription: String,
    @SerialName("item_weight") val itemWeight: Double? = null,
    @SerialName("item_dimensions") val itemDimensions: String? = null,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("pickup_latitude") val pickupLatitude: Double,
    @SerialName("pickup_longitude") val pickupLongitude: Double,
    @SerialName("delivery_address") val deliveryAddress: String,
    @SerialName("delivery_latitude") val deliveryLatitude: Double,
    @SerialName("delivery_longitude") val deliveryLongitude: Double,
    @SerialName("pickup_contact_name") val pickupContactName: String,
    @SerialName("pickup_contact_phone") val pickupContactPhone: String,
    @SerialName("delivery_contact_name") val deliveryContactName: String,
    @SerialName("delivery_contact_phone") val deliveryContactPhone: String,
    @SerialName("requested_pickup_time") val requestedPickupTime: String? = null,
    @SerialName("requested_delivery_time") val requestedDeliveryTime: String? = null,
    @SerialName("special_instructions") val specialInstructions: String? = null,
    @SerialName("delivery_fee") val deliveryFee: Double
)

@Serializable
data class DeliveryRequestsResponse(
    val data: List<DeliveryRequest>
)

@Serializable
data class DeliveryRequestResponse(
    val data: DeliveryRequest
) 