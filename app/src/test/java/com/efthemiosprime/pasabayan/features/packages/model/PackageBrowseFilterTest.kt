package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the iOS-parity wire contract for `GET /packages/available` query params.
 * If the backend changes a key (e.g. `max_budget` → `max_price`), these tests
 * fail loudly instead of silently dropping the filter.
 */
class PackageBrowseFilterTest {

    @Test
    fun `empty filter produces empty query map`() {
        assertTrue(PackageBrowseFilter().toQueryMap().isEmpty())
    }

    @Test
    fun `isEmpty true for default filter`() {
        assertTrue(PackageBrowseFilter().isEmpty)
    }

    @Test
    fun `isEmpty false when any field is set`() {
        assertFalse(PackageBrowseFilter(searchText = "x").isEmpty)
        assertFalse(PackageBrowseFilter(urgency = UrgencyLevel.HIGH).isEmpty)
        assertFalse(PackageBrowseFilter(packageType = PackageType.ELECTRONICS).isEmpty)
        assertFalse(PackageBrowseFilter(maxWeight = "5").isEmpty)
        assertFalse(PackageBrowseFilter(maxPrice = "100").isEmpty)
    }

    @Test
    fun `searchText maps to q and trims whitespace`() {
        val q = PackageBrowseFilter(searchText = "  laptop  ").toQueryMap()
        assertEquals(mapOf("q" to "laptop"), q)
    }

    @Test
    fun `blank searchText is omitted`() {
        assertTrue(PackageBrowseFilter(searchText = "   ").toQueryMap().isEmpty())
    }

    @Test
    fun `urgency maps to lowercase wire value`() {
        val q = PackageBrowseFilter(urgency = UrgencyLevel.URGENT).toQueryMap()
        assertEquals(mapOf("urgency" to "urgent"), q)
    }

    @Test
    fun `maxWeight maps to max_weight when parseable`() {
        val q = PackageBrowseFilter(maxWeight = "12.5").toQueryMap()
        assertEquals(mapOf("max_weight" to "12.5"), q)
    }

    @Test
    fun `maxPrice maps to max_budget when parseable`() {
        // iOS-parity: the UI label says "max price" but the wire key is `max_budget`.
        val q = PackageBrowseFilter(maxPrice = "150").toQueryMap()
        assertEquals(mapOf("max_budget" to "150.0"), q)
    }

    @Test
    fun `unparseable maxWeight is silently dropped`() {
        // Filter state holds raw user input; bad input must not poison the request.
        assertTrue(PackageBrowseFilter(maxWeight = "abc").toQueryMap().isEmpty())
    }

    @Test
    fun `packageType is never sent server-side`() {
        // Client-side filter only (iOS // TODO until backend support lands).
        val q = PackageBrowseFilter(packageType = PackageType.ELECTRONICS).toQueryMap()
        assertTrue(q.isEmpty())
    }

    @Test
    fun `full filter combines all server-side fields`() {
        val q = PackageBrowseFilter(
            searchText = "doc",
            urgency = UrgencyLevel.HIGH,
            packageType = PackageType.ELECTRONICS, // ignored
            maxWeight = "10",
            maxPrice = "200",
        ).toQueryMap()

        assertEquals(
            mapOf(
                "q" to "doc",
                "urgency" to "high",
                "max_weight" to "10.0",
                "max_budget" to "200.0",
            ),
            q,
        )
    }
}
