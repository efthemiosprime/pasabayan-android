package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Domain result of submitting a carrier-side or shipper-side request
 * (initial or counter-offer). Carries:
 *  - the resulting match,
 *  - envelope negotiation metadata (warnings, `negotiationNeeded`,
 *    `isCounterOffer`),
 *  - optional [compatibility] populated on 201 when the backend surfaces a
 *    structured weight/date/route flag set. `null` when the envelope omits
 *    `compatibility` entirely.
 */
data class RequestMatchResult(
    val match: DeliveryMatch,
    val negotiation: NegotiationMetadata,
    val compatibility: MatchCompatibility? = null,
)
