package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// -- Carrier request flow --

@Serializable
data class CarrierRequestBodyJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = false,
)

@Serializable
data class CarrierRequestResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: CarrierRequestDataJson? = null,
)

@Serializable
data class CarrierRequestDataJson(
    val id: Int,
    @SerialName("match_id") val matchId: Int? = null,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("package_request_id") val packageRequestId: Int? = null,
    @SerialName("proposed_price") @Serializable(with = FlexibleDoubleSerializer::class) val proposedPrice: Double? = null,
    val message: String? = null,
    val status: String? = null,
    val carrier: UserSummary? = null,
    val shipper: UserSummary? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// -- Shipper request flow --

@Serializable
data class ShipperTripRequestJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = false,
)

@Serializable
data class ShipperTripRequestResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)

// -- Location tracking --

@Serializable
data class UpdateLocationRequestJson(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double? = null,
)

@Serializable
data class CarrierLocationDataResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: CarrierLocationDataJson? = null,
)

@Serializable
data class CarrierLocationDataJson(
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("last_updated_at") val lastUpdatedAt: String? = null,
    val carrier: UserSummary? = null,
)

// -- Receiver access --

@Serializable
data class ReceiverAccessResponseJson(
    val success: Boolean = false,
    val message: String = "",
)

// -- Receipt --

@Serializable
data class ReceiptResponseJson(
    val success: Boolean = false,
    val message: String = "",
    @SerialName("receipt_url") val receiptUrl: String? = null,
)

// -- Compatibility --

@Serializable
data class CompatibilityResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: CompatibilityDataJson? = null,
)

@Serializable
data class CompatibilityDataJson(
    val compatible: Boolean = false,
    val score: Int? = null,
    @SerialName("route_match") val routeMatch: String? = null,
    @SerialName("capacity_sufficient") val capacitySufficient: Boolean = false,
    @SerialName("date_compatible") val dateCompatible: Boolean = false,
    @SerialName("price_compatible") val priceCompatible: Boolean = false,
)
