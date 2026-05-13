package com.efthemiosprime.pasabayan.features.payments.model

data class OtherParty(
    val id: Int = 0,
    val name: String,
    val verificationLevel: String? = null,
)
