package com.efthemiosprime.pasabayan.features.locations.model

import com.efthemiosprime.pasabayan.core.network.location.CatalogAliasJson
import com.efthemiosprime.pasabayan.core.network.location.CatalogCityJson
import com.efthemiosprime.pasabayan.core.network.location.CatalogCountryJson
import com.efthemiosprime.pasabayan.core.network.location.CatalogStateJson
import com.efthemiosprime.pasabayan.core.network.location.LocationCatalogResponseJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationCatalogTransformerTest {

    @Test
    fun `flattens countries into cityData and cityIds`() {
        val response = LocationCatalogResponseJson(
            success = true,
            version = "2026.05.11",
            data = listOf(
                CatalogCountryJson(
                    iso2 = "CA",
                    states = listOf(
                        CatalogStateJson(
                            code = "ON",
                            cities = listOf(
                                CatalogCityJson(id = 1, name = "Toronto"),
                                CatalogCityJson(id = 2, name = "Ottawa"),
                            ),
                        ),
                        CatalogStateJson(
                            code = "QC",
                            cities = listOf(
                                CatalogCityJson(id = 3, name = "Montreal"),
                            ),
                        ),
                    ),
                ),
                CatalogCountryJson(
                    iso2 = "US",
                    states = listOf(
                        CatalogStateJson(
                            code = "NY",
                            cities = listOf(CatalogCityJson(id = 11, name = "New York")),
                        ),
                    ),
                ),
            ),
        )

        val snapshot = LocationCatalogTransformer.fromResponse(response)
        assertEquals("2026.05.11", snapshot.version)
        // Cities are sorted alphabetically per country
        assertEquals(
            listOf("Montreal, QC", "Ottawa, ON", "Toronto, ON"),
            snapshot.cityData["CA"],
        )
        assertEquals(listOf("New York, NY"), snapshot.cityData["US"])
        assertEquals(3, snapshot.cityIds["Montreal, QC"])
        assertEquals(11, snapshot.cityIds["New York, NY"])
        assertEquals(4, snapshot.totalCityCount)
    }

    @Test
    fun `aliases populate aliasData and metro areas`() {
        val response = LocationCatalogResponseJson(
            success = true,
            version = "1",
            data = listOf(
                CatalogCountryJson(
                    iso2 = "CA",
                    states = listOf(
                        CatalogStateJson(
                            code = "ON",
                            cities = listOf(
                                CatalogCityJson(
                                    id = 87,
                                    name = "Toronto",
                                    aliases = listOf(
                                        CatalogAliasJson(alias = "GTA", type = "metro"),
                                        CatalogAliasJson(alias = "T-Dot", type = "nickname"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val snapshot = LocationCatalogTransformer.fromResponse(response)
        assertEquals("Toronto, ON", snapshot.aliasData["GTA"])
        assertEquals("Toronto, ON", snapshot.aliasData["T-Dot"])
        assertEquals(listOf("GTA"), snapshot.metroAreas["CA"])
    }

    @Test
    fun `cityIdFor resolves alias to canonical city id`() {
        val snapshot = LocationCatalogSnapshot(
            version = "1",
            cityData = mapOf("CA" to listOf("Toronto, ON")),
            aliasData = mapOf("GTA" to "Toronto, ON"),
            metroAreas = mapOf("CA" to listOf("GTA")),
            cityIds = mapOf("Toronto, ON" to 87),
        )
        assertEquals(87, snapshot.cityIdFor("Toronto, ON"))
        assertEquals(87, snapshot.cityIdFor("GTA"))
        assertNull(snapshot.cityIdFor("Unknown"))
    }

    @Test
    fun `displayNameFor returns canonical for a city id`() {
        val snapshot = LocationCatalogSnapshot(
            version = "1",
            cityData = emptyMap(),
            aliasData = emptyMap(),
            metroAreas = emptyMap(),
            cityIds = mapOf("Toronto, ON" to 87, "Montreal, QC" to 9),
        )
        assertEquals("Toronto, ON", snapshot.displayNameFor(87))
        assertEquals("Montreal, QC", snapshot.displayNameFor(9))
        assertNull(snapshot.displayNameFor(999))
    }

    @Test
    fun `duplicate cities across states are deduplicated within a country`() {
        val response = LocationCatalogResponseJson(
            success = true,
            version = "1",
            data = listOf(
                CatalogCountryJson(
                    iso2 = "CA",
                    states = listOf(
                        CatalogStateJson(
                            code = "ON",
                            cities = listOf(
                                CatalogCityJson(id = 1, name = "London"),
                            ),
                        ),
                        CatalogStateJson(
                            code = "ON",
                            cities = listOf(
                                CatalogCityJson(id = 1, name = "London"),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val snapshot = LocationCatalogTransformer.fromResponse(response)
        assertEquals(listOf("London, ON"), snapshot.cityData["CA"])
        assertTrue(snapshot.aliasData.isEmpty())
    }
}
