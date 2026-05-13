package com.efthemiosprime.pasabayan.features.shipper.services

import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.features.shipper.model.RecentSearchEntry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Persists the shipper's recent (city, country) destination searches.
 * iOS parity: `PopularSearchStorage` in `ShipperHomeContent.swift`.
 *
 * Caps at [MAX_ENTRIES] (8) and dedupes case-insensitively by (city, country).
 * Newest entry is always at index 0.
 */
@Singleton
class RecentSearchStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val json: Json,
) {

    fun fetch(): List<RecentSearchEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching { json.decodeFromString(SERIALIZER, raw) }.getOrElse { emptyList() }
    }

    /**
     * Saves a new entry at the head; trims trailing duplicates (case-insensitive on
     * city + country) so the same destination doesn't repeat. No-op when either field
     * is blank after trimming. Returns the updated list (already capped).
     */
    fun save(city: String, country: String): List<RecentSearchEntry> {
        val trimmedCity = city.trim()
        val trimmedCountry = country.trim().uppercase()
        if (trimmedCity.isEmpty() || trimmedCountry.isEmpty()) return fetch()

        val existing = fetch().filterNot {
            it.city.equals(trimmedCity, ignoreCase = true) &&
                it.country.equals(trimmedCountry, ignoreCase = true)
        }
        val updated = (listOf(RecentSearchEntry(trimmedCity, trimmedCountry)) + existing)
            .take(MAX_ENTRIES)
        prefs.edit().putString(KEY, json.encodeToString(SERIALIZER, updated)).apply()
        return updated
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        const val KEY: String = "shipper_recent_searches"
        const val MAX_ENTRIES: Int = 8
        private val SERIALIZER = ListSerializer(RecentSearchEntry.serializer())
    }
}
