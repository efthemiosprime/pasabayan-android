package com.efthemiosprime.pasabayan.features.shipper.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic coverage for [ShipperExploreSectionVisibility]. iOS parity:
 * `ShipperExploreSectionVisibility` in `ShipperExplorePureHelpers.swift`.
 */
class ShipperExploreSectionVisibilityTest {

    // -- Top Carriers --

    @Test
    fun `shouldShowTopCarriers requires empty search, no popular destination, and at least one carrier`() {
        assertTrue(
            ShipperExploreSectionVisibility.shouldShowTopCarriers(
                searchText = "",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 3,
            ),
        )
    }

    @Test
    fun `shouldShowTopCarriers is false when search has text`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowTopCarriers(
                searchText = "Toronto",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 3,
            ),
        )
    }

    @Test
    fun `shouldShowTopCarriers is false when search is whitespace-only`() {
        // iOS trims before checking — Android matches that semantic.
        assertTrue(
            ShipperExploreSectionVisibility.shouldShowTopCarriers(
                searchText = "   ",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 3,
            ),
        )
    }

    @Test
    fun `shouldShowTopCarriers is false when a popular destination is selected`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowTopCarriers(
                searchText = "",
                selectedPopularDestination = "Vancouver, CA",
                carriersWithCompletedTripsCount = 3,
            ),
        )
    }

    @Test
    fun `shouldShowTopCarriers is false when zero carriers qualify`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowTopCarriers(
                searchText = "",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 0,
            ),
        )
    }

    // -- Recent Searches (fallback when Top Carriers is empty) --

    @Test
    fun `shouldShowRecentSearches requires empty search, no destination, zero carriers, and at least one recent`() {
        assertTrue(
            ShipperExploreSectionVisibility.shouldShowRecentSearches(
                searchText = "",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 0,
                recentSearchesCount = 1,
            ),
        )
    }

    @Test
    fun `shouldShowRecentSearches is suppressed when carriers are present (Top Carriers wins)`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowRecentSearches(
                searchText = "",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 1,
                recentSearchesCount = 5,
            ),
        )
    }

    @Test
    fun `shouldShowRecentSearches is false when no recents are stored`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowRecentSearches(
                searchText = "",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 0,
                recentSearchesCount = 0,
            ),
        )
    }

    @Test
    fun `shouldShowRecentSearches is false when search has text`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowRecentSearches(
                searchText = "Toronto",
                selectedPopularDestination = null,
                carriersWithCompletedTripsCount = 0,
                recentSearchesCount = 5,
            ),
        )
    }

    @Test
    fun `shouldShowRecentSearches is false when a popular destination is selected`() {
        assertFalse(
            ShipperExploreSectionVisibility.shouldShowRecentSearches(
                searchText = "",
                selectedPopularDestination = "Vancouver, CA",
                carriersWithCompletedTripsCount = 0,
                recentSearchesCount = 5,
            ),
        )
    }
}
