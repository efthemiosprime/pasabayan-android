package com.efthemiosprime.pasabayan.features.bookings.model

data class MatchTransaction(
    val id: Int,
    val matchId: Int,
    val amount: Double,
    val currency: String,
    val status: String,
    val type: String,
    val createdAt: String,
    val updatedAt: String,
)
