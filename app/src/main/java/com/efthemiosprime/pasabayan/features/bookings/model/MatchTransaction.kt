package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Stripe-backed transaction tied to a delivery match.
 * Parity with iOS `MatchTransaction` from MatchingModels.swift.
 *
 * Amount fields arrive as strings (the backend may serialize numbers or
 * strings; the wire DTO normalizes to String via FlexibleStringSerializer).
 */
data class MatchTransaction(
    val id: Int,
    val status: String?,
    val totalAmount: String?,
    val platformFee: String?,
    val carrierAmount: String?,
    val currency: String?,
    val requiresActionAt: String?,
    val errorCode: String?,
    val errorMessage: String?,
    val createdAt: String?,
)
