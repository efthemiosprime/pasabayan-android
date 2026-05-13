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
 *  - `warnings`: pickup/delivery compatibility warnings to surface in UI.
 *    Weight overage is NOT present here anymore (advisory-weight policy);
 *    use [compatibility] for that.
 *  - `negotiationNeeded`: backend hint that price or details need follow-up
 *  - `isCounterOffer`: envelope-level flag reflecting the submitted body
 *  - `compatibility`: structured weight/date/route flags (see
 *    [MatchCompatibilityJson]). Present on 201 success of request +
 *    counter-offer; also echoed in the 422 capacity-ack-required body.
 */
@Serializable
data class CarrierRequestResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
    val warnings: List<String>? = null,
    @SerialName("negotiation_needed") val negotiationNeeded: Boolean? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean? = null,
    val compatibility: MatchCompatibilityJson? = null,
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
    val compatibility: MatchCompatibilityJson? = null,
)

// -- Inline match compatibility (advisory-weight policy) --

/**
 * Structured compatibility flags returned as a sibling of `data` on the
 * request and counter-offer endpoints, and echoed in the 422 body when the
 * server requires explicit overage acknowledgment.
 *
 * Distinct from [CompatibilityResponseJson] below — that envelope is for the
 * standalone compatibility lookup endpoint and carries a different shape.
 */
@Serializable
data class MatchCompatibilityJson(
    @SerialName("weight_over_capacity") val weightOverCapacity: Boolean = false,
    @SerialName("package_weight_kg") val packageWeightKg: Double? = null,
    @SerialName("trip_available_weight_kg") val tripAvailableWeightKg: Double? = null,
    @SerialName("overage_kg") val overageKg: Double? = null,
    @SerialName("dates_misaligned") val datesMisaligned: Boolean = false,
    @SerialName("route_uncertain") val routeUncertain: Boolean = false,
    @SerialName("requires_capacity_acknowledgment") val requiresCapacityAcknowledgment: Boolean = false,
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

