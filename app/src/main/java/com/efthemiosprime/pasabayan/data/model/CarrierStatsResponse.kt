package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Carrier Statistics Response
 * Matches API response structure for carrier stats endpoint
 */
@Serializable
data class CarrierStatsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: CarrierStatsData
)

/**
 * Carrier Statistics Data
 * Main data container for carrier statistics
 */
@Serializable
data class CarrierStatsData(
    @SerialName("deliveries")
    val deliveries: DeliveryStats,
    @SerialName("ratings")
    val ratings: RatingStats,
    @SerialName("earnings")
    val earnings: EarningsStats,
    @SerialName("profile_complete")
    val profileComplete: Boolean
)

/**
 * Delivery Statistics
 * Contains trip and delivery related metrics
 */
@Serializable
data class DeliveryStats(
    @SerialName("total_trips")
    val totalTrips: Int,
    @SerialName("active_trips")
    val activeTrips: Int,
    @SerialName("completed_trips")
    val completedTrips: Int,
    @SerialName("total_matches")
    val totalMatches: Int,
    @SerialName("completed_matches")
    val completedMatches: Int,
    @SerialName("success_rate")
    val successRate: Double
)

/**
 * Rating Statistics
 * Contains rating and performance metrics
 */
@Serializable
data class RatingStats(
    @SerialName("average_rating")
    val averageRating: Double,
    @SerialName("total_ratings")
    val totalRatings: Int,
    @SerialName("response_time_hours")
    val responseTimeHours: Double,
    @SerialName("reliability_score")
    val reliabilityScore: Double
)

/**
 * Earnings Statistics
 * Contains financial metrics and top routes
 */
@Serializable
data class EarningsStats(
    @SerialName("total_earnings")
    val totalEarnings: Double,
    @SerialName("monthly_earnings")
    val monthlyEarnings: Double,
    @SerialName("average_per_delivery")
    val averagePerDelivery: Double,
    @SerialName("pending_payments")
    val pendingPayments: Double,
    @SerialName("top_routes")
    val topRoutes: List<TopRoute>
)

/**
 * Top Route Information
 * Individual route performance data
 */
@Serializable
data class TopRoute(
    @SerialName("route")
    val route: String,
    @SerialName("trips")
    val trips: Int,
    @SerialName("potential_earnings")
    val potentialEarnings: Double
) 