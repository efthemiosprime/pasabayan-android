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
    val city: String = "",
    val country: String = "",
    @SerialName("destination_city") val destinationCity: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("route_type") val routeType: String = "unknown",
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
    val carrier: RouteActivityCarrierSummaryJson? = null,
)

@Serializable
data class RouteActivityCarrierSummaryJson(
    @SerialName("package_delivery_near_home") val packageDeliveryNearHome: Int = 0,
    @SerialName("service_errand_near_home") val serviceErrandNearHome: Int = 0,
    @SerialName("new_packages_this_week_near_home") val newPackagesThisWeekNearHome: Int = 0,
)
