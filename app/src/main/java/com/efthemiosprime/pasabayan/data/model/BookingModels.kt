package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Booking Type Enumeration - EXACTLY matching iOS BookingType
 */
@Serializable
enum class BookingType {
    @SerialName("space_only")
    SPACE_ONLY,
    @SerialName("full_service")
    FULL_SERVICE,
    @SerialName("passenger")
    PASSENGER;
    
    val displayName: String
        get() = when (this) {
            SPACE_ONLY -> "Space Only"
            FULL_SERVICE -> "Full Service"
            PASSENGER -> "Passenger"
        }
    
    val description: String
        get() = when (this) {
            SPACE_ONLY -> "Book cargo space only"
            FULL_SERVICE -> "Full service with pickup & delivery"
            PASSENGER -> "Passenger transportation"
        }
    
    val icon: String
        get() = when (this) {
            SPACE_ONLY -> "📦"
            FULL_SERVICE -> "🚛"
            PASSENGER -> "👥"
        }
}

/**
 * Booking Validation Result (still needed for BrowseTripsViewModel)
 */
sealed class BookingValidationResult {
    object Valid : BookingValidationResult()
    data class Invalid(val errors: List<String>) : BookingValidationResult()
}

// Note: BookingStatus enum is already defined in Booking.kt
// We'll use the existing enum which includes: PENDING, CONFIRMED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED

/**
 * Direct Booking Request - EXACTLY matching iOS DirectBookingRequest
 */
@Serializable
data class DirectBookingRequest(
    @SerialName("booking_type")
    val bookingType: BookingType,
    @SerialName("space_needed_liters")
    val spaceNeededLiters: Double? = null,
    @SerialName("weight_needed_kg")
    val weightNeededKg: Double? = null,
    @SerialName("passenger_count")
    val passengerCount: Int? = null,
    @SerialName("pickup_location")
    val pickupLocation: String? = null,
    @SerialName("delivery_location")
    val deliveryLocation: String? = null,
    @SerialName("pickup_lat")
    val pickupLat: Double? = null,
    @SerialName("pickup_lng")
    val pickupLng: Double? = null,
    @SerialName("delivery_lat")
    val deliveryLat: Double? = null,
    @SerialName("delivery_lng")
    val deliveryLng: Double? = null,
    @SerialName("price_agreed")
    val priceAgreed: Double,
    @SerialName("service_fee")
    val serviceFee: Double? = null,
    @SerialName("special_requirements")
    val specialRequirements: String? = null
)

/**
 * Direct Booking Response - EXACTLY matching iOS DirectBookingResponse
 */
@Serializable
data class DirectBookingResponse(
    val success: Boolean,
    val message: String,
    val data: DirectBookingData
)

/**
 * Direct Booking Data - EXACTLY matching iOS DirectBookingData
 */
@Serializable
data class DirectBookingData(
    @SerialName("booking_id")
    val bookingId: Int,
    @SerialName("trip_id")
    val tripId: Int,
    val status: String,
    @SerialName("price_agreed")
    val priceAgreed: Double,
    @SerialName("service_fee")
    val serviceFee: Double? = null,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("booking_reference")
    val bookingReference: String,
    @SerialName("estimated_pickup_time")
    val estimatedPickupTime: String? = null,
    @SerialName("estimated_delivery_time")
    val estimatedDeliveryTime: String? = null
)

 