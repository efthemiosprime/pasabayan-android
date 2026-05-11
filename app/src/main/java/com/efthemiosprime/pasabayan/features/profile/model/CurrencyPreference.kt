package com.efthemiosprime.pasabayan.features.profile.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * Display currency for amount formatting (per `09-profile-carrier-consent` § Currency selection).
 * Stored in shared preferences; the API still stores the canonical currency per transaction —
 * this only governs how amounts are rendered to the user.
 */
enum class CurrencyPreference(
    val code: String,
    val symbol: String,
    @StringRes val labelRes: Int,
) {
    CAD("CAD", "$", R.string.profile_currency_cad),
    USD("USD", "$", R.string.profile_currency_usd),
    PHP("PHP", "₱", R.string.profile_currency_php),
    ;

    companion object {
        val Default = CAD
        fun fromCode(code: String?): CurrencyPreference =
            entries.firstOrNull { it.code == code } ?: Default
    }
}
