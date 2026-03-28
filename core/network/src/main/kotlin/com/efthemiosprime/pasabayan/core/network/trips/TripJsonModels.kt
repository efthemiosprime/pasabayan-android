package com.efthemiosprime.pasabayan.core.network.trips

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip DTO — mirrors iOS `Trip.swift` fields. Wire format uses snake_case.
 */
@Serializable
data class TripJson(
    val id: Int,
    @SerialName("carrier_id") val carrierId: Int = 0,
    @SerialName("origin_city") val originCity: String = "",
    @SerialName("origin_country") val originCountry: String = "",
    @SerialName("origin_lat") @Serializable(with = FlexibleDoubleSerializer::class) val originLat: Double? = null,
    @SerialName("origin_lng") @Serializable(with = FlexibleDoubleSerializer::class) val originLng: Double? = null,
    @SerialName("destination_city") val destinationCity: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("destination_lat") @Serializable(with = FlexibleDoubleSerializer::class) val destinationLat: Double? = null,
    @SerialName("destination_lng") @Serializable(with = FlexibleDoubleSerializer::class) val destinationLng: Double? = null,
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("available_weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val availableWeightKg: Double? = null,
    @SerialName("available_space_liters") @Serializable(with = FlexibleDoubleSerializer::class) val availableSpaceLiters: Double? = null,
    @SerialName("price_per_kg") @Serializable(with = FlexibleDoubleSerializer::class) val pricePerKg: Double? = null,
    @SerialName("trip_status") val tripStatus: TripStatus = TripStatus.PLANNING,
    @SerialName("transportation_method") val transportationMethod: TransportationMethod = TransportationMethod.OTHER,
    @SerialName("special_notes") val specialNotes: String? = null,
    val carrier: UserSummary? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,

    // Land transport pricing
    @SerialName("pricing_type") val pricingType: String? = null,
    @SerialName("pricing_method") val pricingMethod: String? = null,
    @SerialName("flat_trip_price") @Serializable(with = FlexibleDoubleSerializer::class) val flatTripPrice: Double? = null,
    @SerialName("base_price") @Serializable(with = FlexibleDoubleSerializer::class) val basePrice: Double? = null,
    @SerialName("calculated_price") @Serializable(with = FlexibleDoubleSerializer::class) val calculatedPrice: Double? = null,
    @SerialName("distance_multiplier") @Serializable(with = FlexibleDoubleSerializer::class) val distanceMultiplier: Double? = null,

    // Passenger transport
    @SerialName("passenger_capacity") val passengerCapacity: Int? = null,
    @SerialName("price_per_passenger") @Serializable(with = FlexibleDoubleSerializer::class) val pricePerPassenger: Double? = null,
    @SerialName("passenger_requirements") val passengerRequirements: String? = null,
    @SerialName("age_restrictions") val ageRestrictions: String? = null,
    @SerialName("passenger_amenities") val passengerAmenities: String? = null,

    // Addresses
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("pickup_landmark") val pickupLandmark: String? = null,
    @SerialName("pickup_instructions") val pickupInstructions: String? = null,
    @SerialName("dropoff_address") val dropoffAddress: String? = null,
    @SerialName("dropoff_landmark") val dropoffLandmark: String? = null,
    @SerialName("dropoff_instructions") val dropoffInstructions: String? = null,

    // Earnings
    @SerialName("trip_earnings_total") @Serializable(with = FlexibleDoubleSerializer::class) val tripEarningsTotal: Double? = null,
    @SerialName("trip_earnings_currency") val tripEarningsCurrency: String? = null,
    @SerialName("trip_earnings_breakdown") val tripEarningsBreakdown: TripEarningsBreakdownJson? = null,

    // Pending requests
    @SerialName("has_pending_requests") @Serializable(with = FlexibleBoolSerializer::class) val hasPendingRequests: Boolean? = null,
    @SerialName("pending_request_count") val pendingRequestCount: Int? = null,
    @SerialName("pending_requests") val pendingRequests: List<PendingTripRequestJson>? = null,
    @SerialName("distance_km") @Serializable(with = FlexibleDoubleSerializer::class) val distanceKm: Double? = null,
)

@Serializable
data class CreateTripRequestJson(
    @SerialName("origin_city") val originCity: String,
    @SerialName("origin_country") val originCountry: String,
    @SerialName("destination_city") val destinationCity: String,
    @SerialName("destination_country") val destinationCountry: String,
    @SerialName("departure_date") val departureDate: String,
    @SerialName("arrival_date") val arrivalDate: String,
    @SerialName("available_weight_kg") val availableWeightKg: Double,
    @SerialName("available_space_liters") val availableSpaceLiters: Double = 0.0,
    @SerialName("price_per_kg") val pricePerKg: Double? = null,
    @SerialName("transportation_method") val transportationMethod: String,
    @SerialName("special_notes") val specialNotes: String? = null,
    @SerialName("pricing_method") val pricingMethod: String? = null,
    @SerialName("flat_trip_price") val flatTripPrice: Double? = null,
    @SerialName("base_price") val basePrice: Double? = null,
    @SerialName("origin_city_id") val originCityId: Int? = null,
    @SerialName("destination_city_id") val destinationCityId: Int? = null,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("dropoff_address") val dropoffAddress: String? = null,
    @SerialName("auto_request_package_id") val autoRequestPackageId: Int? = null,
    @SerialName("proposed_price") val proposedPrice: Double? = null,
    @SerialName("request_message") val requestMessage: String? = null,
)

@Serializable
data class TripUpdateRequestJson(
    @SerialName("trip_status") val tripStatus: String? = null,
    @SerialName("available_weight_kg") val availableWeightKg: Double? = null,
    @SerialName("available_space_liters") val availableSpaceLiters: Double? = null,
    @SerialName("price_per_kg") val pricePerKg: Double? = null,
    @SerialName("special_notes") val specialNotes: String? = null,
    @SerialName("flat_trip_price") val flatTripPrice: Double? = null,
    @SerialName("origin_city") val originCity: String? = null,
    @SerialName("origin_country") val originCountry: String? = null,
    @SerialName("origin_lat") val originLat: Double? = null,
    @SerialName("origin_lng") val originLng: Double? = null,
    @SerialName("destination_city") val destinationCity: String? = null,
    @SerialName("destination_country") val destinationCountry: String? = null,
    @SerialName("destination_lat") val destinationLat: Double? = null,
    @SerialName("destination_lng") val destinationLng: Double? = null,
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("transportation_method") val transportationMethod: String? = null,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("dropoff_address") val dropoffAddress: String? = null,
)

// -- Response wrappers --

@Serializable
data class TripResponseJson(
    val message: String = "",
    val data: TripJson? = null,
)

@Serializable
data class TripsResponseJson(
    val message: String = "",
    val data: PaginatedTripsJson? = null,
)

@Serializable
data class TripsSuccessResponseJson(
    val success: Boolean = false,
    val data: PaginatedTripsJson? = null,
)

@Serializable
data class TripDeleteResponseJson(
    val message: String = "",
    val success: Boolean = false,
)

@Serializable
data class PaginatedTripsJson(
    val data: List<TripJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)
