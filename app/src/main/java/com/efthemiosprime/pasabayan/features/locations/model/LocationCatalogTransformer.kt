package com.efthemiosprime.pasabayan.features.locations.model

import com.efthemiosprime.pasabayan.core.network.location.LocationCatalogResponseJson

/**
 * Flattens the API tree into the four lookups used by autocomplete and reverse-lookup. Mirrors
 * `LocationCatalogService.transformAndPersist`. Pure / testable.
 */
object LocationCatalogTransformer {

    fun fromResponse(response: LocationCatalogResponseJson): LocationCatalogSnapshot {
        val newCityData = mutableMapOf<String, MutableList<String>>()
        val newAliasData = mutableMapOf<String, String>()
        val newMetroAreas = mutableMapOf<String, MutableList<String>>()
        val newCityIds = mutableMapOf<String, Int>()

        for (country in response.data) {
            val countryCode = country.iso2
            for (state in country.states) {
                val stateCode = state.code
                for (city in state.cities) {
                    val display = "${city.name}, $stateCode"
                    newCityData.getOrPut(countryCode, ::mutableListOf).add(display)
                    newCityIds[display] = city.id

                    city.aliases?.forEach { alias ->
                        newAliasData[alias.alias] = display
                        if (alias.type == "metro") {
                            newMetroAreas.getOrPut(countryCode, ::mutableListOf).add(alias.alias)
                        }
                    }
                }
            }
        }

        val sortedCityData = newCityData.mapValues { (_, list) -> list.distinct().sorted() }

        return LocationCatalogSnapshot(
            version = response.version,
            cityData = sortedCityData,
            aliasData = newAliasData.toMap(),
            metroAreas = newMetroAreas.mapValues { (_, v) -> v.distinct() },
            cityIds = newCityIds.toMap(),
        )
    }
}
