package com.efthemiosprime.pasabayan.features.locations.model

/**
 * In-memory derived view of the location catalog (`GET /locations/catalog`). Maps the raw
 * country → state → city tree into the four lookups iOS keeps on
 * `LocationCatalogService`. iOS parity: `PersistedCatalog`.
 */
data class LocationCatalogSnapshot(
    val version: String,
    /** ISO2 country code → list of "City, ST" display strings sorted alphabetically. */
    val cityData: Map<String, List<String>>,
    /** alias text → resolved "City, ST" display. */
    val aliasData: Map<String, String>,
    /** ISO2 country code → list of metro alias texts (subset of [aliasData] keys). */
    val metroAreas: Map<String, List<String>>,
    /** "City, ST" → backend city id. */
    val cityIds: Map<String, Int>,
) {
    val totalCityCount: Int get() = cityData.values.sumOf { it.size }

    /** Lookup the backend city id for a display name; resolves aliases. */
    fun cityIdFor(displayOrAlias: String): Int? {
        cityIds[displayOrAlias]?.let { return it }
        return aliasData[displayOrAlias]?.let { cityIds[it] }
    }

    /** Reverse lookup: display name (e.g. "Montreal, QC") for a known city id. */
    fun displayNameFor(cityId: Int): String? =
        cityIds.entries.firstOrNull { it.value == cityId }?.key

    companion object {
        val Empty = LocationCatalogSnapshot(
            version = "",
            cityData = emptyMap(),
            aliasData = emptyMap(),
            metroAreas = emptyMap(),
            cityIds = emptyMap(),
        )
    }
}
