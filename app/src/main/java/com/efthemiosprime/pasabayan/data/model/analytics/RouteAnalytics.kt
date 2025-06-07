package com.efthemiosprime.pasabayan.data.model.analytics

/**
 * Analytics data for specific routes
 */
data class RouteAnalytics(
    val route: String,
    val totalTrips: Int,
    val avgPricePerKg: Double, // CAD values
    val monthlyData: List<RouteMonthlyData>
)

/**
 * Monthly data for a specific route
 */
data class RouteMonthlyData(
    val period: String,
    val trips: Int,
    val avgPrice: Double // CAD values
) 