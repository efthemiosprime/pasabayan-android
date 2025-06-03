package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Trip model exactly matching iOS Trip.swift structure
 * Represents a carrier's delivery trip with all iOS properties
 */
@Serializable
data class Trip(
    val id: Int,
    @SerialName("carrier_id")
    val carrierId: Int,
    @SerialName("origin_city")
    val originCity: String,
    @SerialName("origin_country")
    val originCountry: String,
    @SerialName("origin_lat")
    val originLat: Double,
    @SerialName("origin_lng")
    val originLng: Double,
    @SerialName("destination_city")
    val destinationCity: String,
    @SerialName("destination_country")
    val destinationCountry: String,
    @SerialName("destination_lat")
    val destinationLat: Double,
    @SerialName("destination_lng")
    val destinationLng: Double,
    @SerialName("departure_date")
    val departureDate: String, // ISO date string
    @SerialName("arrival_date")
    val arrivalDate: String, // ISO date string
    @SerialName("available_weight_kg")
    val availableWeightKg: Double,
    @SerialName("available_space_liters")
    val availableSpaceLiters: Double,
    @SerialName("price_per_kg")
    val pricePerKg: Double,
    @SerialName("trip_status")
    val tripStatus: TripStatus,
    @SerialName("transportation_method")
    val transportationMethod: TransportationMethod,
    @SerialName("special_notes")
    val specialNotes: String? = null
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
        get() = String.format("₱%.2f/kg", pricePerKg)
    
    val formattedCapacity: String
        get() = String.format("%.1fkg, %.1fL", availableWeightKg, availableSpaceLiters)
    
    companion object {
        // MARK: - Static Mock Data (exactly matching iOS)
        val mockTrips: List<Trip> = listOf(
            Trip(
                id = 1,
                carrierId = 1,
                originCity = "Manila",
                originCountry = "Philippines",
                originLat = 14.5995,
                originLng = 120.9842,
                destinationCity = "Cebu",
                destinationCountry = "Philippines",
                destinationLat = 10.3157,
                destinationLng = 123.8854,
                departureDate = getDateString(2), // 2 days from now
                arrivalDate = getDateString(2, 9000), // 2 days + 2.5 hours
                availableWeightKg = 15.0,
                availableSpaceLiters = 50.0,
                pricePerKg = 25.00,
                tripStatus = TripStatus.ACTIVE,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Regular PAL flight, reliable schedule. Can handle fragile items with care."
            ),
            Trip(
                id = 2,
                carrierId = 1,
                originCity = "Quezon City",
                originCountry = "Philippines",
                originLat = 14.6760,
                originLng = 121.0437,
                destinationCity = "Davao",
                destinationCountry = "Philippines",
                destinationLat = 7.1907,
                destinationLng = 125.4553,
                departureDate = getDateString(5), // 5 days from now
                arrivalDate = getDateString(5, 7200), // 5 days + 2 hours
                availableWeightKg = 25.0,
                availableSpaceLiters = 80.0,
                pricePerKg = 30.00,
                tripStatus = TripStatus.SCHEDULED,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Morning flight with layover in Cebu. Extra space available."
            ),
            Trip(
                id = 3,
                carrierId = 1,
                originCity = "Makati",
                originCountry = "Philippines",
                originLat = 14.5547,
                originLng = 121.0244,
                destinationCity = "Baguio",
                destinationCountry = "Philippines",
                destinationLat = 16.4023,
                destinationLng = 120.5960,
                departureDate = getDateString(-3), // 3 days ago
                arrivalDate = getDateString(-3, 18000), // 3 days ago + 5 hours
                availableWeightKg = 0.0,
                availableSpaceLiters = 0.0,
                pricePerKg = 20.00,
                tripStatus = TripStatus.COMPLETED,
                transportationMethod = TransportationMethod.BUS,
                specialNotes = "Comfortable bus ride through scenic mountain routes. Full capacity reached."
            ),
            Trip(
                id = 4,
                carrierId = 1,
                originCity = "BGC",
                originCountry = "Philippines",
                originLat = 14.5515,
                originLng = 121.0497,
                destinationCity = "Iloilo",
                destinationCountry = "Philippines",
                destinationLat = 10.7202,
                destinationLng = 122.5621,
                departureDate = getDateString(1), // 1 day from now
                arrivalDate = getDateString(1, 5400), // 1 day + 1.5 hours
                availableWeightKg = 12.0,
                availableSpaceLiters = 35.0,
                pricePerKg = 28.00,
                tripStatus = TripStatus.SCHEDULED,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Quick domestic flight. Limited space for packages."
            ),
            Trip(
                id = 5,
                carrierId = 1,
                originCity = "Pasig",
                originCountry = "Philippines",
                originLat = 14.5764,
                originLng = 121.0851,
                destinationCity = "Bacolod",
                destinationCountry = "Philippines",
                destinationLat = 10.6319,
                destinationLng = 122.9951,
                departureDate = getDateString(-1), // 1 day ago
                arrivalDate = getDateString(-1, 3600), // 1 day ago + 1 hour
                availableWeightKg = 0.0,
                availableSpaceLiters = 0.0,
                pricePerKg = 35.00,
                tripStatus = TripStatus.CANCELLED,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Flight cancelled due to weather conditions. Refunds processed."
            ),
            Trip(
                id = 6,
                carrierId = 1,
                originCity = "Taguig",
                originCountry = "Philippines",
                originLat = 14.5176,
                originLng = 121.0509,
                destinationCity = "Cagayan de Oro",
                destinationCountry = "Philippines",
                destinationLat = 8.4542,
                destinationLng = 124.6319,
                departureDate = getDateString(7), // 7 days from now
                arrivalDate = getDateString(7, 6300), // 7 days + 1.75 hours
                availableWeightKg = 20.0,
                availableSpaceLiters = 65.0,
                pricePerKg = 27.50,
                tripStatus = TripStatus.SCHEDULED,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Evening flight with good capacity. Accepting bookings now."
            )
        )
        
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
            SCHEDULED -> "Scheduled"
            ACTIVE -> "Active"
            COMPLETED -> "Completed"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            SCHEDULED -> "blue"
            ACTIVE -> "green"
            COMPLETED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            SCHEDULED -> "📅"
            ACTIVE -> "🚛"
            COMPLETED -> "✅"
            CANCELLED -> "❌"
        }
    
    companion object {
        val allCases = values().toList()
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
 * API Response Models (matching iOS)
 */
@Serializable
data class TripResponse(
    val message: String,
    val data: Trip
)

@Serializable
data class TripsResponse(
    val message: String,
    val data: List<Trip>
) 