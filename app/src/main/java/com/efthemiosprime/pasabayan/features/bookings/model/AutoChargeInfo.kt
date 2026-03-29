package com.efthemiosprime.pasabayan.features.bookings.model

data class AutoChargeInfo(
    val chargeAmount: Double?,
    val chargeStatus: String?,
    val chargeTimestamp: String?,
    val failureReason: String?,
)
