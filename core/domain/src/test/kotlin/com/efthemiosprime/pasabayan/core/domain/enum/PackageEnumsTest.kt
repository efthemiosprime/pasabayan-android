package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageEnumsTest {

    private val json = Json { ignoreUnknownKeys = true }

    // -- MatchStatus --

    @Test
    fun `MatchStatus serialization round-trip`() {
        for (status in MatchStatus.entries) {
            val encoded = json.encodeToString(MatchStatus.serializer(), status)
            val decoded = json.decodeFromString(MatchStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `MatchStatus has 12 cases`() {
        assertEquals(12, MatchStatus.entries.size)
    }

    @Test
    fun `MatchStatus isCancellable correct for cancellable statuses`() {
        val cancellable = listOf(
            MatchStatus.PENDING,
            MatchStatus.CARRIER_REQUESTED,
            MatchStatus.SHIPPER_REQUESTED,
            MatchStatus.SHIPPER_ACCEPTED,
            MatchStatus.CARRIER_ACCEPTED,
        )
        for (status in cancellable) {
            assertTrue("$status should be cancellable", status.isCancellable)
        }
    }

    @Test
    fun `MatchStatus isCancellable false for non-cancellable statuses`() {
        val nonCancellable = listOf(
            MatchStatus.CONFIRMED,
            MatchStatus.PICKED_UP,
            MatchStatus.IN_TRANSIT,
            MatchStatus.DELIVERED,
            MatchStatus.CANCELLED,
            MatchStatus.SHIPPER_DECLINED,
            MatchStatus.CARRIER_DECLINED,
        )
        for (status in nonCancellable) {
            assertFalse("$status should not be cancellable", status.isCancellable)
        }
    }

    // -- PackageRequestStatus --

    @Test
    fun `PackageRequestStatus serialization round-trip`() {
        for (status in PackageRequestStatus.entries) {
            val encoded = json.encodeToString(PackageRequestStatus.serializer(), status)
            val decoded = json.decodeFromString(PackageRequestStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `PackageRequestStatus has 9 cases`() {
        assertEquals(9, PackageRequestStatus.entries.size)
    }

    @Test
    fun `PackageRequestStatus decodes legacy values`() {
        assertEquals(
            PackageRequestStatus.PENDING,
            json.decodeFromString(PackageRequestStatus.serializer(), "\"pending\""),
        )
        assertEquals(
            PackageRequestStatus.BOOKED,
            json.decodeFromString(PackageRequestStatus.serializer(), "\"booked\""),
        )
        assertEquals(
            PackageRequestStatus.IN_TRANSIT,
            json.decodeFromString(PackageRequestStatus.serializer(), "\"in_transit\""),
        )
    }

    // -- PackageType --

    @Test
    fun `PackageType serialization round-trip`() {
        for (pt in PackageType.entries) {
            val encoded = json.encodeToString(PackageType.serializer(), pt)
            val decoded = json.decodeFromString(PackageType.serializer(), encoded)
            assertEquals(pt, decoded)
        }
    }

    @Test
    fun `PackageType has 22 cases`() {
        assertEquals(22, PackageType.entries.size)
    }

    @Test
    fun `PackageType icon non-empty for all`() {
        for (pt in PackageType.entries) {
            assertTrue("$pt should have an icon", pt.icon.isNotEmpty())
        }
    }

    @Test
    fun `PackageType document alias decodes`() {
        assertEquals(
            PackageType.DOCUMENT,
            json.decodeFromString(PackageType.serializer(), "\"document\""),
        )
        assertEquals(
            PackageType.DOCUMENTS,
            json.decodeFromString(PackageType.serializer(), "\"documents\""),
        )
    }

    @Test
    fun `PackageType DOCUMENTS and DOCUMENT share same icon`() {
        assertEquals(PackageType.DOCUMENTS.icon, PackageType.DOCUMENT.icon)
    }

    // -- UrgencyLevel --

    @Test
    fun `UrgencyLevel serialization round-trip`() {
        for (ul in UrgencyLevel.entries) {
            val encoded = json.encodeToString(UrgencyLevel.serializer(), ul)
            val decoded = json.decodeFromString(UrgencyLevel.serializer(), encoded)
            assertEquals(ul, decoded)
        }
    }

    @Test
    fun `UrgencyLevel icon non-empty for all`() {
        for (ul in UrgencyLevel.entries) {
            assertTrue("$ul should have an icon", ul.icon.isNotEmpty())
        }
    }

    // -- ServiceType --

    @Test
    fun `ServiceType serialization round-trip`() {
        for (st in ServiceType.entries) {
            val encoded = json.encodeToString(ServiceType.serializer(), st)
            val decoded = json.decodeFromString(ServiceType.serializer(), encoded)
            assertEquals(st, decoded)
        }
    }

    @Test
    fun `ServiceType requiresCarOrMotorcycle false only for DELIVERY`() {
        assertFalse(ServiceType.DELIVERY.requiresCarOrMotorcycle)
        assertTrue(ServiceType.GROCERY_SHOPPING.requiresCarOrMotorcycle)
        assertTrue(ServiceType.FOOD_DELIVERY.requiresCarOrMotorcycle)
        assertTrue(ServiceType.PHARMACY_PICKUP.requiresCarOrMotorcycle)
        assertTrue(ServiceType.GENERAL_ERRAND.requiresCarOrMotorcycle)
    }

    // -- PackageSize --

    @Test
    fun `PackageSize fromWeight boundaries`() {
        assertEquals(PackageSize.SMALL, PackageSize.fromWeight(0.5))
        assertEquals(PackageSize.SMALL, PackageSize.fromWeight(5.0))
        assertEquals(PackageSize.MEDIUM, PackageSize.fromWeight(5.1))
        assertEquals(PackageSize.MEDIUM, PackageSize.fromWeight(15.0))
        assertEquals(PackageSize.LARGE, PackageSize.fromWeight(15.1))
        assertEquals(PackageSize.LARGE, PackageSize.fromWeight(30.0))
        assertEquals(PackageSize.EXTRA_LARGE, PackageSize.fromWeight(30.1))
        assertEquals(PackageSize.EXTRA_LARGE, PackageSize.fromWeight(100.0))
    }

    @Test
    fun `PackageSize serialization round-trip`() {
        for (ps in PackageSize.entries) {
            val encoded = json.encodeToString(PackageSize.serializer(), ps)
            val decoded = json.decodeFromString(PackageSize.serializer(), encoded)
            assertEquals(ps, decoded)
        }
    }

    // -- BookingStatus --

    @Test
    fun `BookingStatus serialization round-trip`() {
        for (bs in BookingStatus.entries) {
            val encoded = json.encodeToString(BookingStatus.serializer(), bs)
            val decoded = json.decodeFromString(BookingStatus.serializer(), encoded)
            assertEquals(bs, decoded)
        }
    }

    @Test
    fun `BookingStatus fromMatchStatus maps request statuses to PENDING`() {
        assertEquals(BookingStatus.PENDING, BookingStatus.fromMatchStatus(MatchStatus.PENDING))
        assertEquals(BookingStatus.PENDING, BookingStatus.fromMatchStatus(MatchStatus.CARRIER_REQUESTED))
        assertEquals(BookingStatus.PENDING, BookingStatus.fromMatchStatus(MatchStatus.SHIPPER_REQUESTED))
    }

    @Test
    fun `BookingStatus fromMatchStatus maps accepted statuses to CONFIRMED`() {
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromMatchStatus(MatchStatus.CONFIRMED))
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromMatchStatus(MatchStatus.SHIPPER_ACCEPTED))
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromMatchStatus(MatchStatus.CARRIER_ACCEPTED))
    }

    @Test
    fun `BookingStatus fromMatchStatus maps delivery flow`() {
        assertEquals(BookingStatus.PICKED_UP, BookingStatus.fromMatchStatus(MatchStatus.PICKED_UP))
        assertEquals(BookingStatus.IN_TRANSIT, BookingStatus.fromMatchStatus(MatchStatus.IN_TRANSIT))
        assertEquals(BookingStatus.DELIVERED, BookingStatus.fromMatchStatus(MatchStatus.DELIVERED))
    }

    @Test
    fun `BookingStatus fromMatchStatus maps declined and cancelled to CANCELLED`() {
        assertEquals(BookingStatus.CANCELLED, BookingStatus.fromMatchStatus(MatchStatus.CANCELLED))
        assertEquals(BookingStatus.CANCELLED, BookingStatus.fromMatchStatus(MatchStatus.SHIPPER_DECLINED))
        assertEquals(BookingStatus.CANCELLED, BookingStatus.fromMatchStatus(MatchStatus.CARRIER_DECLINED))
    }

    @Test
    fun `BookingStatus fromMatchStatus covers all MatchStatus cases`() {
        // Ensures no case is missed — would fail to compile if a new MatchStatus is added
        for (ms in MatchStatus.entries) {
            val bs = BookingStatus.fromMatchStatus(ms)
            assertTrue("$ms should map to a BookingStatus", BookingStatus.entries.contains(bs))
        }
    }
}
