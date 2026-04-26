package com.efthemiosprime.pasabayan.core.network.profile

import com.efthemiosprime.pasabayan.core.domain.model.UserTypesSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarrierStatsResponseJson(
    val success: Boolean = true,
    val message: String? = null,
    val data: CarrierStatsJson? = null,
)

@Serializable
data class CarrierStatsJson(
    val deliveries: CarrierDeliveriesJson? = null,
    val ratings: CarrierRatingsJson? = null,
    val earnings: CarrierEarningsJson? = null,
    @SerialName("profile_complete") val profileComplete: Boolean? = null,
)

@Serializable
data class CarrierDeliveriesJson(
    @SerialName("total_trips") val totalTrips: Int? = null,
    @SerialName("active_trips") val activeTrips: Int? = null,
    @SerialName("completed_trips") val completedTrips: Int? = null,
    @SerialName("total_matches") val totalMatches: Int? = null,
    @SerialName("completed_matches") val completedMatches: Int? = null,
    @SerialName("success_rate")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val successRate: Double? = null,
)

@Serializable
data class CarrierRatingsJson(
    @SerialName("average_rating") val averageRating: String? = null,
    @SerialName("total_ratings") val totalRatings: Int? = null,
    @SerialName("response_time_hours")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val responseTimeHours: Double? = null,
    @SerialName("reliability_score") val reliabilityScore: Int? = null,
)

@Serializable
data class CarrierEarningsJson(
    @SerialName("total_earnings")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val totalEarnings: Double? = null,
    @SerialName("monthly_earnings")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val monthlyEarnings: Double? = null,
    @SerialName("average_per_delivery")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val averagePerDelivery: Double? = null,
    @SerialName("pending_payments")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val pendingPayments: Double? = null,
    @SerialName("top_routes") val topRoutes: List<TopRouteJson>? = null,
)

@Serializable
data class TopRouteJson(
    val route: String? = null,
    val trips: Int? = null,
    @SerialName("potential_earnings")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val potentialEarnings: Double? = null,
)

@Serializable
data class CarrierEnableResponseJson(
    val message: String? = null,
)

@Serializable
data class CarrierStatusResponseJson(
    val success: Boolean? = null,
    val message: String? = null,
    val data: CarrierStatusDataJson? = null,
)

@Serializable
data class CarrierStatusDataJson(
    @SerialName("is_active_carrier") val isActiveCarrier: Boolean? = null,
    @SerialName("carrier_status") val carrierStatus: String? = null,
    @SerialName("has_carrier_profile") val hasCarrierProfile: Boolean? = null,
    @SerialName("profile_recommended") val profileRecommended: Boolean? = null,
    @SerialName("user_types")
    @Serializable(with = UserTypesSerializer::class)
    val userTypes: List<String>? = null,
)

@Serializable
data class UserStatsResponseJson(
    val success: Boolean = true,
    val message: String? = null,
    val data: UserStatsDataJson? = null,
)

@Serializable
data class UserStatsDataJson(
    @SerialName("packages_count") val packagesCount: Int? = null,
    @SerialName("delivered_count") val deliveredCount: Int? = null,
    @SerialName("average_rating")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val averageRating: Double? = null,
    @SerialName("total_ratings") val totalRatings: Int? = null,
    @SerialName("ratings_count") val ratingsCount: Int? = null,
)
