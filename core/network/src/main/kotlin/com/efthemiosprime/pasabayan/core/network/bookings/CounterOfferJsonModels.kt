package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for `POST /packages/{packageId}/request-trip/{tripId}`.
 * iOS uses `ShipperCounterOfferRequest` (counter-offer) and
 * `ShipperTripRequest` (initial) — both posted to the same endpoint.
 *
 * Counter-offer fields (`originalMatchId`, `originalPrice`) are included
 * when `isCounterOffer` is true. iOS dropped `counter_offer_round` from
 * the body per commit 781eda5 — the backend assigns it.
 *
 * NOTE — iOS uses `offered_price` here while Android currently sends
 * `proposed_price`. Backend tolerance is unverified; rename deferred.
 */
@Serializable
data class ShipperCounterOfferRequestJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = true,
    @SerialName("original_match_id") val originalMatchId: Int? = null,
    @SerialName("original_price") val originalPrice: Double? = null,
)

/**
 * Body for `POST /trips/{tripId}/packages/{packageId}/request`.
 * Carrier-side initial request and counter-offer both use this shape.
 * iOS `CarrierRequestBody` (commit 8c9646d).
 */
@Serializable
data class CarrierCounterOfferRequestJson(
    @SerialName("proposed_price") val proposedPrice: Double,
    val message: String? = null,
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = true,
    @SerialName("original_match_id") val originalMatchId: Int? = null,
    @SerialName("original_price") val originalPrice: Double? = null,
)

@Serializable
data class CounterOfferResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)
