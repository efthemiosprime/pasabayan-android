package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageTutorialStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun hasSeenTutorial(userId: Long): Boolean =
        prefs.getBoolean(key(userId), false)

    fun markTutorialSeen(userId: Long) {
        prefs.edit().putBoolean(key(userId), true).apply()
    }

    private fun key(userId: Long): String = "packages_tutorial_seen_v1_$userId"
}
