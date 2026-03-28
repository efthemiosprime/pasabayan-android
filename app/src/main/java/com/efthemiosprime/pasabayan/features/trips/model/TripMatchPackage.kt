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
    val shipper: UserSummary?,
    val chatConversationId: Int?,
    val confirmedAt: String?,
    val pickedUpAt: String?,
    val deliveredAt: String?,
    val createdAt: String?,
)
