package com.efthemiosprime.pasabayan.features.locations.services

import android.content.Context
import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.features.locations.model.LocationCatalogSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the transformed catalog so the API isn't re-hit on every cold start. iOS parity:
 * `PersistedCatalog` on disk.
 */
interface LocationCatalogStore {
    fun load(): LocationCatalogSnapshot?
    fun save(snapshot: LocationCatalogSnapshot)
    fun clear()
}

@Singleton
class SharedPreferencesLocationCatalogStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) : LocationCatalogStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): LocationCatalogSnapshot? {
        val raw = prefs.getString(KEY_CATALOG, null)?.takeIf { it.isNotBlank() } ?: return null
        val persisted = runCatching {
            json.decodeFromString(PersistedLocationCatalog.serializer(), raw)
        }.getOrNull() ?: return null
        return LocationCatalogSnapshot(
            version = persisted.version,
            cityData = persisted.cityData,
            aliasData = persisted.aliasData,
            metroAreas = persisted.metroAreas,
            cityIds = persisted.cityIds,
        )
    }

    override fun save(snapshot: LocationCatalogSnapshot) {
        val encoded = json.encodeToString(
            PersistedLocationCatalog.serializer(),
            PersistedLocationCatalog(
                version = snapshot.version,
                cityData = snapshot.cityData,
                aliasData = snapshot.aliasData,
                metroAreas = snapshot.metroAreas,
                cityIds = snapshot.cityIds,
            ),
        )
        prefs.edit().putString(KEY_CATALOG, encoded).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_CATALOG).apply()
    }

    private companion object {
        const val PREFS_NAME = "pasabayan_locations"
        const val KEY_CATALOG = "location_catalog"
    }
}

@Serializable
internal data class PersistedLocationCatalog(
    val version: String,
    val cityData: Map<String, List<String>>,
    val aliasData: Map<String, String>,
    val metroAreas: Map<String, List<String>>,
    val cityIds: Map<String, Int>,
)
