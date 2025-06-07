package com.efthemiosprime.pasabayan.data.model.analytics

/**
 * Historical statistics for carriers
 */
data class CarrierHistoricalStats(
    val period: AnalyticsPeriod,
    val monthlyPerformance: List<MonthlyPerformance>,
    val trends: CarrierTrends,
    val routeAnalytics: List<RouteAnalytics>,
    val performanceInsights: List<String>
)

/**
 * Monthly performance data for carriers
 */
data class MonthlyPerformance(
    val month: String,
    val monthName: String,
    val tripsCreated: Int,
    val matchesReceived: Int,
    val deliveriesCompleted: Int,
    val earnings: Double, // CAD values
    val successRate: Double
)

/**
 * Trends data for carriers
 */
data class CarrierTrends(
    val earningsTrend: String,
    val deliveriesTrend: String,
    val earningsGrowthRate: Double,
    val deliveriesGrowthRate: Double
) 