package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mirrors iOS `VerificationLevelTests`. Pins `normalized()` (lenient parsing of
 * the API string), the `isVerified` / `isPremium` matrix, and the
 * `@SerialName` round-trip so a future enum rename doesn't silently break
 * decoding from the server.
 */
class VerificationLevelTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- normalized() ----

    @Test
    fun `normalized maps recognized lowercase values`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("basic"))
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized("verified"))
        assertEquals(VerificationLevel.PREMIUM, VerificationLevel.normalized("premium"))
    }

    @Test
    fun `normalized is case-insensitive`() {
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized("VERIFIED"))
        assertEquals(VerificationLevel.PREMIUM, VerificationLevel.normalized("Premium"))
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized("vErIfIeD"))
    }

    @Test
    fun `normalized trims surrounding whitespace`() {
        assertEquals(VerificationLevel.VERIFIED, VerificationLevel.normalized(" verified "))
        assertEquals(VerificationLevel.PREMIUM, VerificationLevel.normalized("\tpremium\n"))
    }

    @Test
    fun `normalized defaults to BASIC for null`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized(null))
    }

    @Test
    fun `normalized defaults to BASIC for empty and whitespace`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized(""))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("   "))
    }

    @Test
    fun `normalized defaults to BASIC for unrecognized values`() {
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("unknown"))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("gold"))
        assertEquals(VerificationLevel.BASIC, VerificationLevel.normalized("42"))
    }

    // ---- isVerified / isPremium ----

    @Test
    fun `isVerified matrix`() {
        assertFalse(VerificationLevel.BASIC.isVerified)
        assertTrue(VerificationLevel.VERIFIED.isVerified)
        assertTrue(VerificationLevel.PREMIUM.isVerified)
    }

    @Test
    fun `isPremium matrix`() {
        assertFalse(VerificationLevel.BASIC.isPremium)
        assertFalse(VerificationLevel.VERIFIED.isPremium)
        assertTrue(VerificationLevel.PREMIUM.isPremium)
    }

    // ---- JSON round-trip — guards against @SerialName drift ----

    @Test
    fun `decodes from API serial names`() {
        assertEquals(VerificationLevel.BASIC, json.decodeFromString<VerificationLevel>("\"basic\""))
        assertEquals(VerificationLevel.VERIFIED, json.decodeFromString<VerificationLevel>("\"verified\""))
        assertEquals(VerificationLevel.PREMIUM, json.decodeFromString<VerificationLevel>("\"premium\""))
    }

    @Test
    fun `encodes to API serial names`() {
        assertEquals("\"basic\"", json.encodeToString(VerificationLevel.BASIC))
        assertEquals("\"verified\"", json.encodeToString(VerificationLevel.VERIFIED))
        assertEquals("\"premium\"", json.encodeToString(VerificationLevel.PREMIUM))
    }

    @Test
    fun `round-trip preserves identity for each level`() {
        VerificationLevel.values().forEach { level ->
            val encoded = json.encodeToString(level)
            assertEquals(level, json.decodeFromString<VerificationLevel>(encoded))
        }
    }
}
