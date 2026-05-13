package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for the accept endpoints:
 * - `PUT /matches/{id}/accept-shipper-request` (carrier accepts shipper's request)
 * - `PUT /matches/{id}/accept-carrier-request` (shipper accepts carrier's offer)
 *
 * `acknowledgeOverage` is only set when the accepting party has explicitly
 * confirmed an over-capacity match via the confirmation sheet. Omitted (null)
 * on the fitting-weight happy path so the server treats it the same as
 * `false`.
 */
@Serializable
data class AcceptMatchRequestJson(
    val message: String? = null,
    @SerialName("acknowledge_overage") val acknowledgeOverage: Boolean? = null,
)
