package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// -- Carrier request envelope --

/**
 * Envelope returned by `POST /trips/{tripId}/packages/{packageId}/request`.
 * Mirrors iOS `CarrierRequestResponse` (commit 8c9646d).
 *
 * Carries the resulting DeliveryMatch alongside negotiation metadata:
 *  - `warnings`: pickup/delivery compatibility warnings to surface in UI
 *  - `negotiationNeeded`: backend hint that price or details need follow-up
 *  - `isCounterOffer`: envelope-level flag reflecting the submitted body
 */
@Serializable
data class CarrierRequestResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
    val warnings: List<String>? = null,
    @SerialName("negotiation_needed") val negotiationNeeded: Boolean? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean? = null,
)

// -- Shipper request envelope --

/**
 * Envelope returned by `POST /packages/{packageId}/request-trip/{tripId}`.
 * Mirrors iOS `ShipperTripRequestResponse` (commit 8c9646d).
 */
@Serializable
data class ShipperRequestResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
    val warnings: List<String>? = null,
    @SerialName("negotiation_needed") val negotiationNeeded: Boolean? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean? = null,
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

