package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Envelope-level negotiation metadata returned by the request and
 * counter-offer endpoints. Mirrors iOS `CarrierRequestResponse` /
 * `ShipperTripRequestResponse` envelope fields (commit 8c9646d).
 *
 * Distinct from `DeliveryMatch.isCounterOffer` — the envelope flag reflects
 * the *submitted* body, not the resulting match's role.
 */
data class NegotiationMetadata(
    val warnings: List<String>,
    val negotiationNeeded: Boolean,
    val isCounterOffer: Boolean,
) {
    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()

    companion object {
        val EMPTY = NegotiationMetadata(
            warnings = emptyList(),
            negotiationNeeded = false,
            isCounterOffer = false,
        )
    }
}
