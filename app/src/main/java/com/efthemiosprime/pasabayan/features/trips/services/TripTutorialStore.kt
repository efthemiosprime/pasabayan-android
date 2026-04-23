package com.efthemiosprime.pasabayan.features.trips.services

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-user trip tutorial dismissal state.
 * Key: `trip_tutorial_shown_v1_{userId}`.
 */
@Singleton
class TripTutorialStore @Inject constructor(
    @param:TripsPrefs private val prefs: SharedPreferences,
) {
    private companion object {
        const val KEY_PREFIX = "trip_tutorial_shown_v1_"
    }

    fun hasSeenTutorial(userId: Long): Boolean =
        prefs.getBoolean(storageKey(userId), false)

    fun markTutorialSeen(userId: Long) {
        prefs.edit().putBoolean(storageKey(userId), true).apply()
    }

    fun clearTutorialSeen(userId: Long) {
        prefs.edit().remove(storageKey(userId)).apply()
    }

    fun storageKey(userId: Long): String = "$KEY_PREFIX$userId"
}
