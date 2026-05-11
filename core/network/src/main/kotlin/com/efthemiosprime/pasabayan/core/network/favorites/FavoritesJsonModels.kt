package com.efthemiosprime.pasabayan.core.network.favorites

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCarrierJson(
    val id: Int,
    val carrier: FavoriteCarrierInfoJson,
    @SerialName("added_at") val addedAt: String? = null,
    val notes: String? = null,
    @SerialName("notification_enabled") val notificationEnabled: Boolean = true,
    @SerialName("total_deliveries_together") val totalDeliveriesTogether: Int = 0,
    @SerialName("last_delivery_at") val lastDeliveryAt: String? = null,
    @SerialName("last_delivery_route") val lastDeliveryRoute: String? = null,
    @SerialName("has_upcoming_trips") val hasUpcomingTrips: Boolean = false,
    @SerialName("upcoming_trips_count") val upcomingTripsCount: Int = 0,
)

@Serializable
data class FavoriteCarrierInfoJson(
    val id: Int,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
    val rating: Double? = null,
    @SerialName("total_ratings") val totalRatings: Int? = null,
    @SerialName("verification_level") val verificationLevel: String? = null,
    @SerialName("is_active_carrier") val isActiveCarrier: Boolean? = null,
)

@Serializable
data class FavoritesResponseJson(
    val success: Boolean,
    val message: String? = null,
    val data: List<FavoriteCarrierJson> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val remaining: Int = 0,
)

@Serializable
data class AddFavoriteRequestJson(
    val notes: String? = null,
    @SerialName("notification_enabled") val notificationEnabled: Boolean? = null,
)

@Serializable
data class IsFavoriteResponseJson(
    val success: Boolean = true,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
)

@Serializable
data class SendDeliveryRequestJson(
    @SerialName("pickup_city") val pickupCity: String,
    @SerialName("pickup_date_preferred") val pickupDatePreferred: String,
    @SerialName("delivery_city") val deliveryCity: String,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String,
    @SerialName("package_description") val packageDescription: String,
    @SerialName("package_weight_kg") val packageWeightKg: Double,
    @SerialName("package_type") val packageType: String,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("offered_price") val offeredPrice: Double? = null,
    @SerialName("shipper_message") val shipperMessage: String? = null,
)

@Serializable
data class FavoriteCarrierRequestJson(
    val id: Int,
    @SerialName("shipper_id") val shipperId: Int? = null,
    @SerialName("carrier_id") val carrierId: Int? = null,
    val carrier: FavoriteCarrierInfoJson? = null,
    @SerialName("pickup_city") val pickupCity: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("package_weight_kg") val packageWeightKg: Double? = null,
    @SerialName("offered_price") val offeredPrice: Double? = null,
    val status: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("responded_at") val respondedAt: String? = null,
    @SerialName("shipper_message") val shipperMessage: String? = null,
    @SerialName("carrier_response_message") val carrierResponseMessage: String? = null,
)

@Serializable
data class DirectRequestsResponseJson(
    val success: Boolean,
    val data: List<FavoriteCarrierRequestJson> = emptyList(),
    val total: Int = 0,
)
