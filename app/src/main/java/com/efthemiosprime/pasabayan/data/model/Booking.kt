package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Booking model - exactly matching iOS Booking.swift structure
 * Represents a confirmed delivery booking with all iOS properties
 */
@Serializable
data class Booking(
    val id: Int,
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("carrier_id")
    val carrierId: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    @SerialName("pickup_location")
    val pickupLocation: String,
    @SerialName("delivery_location")
    val deliveryLocation: String,
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("scheduled_pickup_date")
    val scheduledPickupDate: String,
    @SerialName("scheduled_pickup_time")
    val scheduledPickupTime: String? = null,
    @SerialName("actual_pickup_time")
    val actualPickupTime: String? = null,
    @SerialName("estimated_delivery_time")
    val estimatedDeliveryTime: String? = null,
    @SerialName("actual_delivery_time")
    val actualDeliveryTime: String? = null,
    @SerialName("agreed_price")
    val agreedPrice: Double,
    val status: BookingStatus,
    val notes: String? = null,
    @SerialName("tracking_number")
    val trackingNumber: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    
    // Related data
    @SerialName("package_request")
    val packageRequest: PackageRequest? = null,
    val trip: Trip? = null,
    val carrier: User? = null,
    val shipper: User? = null
)

/**
 * Booking Status Enum - exactly matching iOS BookingStatus enum
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
    CANCELLED;
    
    val displayName: String
        get() = when (this) {
            PENDING -> "Pending Confirmation"
            CONFIRMED -> "Confirmed"
            PICKED_UP -> "Picked Up"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
    
    val shortDisplayName: String
        get() = when (this) {
            PENDING -> "Pending"
            CONFIRMED -> "Confirmed"
            PICKED_UP -> "Picked Up"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            PENDING -> "orange"
            CONFIRMED -> "blue"
            PICKED_UP -> "purple"
            IN_TRANSIT -> "green"
            DELIVERED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            PENDING -> "⏳"
            CONFIRMED -> "✅"
            PICKED_UP -> "📦"
            IN_TRANSIT -> "🚛"
            DELIVERED -> "✅"
            CANCELLED -> "❌"
        }
    
    val description: String
        get() = when (this) {
            PENDING -> "Waiting for carrier confirmation"
            CONFIRMED -> "Booking confirmed, waiting for pickup"
            PICKED_UP -> "Package has been picked up"
            IN_TRANSIT -> "Package is on the way"
            DELIVERED -> "Package has been delivered"
            CANCELLED -> "Booking has been cancelled"
        }
}

// MARK: - Create Booking Request - exactly matching iOS
@Serializable
data class CreateBookingRequest(
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("pickup_location")
    val pickupLocation: String,
    @SerialName("delivery_location")
    val deliveryLocation: String,
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("scheduled_pickup_date")
    val scheduledPickupDate: String,
    @SerialName("scheduled_pickup_time")
    val scheduledPickupTime: String? = null,
    @SerialName("agreed_price")
    val agreedPrice: Double,
    val notes: String? = null
)

// MARK: - Booking Status Update Request - exactly matching iOS
@Serializable
data class BookingStatusUpdateRequest(
    val status: BookingStatus,
    val notes: String? = null,
    @SerialName("actual_time")
    val actualTime: String? = null // For pickup or delivery time
)

// MARK: - Booking Timeline Event - exactly matching iOS
@Serializable
data class BookingTimelineEvent(
    val id: Int,
    @SerialName("booking_id")
    val bookingId: Int,
    val event: BookingStatus,
    val timestamp: String,
    val notes: String? = null,
    val location: String? = null
)

// MARK: - Booking Statistics - exactly matching iOS
@Serializable
data class BookingStats(
    @SerialName("total_bookings")
    val totalBookings: Int,
    @SerialName("pending_bookings")
    val pendingBookings: Int,
    @SerialName("confirmed_bookings")
    val confirmedBookings: Int,
    @SerialName("active_bookings")
    val activeBookings: Int,
    @SerialName("completed_bookings")
    val completedBookings: Int,
    @SerialName("cancelled_bookings")
    val cancelledBookings: Int,
    @SerialName("total_earnings")
    val totalEarnings: Double,
    @SerialName("average_rating")
    val averageRating: Double? = null
)

// MARK: - API Response Models - exactly matching iOS
@Serializable
data class BookingResponse(
    val message: String,
    val data: Booking
)

@Serializable
data class BookingsResponse(
    val message: String,
    val data: PaginatedResponse<Booking>
)

@Serializable
data class BookingStatsResponse(
    val success: Boolean,
    val message: String,
    val data: BookingStats
)

@Serializable
data class BookingTimelineResponse(
    val success: Boolean,
    val message: String,
    val data: List<BookingTimelineEvent>
) 