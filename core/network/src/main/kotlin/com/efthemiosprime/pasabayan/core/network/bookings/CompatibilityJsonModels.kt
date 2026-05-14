package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleStringSerializer
import com.efthemiosprime.pasabayan.core.network.packages.PackageRequestJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /packages/{packageRequestId}/compatible-trips` payload.
 *
 * Mirrors iOS `CompatibleTrip` (PackageRequest.swift line 1102) and the
 * `CompatibleTripsAPIResponse` envelope (line 1488).
 */
@Serializable
data class CompatibleTripJson(
    val id: Int,
    @SerialName("carrier_id") val carrierId: Int,
    @SerialName("origin_city") val originCity: String,
    @SerialName("origin_country") val originCountry: String = "",
    @SerialName("destination_city") val destinationCity: String,
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("pickup_date") val pickupDate: String? = null,
    @SerialName("delivery_date") val deliveryDate: String? = null,
    @SerialName("available_weight_kg")
    @Serializable(with = FlexibleStringSerializer::class)
    val availableWeightKg: String? = null,
    @SerialName("available_space_liters")
    @Serializable(with = FlexibleStringSerializer::class)
    val availableSpaceLiters: String? = null,
    @SerialName("price_per_kg")
    @Serializable(with = FlexibleStringSerializer::class)
    val pricePerKg: String? = null,
    @SerialName("flat_trip_price")
    @Serializable(with = FlexibleStringSerializer::class)
    val flatTripPrice: String? = null,
    @SerialName("calculated_price")
    @Serializable(with = FlexibleStringSerializer::class)
    val calculatedPrice: String? = null,
    @SerialName("pricing_type") val pricingType: String? = null,
    @SerialName("pricing_method") val pricingMethod: String? = null,
    @SerialName("trip_status") val tripStatus: String? = null,
    @SerialName("transportation_method") val transportationMethod: String? = null,
    @SerialName("special_notes") val specialNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val carrier: UserSummary? = null,
    @SerialName("shipper_request_status") val shipperRequestStatus: String? = null,
    @SerialName("can_request")
    @Serializable(with = FlexibleBoolSerializer::class)
    val canRequest: Boolean? = null,
    @SerialName("request_message") val requestMessage: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
    @SerialName("distance_km")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val distanceKm: Double? = null,
)

@Serializable
data class PaginatedCompatibleTripsJson(
    val data: List<CompatibleTripJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)

@Serializable
data class CompatibleTripsResponseJson(
    val success: Boolean? = null,
    val message: String = "",
    val data: PaginatedCompatibleTripsJson = PaginatedCompatibleTripsJson(),
)

/**
 * `GET /trips/{tripId}/compatible-packages` payload.
 *
 * iOS variant accepts two shapes (nested `compatible_packages` or
 * pagination fields directly on `data`). Android keeps both decoders by
 * making the nested object optional and tolerating the flat shape via
 * `data` being a paginated container fallback. See [CompatiblePackagesDataJson].
 */
@Serializable
data class CompatibilityDetailsJson(
    @SerialName("route_match") val routeMatch: String = "",
    @SerialName("capacity_sufficient") val capacitySufficient: Boolean = false,
    @SerialName("date_compatible") val dateCompatible: Boolean = false,
    @SerialName("price_compatible") val priceCompatible: Boolean = false,
    @SerialName("weight_usage_percentage") val weightUsagePercentage: Double = 0.0,
    @SerialName("space_usage_percentage") val spaceUsagePercentage: Double = 0.0,
)

@Serializable
data class CapacityUtilizationJson(
    @SerialName("weight_kg")
    @Serializable(with = FlexibleStringSerializer::class)
    val weightKg: String? = null,
    @SerialName("space_liters")
    @Serializable(with = FlexibleStringSerializer::class)
    val spaceLiters: String? = null,
    @SerialName("weight_percentage") val weightPercentage: Double = 0.0,
    @SerialName("space_percentage") val spacePercentage: Double = 0.0,
)

@Serializable
data class CompatibilitySummaryJson(
    @SerialName("total_compatible") val totalCompatible: Int = 0,
    @SerialName("perfect_matches") val perfectMatches: Int = 0,
    @SerialName("good_matches") val goodMatches: Int = 0,
    @SerialName("fair_matches") val fairMatches: Int = 0,
    @SerialName("average_compatibility_score") val averageCompatibilityScore: Double = 0.0,
    @SerialName("total_potential_earnings")
    @Serializable(with = FlexibleStringSerializer::class)
    val totalPotentialEarnings: String? = null,
    @SerialName("capacity_utilization") val capacityUtilization: CapacityUtilizationJson? = null,
)

@Serializable
data class PaginatedCompatiblePackagesJson(
    val data: List<PackageRequestJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)

@Serializable
data class CompatiblePackagesDataJson(
    @SerialName("trip_info") val tripInfo: CarrierTripInfoJson? = null,
    @SerialName("compatible_packages") val compatiblePackages: PaginatedCompatiblePackagesJson? = null,
    @SerialName("compatibility_summary") val compatibilitySummary: CompatibilitySummaryJson? = null,
    // Fallback fields when the backend returns pagination directly on `data`
    val data: List<PackageRequestJson>? = null,
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    val total: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
) {
    /** Normalised paginated packages list — handles both nested and flat shapes. */
    val packages: List<PackageRequestJson>
        get() = compatiblePackages?.data ?: data ?: emptyList()
}

@Serializable
data class CompatiblePackagesResponseJson(
    val success: Boolean? = null,
    val message: String = "",
    val data: CompatiblePackagesDataJson = CompatiblePackagesDataJson(),
)
