package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("picked_up") PICKED_UP,
    @SerialName("in_transit") IN_TRANSIT,
    @SerialName("delivered") DELIVERED,
    @SerialName("cancelled") CANCELLED;

    companion object {
        fun fromMatchStatus(ms: MatchStatus): BookingStatus = when (ms) {
            MatchStatus.PENDING,
            MatchStatus.CARRIER_REQUESTED,
            MatchStatus.SHIPPER_REQUESTED -> PENDING

            MatchStatus.CONFIRMED,
            MatchStatus.SHIPPER_ACCEPTED,
            MatchStatus.CARRIER_ACCEPTED -> CONFIRMED

            MatchStatus.PICKED_UP -> PICKED_UP
            MatchStatus.IN_TRANSIT -> IN_TRANSIT
            MatchStatus.DELIVERED -> DELIVERED

            MatchStatus.CANCELLED,
            MatchStatus.SHIPPER_DECLINED,
            MatchStatus.CARRIER_DECLINED -> CANCELLED
        }
    }
}
