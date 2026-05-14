package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for the decline endpoints:
 * - `PUT /matches/{id}/decline-shipper-request` (carrier declines shipper's request)
 * - `PUT /matches/{id}/decline` (shipper declines carrier's offer)
 *
 * Mirrors iOS `CarrierDeclineRequest{reason: String}` and `ShipperDeclineRequest{reason: String?}`
 * — Android collapses both into a single nullable-reason DTO since the only structural
 * difference between them is optionality, and the wire format is identical.
 *
 * Reason is omitted from the JSON when null so the body matches iOS' "no reason" path
 * (which sends an empty `{}` rather than `{"reason": null}`).
 */
@Serializable
data class DeclineMatchRequestJson(
    @SerialName("reason") val reason: String? = null,
)
