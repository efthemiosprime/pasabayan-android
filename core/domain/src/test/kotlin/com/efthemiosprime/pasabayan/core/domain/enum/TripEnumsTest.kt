package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripEnumsTest {

    private val json = Json { ignoreUnknownKeys = true }

    // -- TripStatus --

    @Test
    fun `TripStatus serialization round-trip`() {
        for (status in TripStatus.entries) {
            val encoded = json.encodeToString(TripStatus.serializer(), status)
            val decoded = json.decodeFromString(TripStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `TripStatus decodes from wire values`() {
        assertEquals(TripStatus.PLANNING, json.decodeFromString(TripStatus.serializer(), "\"planning\""))
        assertEquals(TripStatus.ACTIVE, json.decodeFromString(TripStatus.serializer(), "\"active\""))
        assertEquals(TripStatus.IN_TRANSIT, json.decodeFromString(TripStatus.serializer(), "\"in_transit\""))
        assertEquals(TripStatus.COMPLETED, json.decodeFromString(TripStatus.serializer(), "\"completed\""))
        assertEquals(TripStatus.CANCELLED, json.decodeFromString(TripStatus.serializer(), "\"cancelled\""))
    }

    // -- TransportationMethod --

    @Test
    fun `TransportationMethod serialization round-trip`() {
        for (method in TransportationMethod.entries) {
            val encoded = json.encodeToString(TransportationMethod.serializer(), method)
            val decoded = json.decodeFromString(TransportationMethod.serializer(), encoded)
            assertEquals(method, decoded)
        }
    }

    @Test
    fun `TransportationMethod isLandTransport correct for land types`() {
        val landTypes = listOf(
            TransportationMethod.BUS,
            TransportationMethod.CAR,
            TransportationMethod.TRUCK,
            TransportationMethod.VAN,
            TransportationMethod.MOTORCYCLE,
            TransportationMethod.TRAIN,
        )
        for (method in landTypes) {
            assertTrue("$method should be land transport", method.isLandTransport)
        }
    }

    @Test
    fun `TransportationMethod isLandTransport false for non-land types`() {
        val nonLandTypes = listOf(
            TransportationMethod.NONE,
            TransportationMethod.FLIGHT,
            TransportationMethod.SHIP,
            TransportationMethod.OTHER,
        )
        for (method in nonLandTypes) {
            assertFalse("$method should not be land transport", method.isLandTransport)
        }
    }

    @Test
    fun `TransportationMethod defaultPricingType is flat for land, perKg for others`() {
        assertEquals(PricingType.FLAT, TransportationMethod.CAR.defaultPricingType)
        assertEquals(PricingType.FLAT, TransportationMethod.BUS.defaultPricingType)
        assertEquals(PricingType.PER_KG, TransportationMethod.FLIGHT.defaultPricingType)
        assertEquals(PricingType.PER_KG, TransportationMethod.SHIP.defaultPricingType)
    }

    @Test
    fun `TransportationMethod icon is non-empty for all except NONE`() {
        for (method in TransportationMethod.entries) {
            if (method == TransportationMethod.NONE) {
                assertEquals("", method.icon)
            } else {
                assertTrue("$method should have an icon", method.icon.isNotEmpty())
            }
        }
    }

    // -- PricingType --

    @Test
    fun `PricingType serialization round-trip`() {
        for (pt in PricingType.entries) {
            val encoded = json.encodeToString(PricingType.serializer(), pt)
            val decoded = json.decodeFromString(PricingType.serializer(), encoded)
            assertEquals(pt, decoded)
        }
    }

    @Test
    fun `PricingType decodes from wire values`() {
        assertEquals(PricingType.PER_KG, json.decodeFromString(PricingType.serializer(), "\"per_kg\""))
        assertEquals(PricingType.FLAT, json.decodeFromString(PricingType.serializer(), "\"flat\""))
    }

    // -- PricingMethod --

    @Test
    fun `PricingMethod serialization round-trip`() {
        for (pm in PricingMethod.entries) {
            val encoded = json.encodeToString(PricingMethod.serializer(), pm)
            val decoded = json.decodeFromString(PricingMethod.serializer(), encoded)
            assertEquals(pm, decoded)
        }
    }

    // -- SortOrder --

    @Test
    fun `SortOrder decodes from wire values`() {
        assertEquals(SortOrder.ASCENDING, json.decodeFromString(SortOrder.serializer(), "\"asc\""))
        assertEquals(SortOrder.DESCENDING, json.decodeFromString(SortOrder.serializer(), "\"desc\""))
    }

    // -- VerificationLevel --

    @Test
    fun `VerificationLevel serialization round-trip`() {
        for (vl in VerificationLevel.entries) {
            val encoded = json.encodeToString(VerificationLevel.serializer(), vl)
            val decoded = json.decodeFromString(VerificationLevel.serializer(), encoded)
            assertEquals(vl, decoded)
        }
    }

    @Test
    fun `VerificationLevel normalized maps known values`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("basic"))
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized("verified"))
        assertEquals(VerificationLevel.PREMIUM, VerificationLevel.normalized("premium"))
    }

    @Test
    fun `VerificationLevel normalized defaults to BASIC for unknown`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("unknown_level"))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized(null))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized(""))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("   "))
    }

    @Test
    fun `VerificationLevel normalized is case-insensitive`() {
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized("VERIFIED"))
        assertEquals(VerificationLevel.PREMIUM, VerificationLevel.normalized("Premium"))
    }

    @Test
    fun `VerificationLevel isVerified correct`() {
        assertFalse(VerificationLevel.BASIC.isVerified)
        assertTrue(VerificationLevel.VERIFIED.isVerified)
        assertTrue(VerificationLevel.PREMIUM.isVerified)
    }

    @Test
    fun `VerificationLevel isPremium correct`() {
        assertFalse(VerificationLevel.BASIC.isPremium)
        assertFalse(VerificationLevel.VERIFIED.isPremium)
        assertTrue(VerificationLevel.PREMIUM.isPremium)
    }

    // -- InitiatedBy --

    @Test
    fun `InitiatedBy serialization round-trip`() {
        for (ib in InitiatedBy.entries) {
            val encoded = json.encodeToString(InitiatedBy.serializer(), ib)
            val decoded = json.decodeFromString(InitiatedBy.serializer(), encoded)
            assertEquals(ib, decoded)
        }
    }

    // -- UserRole --

    @Test
    fun `UserRole decodes from wire values`() {
        assertEquals(UserRole.SHIPPER, json.decodeFromString(UserRole.serializer(), "\"shipper\""))
        assertEquals(UserRole.CARRIER, json.decodeFromString(UserRole.serializer(), "\"carrier\""))
    }
}
