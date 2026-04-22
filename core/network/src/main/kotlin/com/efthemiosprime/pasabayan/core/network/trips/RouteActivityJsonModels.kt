package com.efthemiosprime.pasabayan.core.network.trips

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PopularRoutesResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: List<PopularRouteJson> = emptyList(),
)

@Serializable
data class PopularRouteJson(
    @SerialName("origin_city") val originCity: String? = null,
    @SerialName("origin_country") val originCountry: String? = null,
    @SerialName("destination_city") val destinationCity: String = "",
    @SerialName("package_count") val packageCount: Int? = null,
    @SerialName("average_price") val averagePrice: Double? = null,
    // Legacy support for previously wired payload variants.
    val city: String? = null,
    val country: String? = null,
    @SerialName("destination_country") val destinationCountry: String? = null,
    @SerialName("route_type") val routeType: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("trip_count") val tripCount: Int? = null,
)

@Serializable
data class RouteActivitySummaryResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: RouteActivitySummaryDataJson = RouteActivitySummaryDataJson(),
)

@Serializable
data class RouteActivitySummaryDataJson(
    @SerialName("total_trips") val totalTrips: Int? = null,
    @SerialName("active_trips") val activeTrips: Int? = null,
    @SerialName("completed_trips") val completedTrips: Int? = null,
    @SerialName("total_earnings") val totalEarnings: Double? = null,
    val currency: String? = null,
    // Legacy payload support.
    val carrier: RouteActivityLegacyCarrierJson? = null,
)

@Serializable
data class RouteActivityLegacyCarrierJson(
    @SerialName("package_delivery_near_home") val packageDeliveryNearHome: Int = 0,
    @SerialName("service_errand_near_home") val serviceErrandNearHome: Int = 0,
    @SerialName("new_packages_this_week_near_home") val newPackagesThisWeekNearHome: Int = 0,
)
