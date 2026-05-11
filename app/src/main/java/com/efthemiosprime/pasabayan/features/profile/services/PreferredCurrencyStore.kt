package com.efthemiosprime.pasabayan.features.profile.services

import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.features.profile.model.CurrencyPreference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user's preferred display currency (CAD/USD/PHP) for amount formatting.
 * Key: `preferred_currency`.
 */
@Singleton
class PreferredCurrencyStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun get(): CurrencyPreference =
        CurrencyPreference.fromCode(prefs.getString(KEY, null))

    fun set(value: CurrencyPreference) {
        prefs.edit().putString(KEY, value.code).apply()
    }

    private companion object {
        const val KEY = "preferred_currency"
    }
}
