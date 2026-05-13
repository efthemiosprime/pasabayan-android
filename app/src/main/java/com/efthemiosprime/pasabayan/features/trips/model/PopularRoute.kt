package com.efthemiosprime.pasabayan.features.trips.model

data class PopularRoute(
    val originCity: String,
    val destinationCity: String,
    val packageCount: Int,
    val averagePrice: Double?,
    /**
     * Destination country code (e.g. "CA", "PH"). Null when the API didn't include
     * it. Used to persist a recent search after the shipper taps a popular route —
     * iOS parity with `PopularSearchEntry { city, country }`.
     */
    val destinationCountry: String? = null,
)
