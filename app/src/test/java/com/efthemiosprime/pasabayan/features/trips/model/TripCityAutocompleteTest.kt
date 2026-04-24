package com.efthemiosprime.pasabayan.features.trips.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripCityAutocompleteTest {

    @Test
    fun `suggestions returns empty when query blank`() {
        val result = TripCityAutocomplete.suggestions("")

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `suggestions returns empty when city already exact match`() {
        val result = TripCityAutocomplete.suggestions("toronto")

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `suggestions returns matching popular cities when partial query`() {
        val result = TripCityAutocomplete.suggestions("to")

        assertTrue(result.contains("Toronto"))
    }

    @Test
    fun `suggestions honors result limit`() {
        val result = TripCityAutocomplete.suggestions("a", limit = 2)

        assertEquals(2, result.size)
    }
}
