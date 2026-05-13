package com.efthemiosprime.pasabayan.features.trips.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic coverage for [CarrierExploreDropdownState]. iOS parity:
 * `Pasabayan/Features/RouteActivity/Models/CarrierExploreDropdownState.swift`.
 */
class CarrierExploreDropdownStateTest {

    // -- showRecentOption --

    @Test
    fun `showRecentOption is false below threshold`() {
        assertFalse(CarrierExploreDropdownState.showRecentOption(0))
        assertFalse(CarrierExploreDropdownState.showRecentOption(4))
    }

    @Test
    fun `showRecentOption is true at and above threshold`() {
        assertTrue(CarrierExploreDropdownState.showRecentOption(5))
        assertTrue(CarrierExploreDropdownState.showRecentOption(99))
    }

    // -- showPopularOption --

    @Test
    fun `showPopularOption is false when there are zero routes`() {
        assertFalse(CarrierExploreDropdownState.showPopularOption(0))
    }

    @Test
    fun `showPopularOption is true with at least one route`() {
        assertTrue(CarrierExploreDropdownState.showPopularOption(1))
        assertTrue(CarrierExploreDropdownState.showPopularOption(50))
    }

    // -- shouldShowDropdown --

    @Test
    fun `shouldShowDropdown requires focus, empty text, and at least one qualifying section`() {
        assertTrue(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = true,
                isSearchEmpty = true,
                showRecentOption = true,
                showPopularOption = false,
            ),
        )
        assertTrue(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = true,
                isSearchEmpty = true,
                showRecentOption = false,
                showPopularOption = true,
            ),
        )
        assertTrue(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = true,
                isSearchEmpty = true,
                showRecentOption = true,
                showPopularOption = true,
            ),
        )
    }

    @Test
    fun `shouldShowDropdown is false when search is not focused`() {
        assertFalse(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = false,
                isSearchEmpty = true,
                showRecentOption = true,
                showPopularOption = true,
            ),
        )
    }

    @Test
    fun `shouldShowDropdown is false when search has text`() {
        assertFalse(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = true,
                isSearchEmpty = false,
                showRecentOption = true,
                showPopularOption = true,
            ),
        )
    }

    @Test
    fun `shouldShowDropdown is false when neither section qualifies`() {
        assertFalse(
            CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = true,
                isSearchEmpty = true,
                showRecentOption = false,
                showPopularOption = false,
            ),
        )
    }
}
