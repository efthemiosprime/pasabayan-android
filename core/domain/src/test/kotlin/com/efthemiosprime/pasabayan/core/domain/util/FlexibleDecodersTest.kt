package com.efthemiosprime.pasabayan.core.domain.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexibleDecodersTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // -- FlexibleDouble --

    @Serializable
    private data class DoubleHolder(
        @Serializable(with = FlexibleDoubleSerializer::class)
        val value: Double?,
    )

    @Test
    fun `flexibleDouble decodes from double`() {
        val result = json.decodeFromString<DoubleHolder>("""{"value": 42.5}""")
        assertEquals(42.5, result.value)
    }

    @Test
    fun `flexibleDouble decodes from int`() {
        val result = json.decodeFromString<DoubleHolder>("""{"value": 10}""")
        assertEquals(10.0, result.value)
    }

    @Test
    fun `flexibleDouble decodes from string`() {
        val result = json.decodeFromString<DoubleHolder>("""{"value": "25.75"}""")
        assertEquals(25.75, result.value)
    }

    @Test
    fun `flexibleDouble returns null for non-numeric string`() {
        val result = json.decodeFromString<DoubleHolder>("""{"value": "abc"}""")
        assertNull(result.value)
    }

    @Test
    fun `flexibleDouble returns null for null`() {
        val result = json.decodeFromString<DoubleHolder>("""{"value": null}""")
        assertNull(result.value)
    }

    // -- FlexibleBool --

    @Serializable
    private data class BoolHolder(
        @Serializable(with = FlexibleBoolSerializer::class)
        val value: Boolean?,
    )

    @Test
    fun `flexibleBool decodes from boolean true`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": true}""")
        assertTrue(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from boolean false`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": false}""")
        assertFalse(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from int 1`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": 1}""")
        assertTrue(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from int 0`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": 0}""")
        assertFalse(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string true`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "true"}""")
        assertTrue(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string false`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "false"}""")
        assertFalse(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string yes`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "yes"}""")
        assertTrue(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string no`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "no"}""")
        assertFalse(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string 1`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "1"}""")
        assertTrue(result.value!!)
    }

    @Test
    fun `flexibleBool decodes from string 0`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "0"}""")
        assertFalse(result.value!!)
    }

    @Test
    fun `flexibleBool returns null for unrecognized string`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": "maybe"}""")
        assertNull(result.value)
    }

    @Test
    fun `flexibleBool returns null for null`() {
        val result = json.decodeFromString<BoolHolder>("""{"value": null}""")
        assertNull(result.value)
    }

    // -- FlexibleString --

    @Serializable
    private data class StringHolder(
        @Serializable(with = FlexibleStringSerializer::class)
        val value: String?,
    )

    @Test
    fun `flexibleString decodes normal string`() {
        val result = json.decodeFromString<StringHolder>("""{"value": "hello"}""")
        assertEquals("hello", result.value)
    }

    @Test
    fun `flexibleString returns null for blank string`() {
        val result = json.decodeFromString<StringHolder>("""{"value": "   "}""")
        assertNull(result.value)
    }

    @Test
    fun `flexibleString returns null for empty string`() {
        val result = json.decodeFromString<StringHolder>("""{"value": ""}""")
        assertNull(result.value)
    }

    @Test
    fun `flexibleString returns null for null`() {
        val result = json.decodeFromString<StringHolder>("""{"value": null}""")
        assertNull(result.value)
    }

    // -- Non-nullable variants --

    @Serializable
    private data class DoubleNotNullHolder(
        @Serializable(with = FlexibleDoubleNotNullSerializer::class)
        val value: Double,
    )

    @Test
    fun `flexibleDoubleNotNull defaults to 0 for null`() {
        val result = json.decodeFromString<DoubleNotNullHolder>("""{"value": null}""")
        assertEquals(0.0, result.value, 0.001)
    }

    @Test
    fun `flexibleDoubleNotNull decodes from string`() {
        val result = json.decodeFromString<DoubleNotNullHolder>("""{"value": "99.9"}""")
        assertEquals(99.9, result.value, 0.001)
    }

    @Serializable
    private data class BoolNotNullHolder(
        @Serializable(with = FlexibleBoolNotNullSerializer::class)
        val value: Boolean,
    )

    @Test
    fun `flexibleBoolNotNull defaults to false for null`() {
        val result = json.decodeFromString<BoolNotNullHolder>("""{"value": null}""")
        assertFalse(result.value)
    }

    @Test
    fun `flexibleBoolNotNull decodes from string yes`() {
        val result = json.decodeFromString<BoolNotNullHolder>("""{"value": "yes"}""")
        assertTrue(result.value)
    }
}
