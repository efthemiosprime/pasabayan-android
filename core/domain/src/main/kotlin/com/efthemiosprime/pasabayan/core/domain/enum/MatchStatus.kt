package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("picked_up") PICKED_UP,
    @SerialName("in_transit") IN_TRANSIT,
    @SerialName("delivered") DELIVERED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("carrier_requested") CARRIER_REQUESTED,
    @SerialName("shipper_requested") SHIPPER_REQUESTED,
    @SerialName("shipper_accepted") SHIPPER_ACCEPTED,
    @SerialName("shipper_declined") SHIPPER_DECLINED,
    @SerialName("carrier_accepted") CARRIER_ACCEPTED,
    @SerialName("carrier_declined") CARRIER_DECLINED;

    val isCancellable: Boolean
        get() = when (this) {
            CARRIER_REQUESTED, SHIPPER_REQUESTED,
            SHIPPER_ACCEPTED, CARRIER_ACCEPTED, PENDING -> true
            CONFIRMED, PICKED_UP, IN_TRANSIT, DELIVERED,
            CANCELLED, SHIPPER_DECLINED, CARRIER_DECLINED -> false
        }
}
