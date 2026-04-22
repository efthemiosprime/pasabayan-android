package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripFilterTest {
    @Test
    fun `toQueryMap includes pagination only after first page`() {
        val firstPage = TripFilter(page = 1).toQueryMap()
        val secondPage = TripFilter(page = 2).toQueryMap()

        assertFalse(firstPage.containsKey("page"))
        assertEquals("2", secondPage["page"])
    }

    @Test
    fun `toQueryMap includes core search and sort params`() {
        val params = TripFilter(
            searchText = "Toronto",
            origin = "Toronto",
            destination = "Ottawa",
            sortOrder = SortOrder.DESCENDING,
        ).toQueryMap()

        assertEquals("Toronto", params["q"])
        assertEquals("Toronto", params["origin"])
        assertEquals("Ottawa", params["destination"])
        assertEquals("desc", params["sort_order"])
        assertTrue(params.containsKey("sort_by"))
    }
}
