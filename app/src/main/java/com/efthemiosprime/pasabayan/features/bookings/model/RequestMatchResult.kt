package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Domain result of submitting a carrier-side or shipper-side request
 * (initial or counter-offer). Carries both the resulting match and the
 * envelope negotiation metadata so the UI can surface warnings, gate the
 * next action on `negotiationNeeded`, and confirm `isCounterOffer`.
 */
data class RequestMatchResult(
    val match: DeliveryMatch,
    val negotiation: NegotiationMetadata,
)
