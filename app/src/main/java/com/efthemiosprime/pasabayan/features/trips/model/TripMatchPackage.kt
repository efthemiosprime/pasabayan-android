package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

data class TripMatchPackage(
    val id: Int,
    val matchStatus: MatchStatus,
    val agreedPrice: Double?,
    val packageDescription: String?,
    val packageWeightKg: Double?,
    val packageId: Int?,
    // iOS parity (`TripPackageInfo`): used by the per-match card row to render pickup→delivery
    // city, fragility chip, and package type.
    val packagePickupCity: String? = null,
    val packageDeliveryCity: String? = null,
    val packageFragile: Boolean? = null,
    val packageType: String? = null,
    val shipper: UserSummary?,
    val chatConversationId: Int?,
    val confirmedAt: String?,
    val pickedUpAt: String?,
    val deliveredAt: String?,
    val createdAt: String?,
)
