package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recent package descriptions — max 15 entries, FIFO, case-insensitive dedup.
 * Key: `saved_package_descriptions_v1`.
 */
@Singleton
class SavedPackageDescriptionsStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val json: Json,
) {
    private val key = "saved_package_descriptions_v1"
    private val maxEntries = 15

    fun getAll(): List<String> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(description: String) {
        val trimmed = description.trim()
        if (trimmed.isEmpty()) return
        val current = getAll().toMutableList()
        // Case-insensitive dedup
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        // Prepend (most recent first)
        current.add(0, trimmed)
        val capped = current.take(maxEntries)
        prefs.edit().putString(key, json.encodeToString(capped)).apply()
    }
}
