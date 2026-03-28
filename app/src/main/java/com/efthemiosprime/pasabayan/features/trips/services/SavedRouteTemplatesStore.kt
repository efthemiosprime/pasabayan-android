package com.efthemiosprime.pasabayan.features.trips.services

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SavedRouteTemplate(
    val startCountryCode: String,
    val startLocation: String,
    val pickupAddress: String? = null,
    val endCountryCode: String,
    val endLocation: String,
    val dropoffAddress: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val displayLabel: String
        get() = "$startLocation → $endLocation"
}

/**
 * Max 5 saved route templates, sorted by date desc, drops oldest when 6th added.
 * Key: `saved_route_templates_v1`.
 */
@Singleton
class SavedRouteTemplatesStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val json: Json,
) {
    private val key = "saved_route_templates_v1"
    private val maxTemplates = 5

    fun getAll(): List<SavedRouteTemplate> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<SavedRouteTemplate>>(raw)
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(template: SavedRouteTemplate) {
        val current = getAll().toMutableList()
        current.add(0, template)
        val trimmed = current.take(maxTemplates)
        prefs.edit().putString(key, json.encodeToString(trimmed)).apply()
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }
}
