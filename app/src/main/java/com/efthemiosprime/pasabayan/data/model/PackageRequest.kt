package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Package request model representing a delivery request
 * Mirrors iOS PackageRequest model structure
 */
@Serializable
data class PackageRequest(
    val id: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("pickup_location")
    val pickupLocation: String,
    @SerialName("delivery_location")
    val deliveryLocation: String,
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("preferred_pickup_date")
    val preferredPickupDate: String,
    @SerialName("preferred_pickup_time")
    val preferredPickupTime: String? = null,
    @SerialName("preferred_delivery_date")
    val preferredDeliveryDate: String? = null,
    @SerialName("package_size")
    val packageSize: PackageSize,
    @SerialName("package_weight")
    val packageWeight: Double? = null,
    @SerialName("package_value")
    val packageValue: Double? = null,
    @SerialName("is_fragile")
    val isFragile: Boolean = false,
    @SerialName("special_instructions")
    val specialInstructions: String? = null,
    @SerialName("max_budget")
    val maxBudget: Double? = null,
    val status: PackageRequestStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val shipper: User? = null,
    @SerialName("compatible_trips_count")
    val compatibleTripsCount: Int? = null
)

/**
 * Coordinates model for location data
 */
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Package size enumeration
 */
@Serializable
enum class PackageSize {
    @SerialName("small")
    SMALL,
    @SerialName("medium")
    MEDIUM,
    @SerialName("large")
    LARGE,
    @SerialName("extra_large")
    EXTRA_LARGE;
    
    val displayName: String
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
            EXTRA_LARGE -> "Extra Large"
        }
    
    val description: String
        get() = when (this) {
            SMALL -> "Up to 5kg - fits in a bag"
            MEDIUM -> "5-15kg - medium box"
            LARGE -> "15-30kg - large box"
            EXTRA_LARGE -> "30kg+ - requires special handling"
        }
    
    val icon: String
        get() = when (this) {
            SMALL -> "📦"
            MEDIUM -> "📦"
            LARGE -> "📦"
            EXTRA_LARGE -> "📦"
        }
}

/**
 * Package request status enumeration
 */
@Serializable
enum class PackageRequestStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("matched")
    MATCHED,
    @SerialName("booked")
    BOOKED,
    @SerialName("in_transit")
    IN_TRANSIT,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED;
    
    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            MATCHED -> "Matched"
            BOOKED -> "Booked"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            PENDING -> "orange"
            MATCHED -> "blue"
            BOOKED -> "purple"
            IN_TRANSIT -> "green"
            DELIVERED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            PENDING -> "⏳"
            MATCHED -> "🔗"
            BOOKED -> "✅"
            IN_TRANSIT -> "🚛"
            DELIVERED -> "✅"
            CANCELLED -> "❌"
        }
}

/**
 * Create package request model
 */
@Serializable
data class CreatePackageRequest(
    val title: String,
    val description: String? = null,
    @SerialName("pickup_location")
    val pickupLocation: String,
    @SerialName("delivery_location")
    val deliveryLocation: String,
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("preferred_pickup_date")
    val preferredPickupDate: String,
    @SerialName("preferred_pickup_time")
    val preferredPickupTime: String? = null,
    @SerialName("preferred_delivery_date")
    val preferredDeliveryDate: String? = null,
    @SerialName("package_size")
    val packageSize: PackageSize,
    @SerialName("package_weight")
    val packageWeight: Double? = null,
    @SerialName("package_value")
    val packageValue: Double? = null,
    @SerialName("is_fragile")
    val isFragile: Boolean = false,
    @SerialName("special_instructions")
    val specialInstructions: String? = null,
    @SerialName("max_budget")
    val maxBudget: Double? = null
)

/**
 * Compatible trip model for matching trips with package requests
 */
@Serializable
data class CompatibleTrip(
    val trip: Trip,
    @SerialName("match_score")
    val matchScore: Double,
    @SerialName("estimated_price")
    val estimatedPrice: Double,
    @SerialName("compatibility_reasons")
    val compatibilityReasons: List<String> = emptyList()
) {
    val id: Int get() = trip.id
}

/**
 * Package request response model
 */
@Serializable
data class PackageRequestResponse(
    val success: Boolean,
    val message: String,
    val data: PackageRequest
)

/**
 * Package requests list response model
 */
@Serializable
data class PackageRequestsResponse(
    val success: Boolean,
    val message: String,
    val data: List<PackageRequest>
)

/**
 * Compatible trips response model
 */
@Serializable
data class CompatibleTripsResponse(
    val success: Boolean,
    val message: String,
    val data: List<CompatibleTrip>
) 