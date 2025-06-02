package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Booking model representing a confirmed delivery booking
 * Mirrors iOS Booking model structure
 */
@Serializable
data class Booking(
    val id: Int,
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    @SerialName("carrier_id")
    val carrierId: Int,
    @SerialName("agreed_price")
    val agreedPrice: Double,
    @SerialName("pickup_time")
    val pickupTime: String? = null,
    @SerialName("delivery_time")
    val deliveryTime: String? = null,
    @SerialName("pickup_confirmation")
    val pickupConfirmation: String? = null,
    @SerialName("delivery_confirmation")
    val deliveryConfirmation: String? = null,
    @SerialName("tracking_number")
    val trackingNumber: String? = null,
    val notes: String? = null,
    @SerialName("carrier_rating")
    val carrierRating: Double? = null,
    @SerialName("shipper_rating")
    val shipperRating: Double? = null,
    @SerialName("carrier_review")
    val carrierReview: String? = null,
    @SerialName("shipper_review")
    val shipperReview: String? = null,
    val status: BookingStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("package_request")
    val packageRequest: PackageRequest? = null,
    val trip: Trip? = null,
    val shipper: User? = null,
    val carrier: User? = null
)

/**
 * Booking status enumeration
 */
@Serializable
enum class BookingStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("confirmed")
    CONFIRMED,
    @SerialName("picked_up")
    PICKED_UP,
    @SerialName("in_transit")
    IN_TRANSIT,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED,
    @SerialName("disputed")
    DISPUTED;
    
    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            CONFIRMED -> "Confirmed"
            PICKED_UP -> "Picked Up"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
            DISPUTED -> "Disputed"
        }
    
    val color: String
        get() = when (this) {
            PENDING -> "orange"
            CONFIRMED -> "blue"
            PICKED_UP -> "purple"
            IN_TRANSIT -> "green"
            DELIVERED -> "gray"
            CANCELLED -> "red"
            DISPUTED -> "yellow"
        }
    
    val icon: String
        get() = when (this) {
            PENDING -> "⏳"
            CONFIRMED -> "✅"
            PICKED_UP -> "📦"
            IN_TRANSIT -> "🚛"
            DELIVERED -> "🎯"
            CANCELLED -> "❌"
            DISPUTED -> "⚠️"
        }
}

/**
 * Create booking request model
 */
@Serializable
data class CreateBookingRequest(
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("agreed_price")
    val agreedPrice: Double,
    val notes: String? = null
)

/**
 * Confirm booking request model
 */
@Serializable
data class ConfirmBookingRequest(
    @SerialName("pickup_time")
    val pickupTime: String? = null,
    val notes: String? = null
)

/**
 * Update booking status request model
 */
@Serializable
data class UpdateBookingStatusRequest(
    val status: BookingStatus,
    @SerialName("pickup_time")
    val pickupTime: String? = null,
    @SerialName("delivery_time")
    val deliveryTime: String? = null,
    @SerialName("pickup_confirmation")
    val pickupConfirmation: String? = null,
    @SerialName("delivery_confirmation")
    val deliveryConfirmation: String? = null,
    val notes: String? = null
)

/**
 * Rate booking request model
 */
@Serializable
data class RateBookingRequest(
    val rating: Double,
    val review: String? = null
)

/**
 * Booking response model
 */
@Serializable
data class BookingResponse(
    val success: Boolean,
    val message: String,
    val data: Booking
)

/**
 * Bookings list response model
 */
@Serializable
data class BookingsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Booking>
)

/**
 * Booking analytics model
 */
@Serializable
data class BookingAnalytics(
    @SerialName("total_bookings")
    val totalBookings: Int,
    @SerialName("completed_bookings")
    val completedBookings: Int,
    @SerialName("cancelled_bookings")
    val cancelledBookings: Int,
    @SerialName("total_earnings")
    val totalEarnings: Double,
    @SerialName("average_rating")
    val averageRating: Double,
    @SerialName("completion_rate")
    val completionRate: Double
) 