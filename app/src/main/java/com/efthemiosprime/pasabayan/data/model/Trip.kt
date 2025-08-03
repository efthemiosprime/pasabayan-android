package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.efthemiosprime.pasabayan.data.common.ValidationResult
import com.efthemiosprime.pasabayan.data.common.Validation
import com.efthemiosprime.pasabayan.data.common.validate

/**
 * Trip model exactly matching iOS Trip.swift structure  
 * Represents a carrier's delivery trip with all iOS properties
 * Field mappings and nullability match iOS CodingKeys exactly
 */
@Serializable
data class Trip(
    val id: Int,
    @SerialName("carrier_id")
    val carrierId: Int = 0, // Default 0 like iOS when not in limited API responses
    @SerialName("origin_city")
    val originCity: String,
    @SerialName("origin_country")
    val originCountry: String = "", // Default empty like iOS when not in limited API responses
    @SerialName("origin_lat")
    val originLat: Double = 0.0, // Handle coordinates that might be sent as strings like iOS
    @SerialName("origin_lng")
    val originLng: Double = 0.0,
    @SerialName("destination_city")
    val destinationCity: String,
    @SerialName("destination_country")
    val destinationCountry: String = "", // Default empty like iOS when not in limited API responses
    @SerialName("destination_lat")
    val destinationLat: Double = 0.0,
    @SerialName("destination_lng")
    val destinationLng: Double = 0.0,
    @SerialName("departure_date")
    val departureDate: String, // ISO date string
    @SerialName("arrival_date")
    val arrivalDate: String, // ISO date string
    @SerialName("available_weight_kg")
    val availableWeightKg: Double,
    @SerialName("available_space_liters")
    val availableSpaceLiters: Double,
    @SerialName("price_per_kg")
    val pricePerKg: Double = 0.0, // Default 0.0 like iOS when not in limited API responses
    @SerialName("trip_status")
    val tripStatus: TripStatus = TripStatus.PLANNING, // Default to planning like iOS when not provided
    @SerialName("transportation_method")
    val transportationMethod: TransportationMethod,
    @SerialName("special_notes")
    val specialNotes: String? = null,
    val carrier: User? = null
) {
    // MARK: - Computed Properties (matching iOS exactly)
    
    val originLocation: String
        get() = "$originCity, $originCountry"
    
    val destinationLocation: String
        get() = "$destinationCity, $destinationCountry"
    
    val route: String
        get() = "$originCity → $destinationCity"
    
    val formattedDepartureDate: String
        get() {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                val date = inputFormat.parse(departureDate)
                date?.let { outputFormat.format(it) } ?: departureDate
            } catch (e: Exception) {
                departureDate
            }
        }
    
    val formattedArrivalDate: String
        get() {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                val date = inputFormat.parse(arrivalDate)
                date?.let { outputFormat.format(it) } ?: arrivalDate
            } catch (e: Exception) {
                arrivalDate
            }
        }
    
    val formattedDuration: String
        get() {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val departureTime = inputFormat.parse(departureDate)?.time ?: 0
                val arrivalTime = inputFormat.parse(arrivalDate)?.time ?: 0
                val durationMs = arrivalTime - departureTime
                val totalMinutes = (durationMs / (1000 * 60)).toInt()
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                
                when {
                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                    hours > 0 -> "${hours}h"
                    else -> "${minutes}m"
                }
            } catch (e: Exception) {
                "N/A"
            }
        }
    
    val formattedPrice: String
        get() = String.format("$%.2f/kg", pricePerKg)
    
    val formattedCapacity: String
        get() = String.format("%.1fkg, %.1fL", availableWeightKg, availableSpaceLiters)
    
    // MARK: - Additional Computed Properties
    
    val hasCapacity: Boolean
        get() = availableWeightKg > 0 && availableSpaceLiters > 0
    
    val isPlanning: Boolean
        get() = tripStatus == TripStatus.PLANNING
    
    val isActive: Boolean
        get() = tripStatus == TripStatus.ACTIVE
    
    val isCompleted: Boolean
        get() = tripStatus == TripStatus.COMPLETED
    
    val isCancelled: Boolean
        get() = tripStatus == TripStatus.CANCELLED
    
    val isScheduled: Boolean
        get() = tripStatus == TripStatus.SCHEDULED
    
    // MARK: - Functional Update Methods (Immutable)
    
    /**
     * Update trip status functionally
     */
    fun updateStatus(newStatus: TripStatus): Trip = copy(tripStatus = newStatus)
    
    /**
     * Update available capacity functionally
     */
    fun updateCapacity(weightKg: Double, spaceLiters: Double): Trip = 
        copy(availableWeightKg = weightKg, availableSpaceLiters = spaceLiters)
    
    /**
     * Reduce capacity after booking (immutable)
     */
    fun reduceCapacity(weightKg: Double, spaceLiters: Double): Trip = copy(
        availableWeightKg = (availableWeightKg - weightKg).coerceAtLeast(0.0),
        availableSpaceLiters = (availableSpaceLiters - spaceLiters).coerceAtLeast(0.0)
    )
    
    /**
     * Update price functionally
     */
    fun updatePrice(newPrice: Double): Trip = copy(pricePerKg = newPrice)
    
    /**
     * Update special notes functionally
     */
    fun updateNotes(newNotes: String?): Trip = copy(specialNotes = newNotes)
    
    /**
     * Update departure date functionally
     */
    fun updateDepartureDate(newDate: String): Trip = copy(departureDate = newDate)
    
    /**
     * Update arrival date functionally
     */
    fun updateArrivalDate(newDate: String): Trip = copy(arrivalDate = newDate)
    
    /**
     * Mark trip as active functionally
     */
    fun markAsActive(): Trip = copy(tripStatus = TripStatus.ACTIVE)
    
    /**
     * Mark trip as completed functionally
     */
    fun markAsCompleted(): Trip = copy(tripStatus = TripStatus.COMPLETED)
    
    /**
     * Cancel trip functionally
     */
    fun cancel(): Trip = copy(tripStatus = TripStatus.CANCELLED)
    
    // MARK: - Validation Methods (Pure Functions)
    
    /**
     * Validate trip data
     */
    fun validate(): ValidationResult = validate {
        validate(Validation.validateRequired(originCity, "Origin city"))
        validate(Validation.validateRequired(destinationCity, "Destination city"))
        validate(Validation.validateISODate(departureDate, "Departure date"))
        validate(Validation.validateISODate(arrivalDate, "Arrival date"))
        validate(Validation.validateWeight(availableWeightKg))
        validate(Validation.validateNonNegativeNumber(availableSpaceLiters, "Available space"))
        validate(Validation.validatePrice(pricePerKg))
    }
    
    /**
     * Validate if trip can accept booking
     */
    fun canAcceptBooking(requiredWeight: Double, requiredSpace: Double): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            !isActive && !isScheduled -> errors.add("Trip is not accepting bookings")
            availableWeightKg < requiredWeight -> errors.add("Insufficient weight capacity")
            availableSpaceLiters < requiredSpace -> errors.add("Insufficient space capacity")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    companion object {
        /**
         * REMOVED: Mock trip data to enforce real API usage
         * Use TripRepository.getAvailableTrips() or similar real API calls instead
         */
        @Deprecated("Mock trip data removed - use real API calls", ReplaceWith(""))
        fun getMockTrips(): List<Trip> = emptyList()
        
        private fun getDateString(daysFromNow: Int, additionalSeconds: Long = 0): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, daysFromNow)
            calendar.add(Calendar.SECOND, additionalSeconds.toInt())
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            return format.format(calendar.time)
        }
    }
}

/**
 * Trip Status Enum (exactly matching iOS)
 */
@Serializable
enum class TripStatus {
    @SerialName("planning")
    PLANNING,
    @SerialName("scheduled")
    SCHEDULED,
    @SerialName("active")
    ACTIVE,
    @SerialName("completed")
    COMPLETED,
    @SerialName("cancelled")
    CANCELLED;
    
    val displayName: String
        get() = when (this) {
            PLANNING -> "Planning"
            SCHEDULED -> "Scheduled"
            ACTIVE -> "Active"
            COMPLETED -> "Completed"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            PLANNING -> "blue"
            SCHEDULED -> "blue"
            ACTIVE -> "green"
            COMPLETED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            PLANNING -> "📝"
            SCHEDULED -> "📅"
            ACTIVE -> "🚛"
            COMPLETED -> "✅"
            CANCELLED -> "❌"
        }
    
    val rawValue: String
        get() = when (this) {
            PLANNING -> "planning"
            SCHEDULED -> "scheduled"
            ACTIVE -> "active"
            COMPLETED -> "completed"
            CANCELLED -> "cancelled"
        }
    
    companion object {
        val allCases = values().toList()
        
        fun fromString(status: String): TripStatus? {
            return when (status.lowercase()) {
                "planning" -> PLANNING
                "scheduled" -> SCHEDULED
                "active" -> ACTIVE
                "completed" -> COMPLETED
                "cancelled" -> CANCELLED
                else -> null
            }
        }
    }
}

/**
 * Transportation Method Enum (exactly matching iOS)
 */
@Serializable
enum class TransportationMethod {
    @SerialName("flight")
    FLIGHT,
    @SerialName("bus")
    BUS,
    @SerialName("car")
    CAR,
    @SerialName("truck")
    TRUCK,
    @SerialName("motorcycle")
    MOTORCYCLE,
    @SerialName("ship")
    SHIP,
    @SerialName("train")
    TRAIN;
    
    val displayName: String
        get() = when (this) {
            FLIGHT -> "Flight"
            BUS -> "Bus"
            CAR -> "Car"
            TRUCK -> "Truck"
            MOTORCYCLE -> "Motorcycle"
            SHIP -> "Ship"
            TRAIN -> "Train"
        }
    
    val icon: String
        get() = when (this) {
            FLIGHT -> "✈️"
            BUS -> "🚌"
            CAR -> "🚗"
            TRUCK -> "🚛"
            MOTORCYCLE -> "🏍️"
            SHIP -> "🚢"
            TRAIN -> "🚂"
        }
    
    val rawValue: String
        get() = when (this) {
            FLIGHT -> "flight"
            BUS -> "bus"
            CAR -> "car"
            TRUCK -> "truck"
            MOTORCYCLE -> "motorcycle"
            SHIP -> "ship"
            TRAIN -> "train"
        }
    
    companion object {
        fun fromString(method: String): TransportationMethod? {
            return when (method.lowercase()) {
                "flight" -> FLIGHT
                "bus" -> BUS
                "car" -> CAR
                "truck" -> TRUCK
                "motorcycle" -> MOTORCYCLE
                "ship" -> SHIP
                "train" -> TRAIN
                else -> null
            }
        }
    }
}

/**
 * Create Trip Request (matching iOS)
 */
@Serializable
data class CreateTripRequest(
    @SerialName("start_location")
    val startLocation: String,
    @SerialName("end_location")
    val endLocation: String,
    @SerialName("start_coordinates")
    val startCoordinates: Coordinates? = null,
    @SerialName("end_coordinates")
    val endCoordinates: Coordinates? = null,
    @SerialName("departure_date")
    val departureDate: String,
    @SerialName("departure_time")
    val departureTime: String,
    @SerialName("available_capacity")
    val availableCapacity: String,
    @SerialName("price_per_km")
    val pricePerKm: Double,
    val notes: String? = null
)

/**
 * Trip Search Parameters (matching iOS)
 */
@Serializable
data class TripSearchRequest(
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    val date: String? = null,
    @SerialName("max_price")
    val maxPrice: Double? = null,
    @SerialName("min_capacity")
    val minCapacity: String? = null
)

/**
 * Create Trip Request for API (matching backend requirements)
 */
@Serializable
data class CreateTripRequestApi(
    @SerialName("origin_city")
    val originCity: String,
    @SerialName("origin_country")
    val originCountry: String,
    @SerialName("destination_city")
    val destinationCity: String,
    @SerialName("destination_country")
    val destinationCountry: String,
    @SerialName("departure_date")
    val departureDate: String, // ISO datetime format: "2025-12-25T10:00:00Z"
    @SerialName("arrival_date")
    val arrivalDate: String, // ISO datetime format: "2025-12-25T14:00:00Z"
    @SerialName("available_weight_kg")
    val availableWeightKg: Double,
    @SerialName("available_space_liters")
    val availableSpaceLiters: Double,
    @SerialName("price_per_kg")
    val pricePerKg: Double,
    @SerialName("transportation_method")
    val transportationMethod: String,
    @SerialName("special_notes")
    val specialNotes: String? = null
)

/**
 * API Response Models (matching iOS)
 */
@Serializable
data class TripResponse(
    val message: String,
    val data: Trip
)

@Serializable
data class PaginatedTripsData(
    @SerialName("current_page")
    val currentPage: Int,
    val data: List<Trip>,
    @SerialName("first_page_url")
    val firstPageUrl: String? = null,
    val from: Int? = null,
    @SerialName("last_page")
    val lastPage: Int,
    @SerialName("last_page_url")
    val lastPageUrl: String? = null,
    val links: List<PaginationLink>? = null,
    @SerialName("next_page_url")
    val nextPageUrl: String? = null,
    val path: String? = null,
    @SerialName("per_page")
    val perPage: Int,
    @SerialName("prev_page_url")
    val prevPageUrl: String? = null,
    val to: Int? = null,
    val total: Int
)

@Serializable
data class TripsResponse(
    val message: String,
    val data: PaginatedTripsData
) 