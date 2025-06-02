package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip model representing a carrier's delivery trip
 * Mirrors iOS Trip model structure
 */
@Serializable
data class Trip(
    val id: Int,
    @SerialName("carrier_id")
    val carrierId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("origin_location")
    val originLocation: String,
    @SerialName("destination_location")
    val destinationLocation: String,
    @SerialName("origin_coordinates")
    val originCoordinates: Coordinates? = null,
    @SerialName("destination_coordinates")
    val destinationCoordinates: Coordinates? = null,
    @SerialName("departure_date")
    val departureDate: String,
    @SerialName("departure_time")
    val departureTime: String? = null,
    @SerialName("arrival_date")
    val arrivalDate: String? = null,
    @SerialName("arrival_time")
    val arrivalTime: String? = null,
    @SerialName("vehicle_type")
    val vehicleType: VehicleType,
    @SerialName("max_weight")
    val maxWeight: Double? = null,
    @SerialName("max_volume")
    val maxVolume: Double? = null,
    @SerialName("available_space")
    val availableSpace: Double? = null,
    @SerialName("price_per_kg")
    val pricePerKg: Double? = null,
    @SerialName("base_price")
    val basePrice: Double? = null,
    val status: TripStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val carrier: User? = null,
    @SerialName("available_bookings_count")
    val availableBookingsCount: Int? = null
)

/**
 * Vehicle type enumeration
 */
@Serializable
enum class VehicleType {
    @SerialName("car")
    CAR,
    @SerialName("motorcycle")
    MOTORCYCLE,
    @SerialName("van")
    VAN,
    @SerialName("truck")
    TRUCK,
    @SerialName("bicycle")
    BICYCLE;
    
    val displayName: String
        get() = when (this) {
            CAR -> "Car"
            MOTORCYCLE -> "Motorcycle"
            VAN -> "Van"
            TRUCK -> "Truck"
            BICYCLE -> "Bicycle"
        }
    
    val description: String
        get() = when (this) {
            CAR -> "Standard car - medium capacity"
            MOTORCYCLE -> "Motorcycle - small packages only"
            VAN -> "Van - large capacity"
            TRUCK -> "Truck - extra large capacity"
            BICYCLE -> "Bicycle - small packages only"
        }
    
    val icon: String
        get() = when (this) {
            CAR -> "🚗"
            MOTORCYCLE -> "🏍️"
            VAN -> "🚐"
            TRUCK -> "🚛"
            BICYCLE -> "🚲"
        }
}

/**
 * Trip status enumeration
 */
@Serializable
enum class TripStatus {
    @SerialName("planned")
    PLANNED,
    @SerialName("active")
    ACTIVE,
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("completed")
    COMPLETED,
    @SerialName("cancelled")
    CANCELLED;
    
    val displayName: String
        get() = when (this) {
            PLANNED -> "Planned"
            ACTIVE -> "Active"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            PLANNED -> "blue"
            ACTIVE -> "green"
            IN_PROGRESS -> "orange"
            COMPLETED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            PLANNED -> "📅"
            ACTIVE -> "🚛"
            IN_PROGRESS -> "⏰"
            COMPLETED -> "✅"
            CANCELLED -> "❌"
        }
}

/**
 * Create trip request model
 */
@Serializable
data class CreateTripRequest(
    val title: String,
    val description: String? = null,
    @SerialName("origin_location")
    val originLocation: String,
    @SerialName("destination_location")
    val destinationLocation: String,
    @SerialName("origin_coordinates")
    val originCoordinates: Coordinates? = null,
    @SerialName("destination_coordinates")
    val destinationCoordinates: Coordinates? = null,
    @SerialName("departure_date")
    val departureDate: String,
    @SerialName("departure_time")
    val departureTime: String? = null,
    @SerialName("arrival_date")
    val arrivalDate: String? = null,
    @SerialName("arrival_time")
    val arrivalTime: String? = null,
    @SerialName("vehicle_type")
    val vehicleType: VehicleType,
    @SerialName("max_weight")
    val maxWeight: Double? = null,
    @SerialName("max_volume")
    val maxVolume: Double? = null,
    @SerialName("available_space")
    val availableSpace: Double? = null,
    @SerialName("price_per_kg")
    val pricePerKg: Double? = null,
    @SerialName("base_price")
    val basePrice: Double? = null
)

/**
 * Trip response model
 */
@Serializable
data class TripResponse(
    val success: Boolean,
    val message: String,
    val data: Trip
)

/**
 * Trips list response model
 */
@Serializable
data class TripsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Trip>
) 