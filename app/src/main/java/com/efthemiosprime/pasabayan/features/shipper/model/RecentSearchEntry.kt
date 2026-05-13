package com.efthemiosprime.pasabayan.features.shipper.model

import kotlinx.serialization.Serializable

/**
 * Single recent-search row persisted by [com.efthemiosprime.pasabayan.features.shipper.services.RecentSearchStore].
 * iOS parity: `PopularSearchEntry` in `ShipperHomeContent.swift`.
 *
 * Identity is the case-insensitive (city, country) pair — used for dedupe on save.
 */
@Serializable
data class RecentSearchEntry(
    val city: String,
    val country: String,
) {
    val displayName: String
        get() = "$city, ${country.uppercase()}"
}
