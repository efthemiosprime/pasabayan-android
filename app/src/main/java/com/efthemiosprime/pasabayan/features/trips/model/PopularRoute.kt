package com.efthemiosprime.pasabayan.features.trips.model

enum class PopularRouteType {
    FLIGHT,
    CAR,
    SHIP,
    UNKNOWN;

    companion object {
        fun fromWire(value: String?): PopularRouteType = when (value?.lowercase()) {
            "flight" -> FLIGHT
            "car" -> CAR
            "ship" -> SHIP
            else -> UNKNOWN
        }
    }
}

data class PopularRoute(
    val city: String,
    val country: String,
    val destinationCity: String,
    val destinationCountry: String,
    val routeType: PopularRouteType,
    val displayName: String,
    val tripCount: Int?,
)
