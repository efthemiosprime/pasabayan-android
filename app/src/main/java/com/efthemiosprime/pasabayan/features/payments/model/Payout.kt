package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus

data class Payout(
    val status: PayoutStatus? = null,
    val notes: String? = null,
    val completedAt: String? = null,
)
