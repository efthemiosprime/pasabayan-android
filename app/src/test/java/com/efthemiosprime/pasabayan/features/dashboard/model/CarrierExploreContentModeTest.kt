package com.efthemiosprime.pasabayan.features.dashboard.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure-logic coverage for [CarrierExploreContentMode] and the [browseMode] mapping
 * to [CarrierBrowseContentMode]. iOS parity:
 * `CarrierExploreContentMode` + `browseModeFromExploreMode` in `CarrierHomeContent.swift`.
 */
class CarrierExploreContentModeTest {

    @Test
    fun `Nearby maps to Nearby browse mode`() {
        assertEquals(
            CarrierBrowseContentMode.Nearby,
            CarrierExploreContentMode.Nearby.browseMode,
        )
    }

    @Test
    fun `Recent maps to Recent browse mode`() {
        assertEquals(
            CarrierBrowseContentMode.Recent,
            CarrierExploreContentMode.Recent.browseMode,
        )
    }

    @Test
    fun `PopularList collapses to Nearby browse mode (iOS parity)`() {
        // The popular-route picker doesn't filter packages itself — only the
        // user's subsequent tap on a route does. iOS does the same collapse.
        assertEquals(
            CarrierBrowseContentMode.Nearby,
            CarrierExploreContentMode.PopularList.browseMode,
        )
    }

    @Test
    fun `PopularDestination preserves the displayName as Destination`() {
        val mode = CarrierExploreContentMode.PopularDestination("Manila, PH")
        assertEquals(
            CarrierBrowseContentMode.Destination("Manila, PH"),
            mode.browseMode,
        )
    }

    @Test
    fun `Search preserves the query`() {
        val mode = CarrierExploreContentMode.Search("Cebu")
        assertEquals(
            CarrierBrowseContentMode.Search("Cebu"),
            mode.browseMode,
        )
    }

    @Test
    fun `data classes use value equality`() {
        assertEquals(
            CarrierExploreContentMode.PopularDestination("Manila, PH"),
            CarrierExploreContentMode.PopularDestination("Manila, PH"),
        )
        assertEquals(
            CarrierExploreContentMode.Search("Cebu"),
            CarrierExploreContentMode.Search("Cebu"),
        )
    }

    @Test
    fun `data objects are singletons`() {
        // Reference equality on data objects.
        assert(CarrierExploreContentMode.Nearby === CarrierExploreContentMode.Nearby)
        assert(CarrierExploreContentMode.Recent === CarrierExploreContentMode.Recent)
        assert(CarrierExploreContentMode.PopularList === CarrierExploreContentMode.PopularList)
    }

    @Test
    fun `PopularDestination with different names are distinct`() {
        assert(
            CarrierExploreContentMode.PopularDestination("Manila, PH") !=
                CarrierExploreContentMode.PopularDestination("Cebu, PH")
        )
    }
}
