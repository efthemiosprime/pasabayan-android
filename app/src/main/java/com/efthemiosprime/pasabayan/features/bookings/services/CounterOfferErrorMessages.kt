package com.efthemiosprime.pasabayan.features.bookings.services

import android.content.Context
import com.efthemiosprime.pasabayan.R

/**
 * Localized strings used by [CounterOfferErrorMapper]. Resolved from
 * `Context` in the UI layer; passed directly in JVM tests.
 */
data class CounterOfferErrorMessages(
    val limitReached: String,
    val signInAgain: String,
    val networkError: String,
    val notFound: String,
    val rateLimited: String,
) {
    companion object {
        fun from(context: Context): CounterOfferErrorMessages = CounterOfferErrorMessages(
            limitReached = context.getString(R.string.bookings_counter_offer_error_limit_reached),
            signInAgain = context.getString(R.string.bookings_counter_offer_error_sign_in),
            networkError = context.getString(R.string.bookings_counter_offer_error_network),
            notFound = context.getString(R.string.bookings_counter_offer_error_not_found),
            rateLimited = context.getString(R.string.bookings_counter_offer_error_rate_limited),
        )
    }
}
