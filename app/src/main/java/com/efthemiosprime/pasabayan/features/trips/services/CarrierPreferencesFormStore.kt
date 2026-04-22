package com.efthemiosprime.pasabayan.features.trips.services

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarrierPreferencesFormStore @Inject constructor(
    @TripsPrefs private val prefs: SharedPreferences,
) {
    private companion object {
        const val KEY_PREFIX = "carrier_preferences_form_shown_v1_"
    }

    fun isAcknowledged(userId: Long): Boolean = prefs.getBoolean(makeStorageKey(userId), false)

    fun markAcknowledged(userId: Long) {
        prefs.edit().putBoolean(makeStorageKey(userId), true).apply()
    }

    fun clearAcknowledgement(userId: Long) {
        prefs.edit().remove(makeStorageKey(userId)).apply()
    }

    fun clearAllAcknowledgements() {
        val keys = prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }
        prefs.edit().apply {
            keys.forEach { remove(it) }
            apply()
        }
    }

    fun makeStorageKey(userId: Long): String = "$KEY_PREFIX$userId"
}
