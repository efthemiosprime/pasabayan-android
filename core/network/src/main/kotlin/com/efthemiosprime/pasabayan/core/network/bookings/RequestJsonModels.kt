package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
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

/**
 * Envelope for `GET /matches/{matchId}/carrier-location`. Mirrors iOS
 * `CarrierLocationDataResponse` — `success` + nested [CarrierLocationDataJson]
 * snapshot. The nested snapshot is iOS-parity exact: `current_location` and
 * `delivery_address` are *separate* nested objects rather than flattened.
 */
@Serializable
data class CarrierLocationDataResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: CarrierLocationDataJson? = null,
)

/**
 * Carrier location snapshot. Mirrors iOS `CarrierLocationResponse` exactly:
 *  - `matchId` plus optional `carrier` (UserSummary parity with `CarrierBasicInfo`)
 *  - `currentLocation`: lat/lng/lastUpdatedAt/isStale (omitted before the
 *    carrier has shared a location, hence the optional nesting)
 *  - `deliveryAddress`: address/city + **String** lat/lng (server quirk —
 *    the mapper converts them to Double).
 */
@Serializable
data class CarrierLocationDataJson(
    @SerialName("match_id") val matchId: Int? = null,
    val carrier: UserSummary? = null,
    @SerialName("current_location") val currentLocation: CurrentLocationJson? = null,
    @SerialName("delivery_address") val deliveryAddress: DeliveryAddressJson? = null,
    @SerialName("match_status") val matchStatus: MatchStatus? = null,
)

@Serializable
data class CurrentLocationJson(
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("last_updated_at") val lastUpdatedAt: String? = null,
    @SerialName("is_stale") val isStale: Boolean? = null,
)

@Serializable
data class DeliveryAddressJson(
    val address: String? = null,
    val city: String? = null,
    /** Server sends these as Strings; mapper converts via `toDoubleOrNull`. */
    val latitude: String? = null,
    val longitude: String? = null,
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

