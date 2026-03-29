package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentEnumsTest {

    private val json = Json { ignoreUnknownKeys = true }

    // -- TransactionStatus --

    @Test
    fun `TransactionStatus serialization round-trip`() {
        for (status in TransactionStatus.entries) {
            val encoded = json.encodeToString(TransactionStatus.serializer(), status)
            val decoded = json.decodeFromString(TransactionStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `TransactionStatus has 8 cases`() {
        assertEquals(8, TransactionStatus.entries.size)
    }

    @Test
    fun `TransactionStatus decodes from wire values`() {
        assertEquals(TransactionStatus.PENDING, json.decodeFromString(TransactionStatus.serializer(), "\"pending\""))
        assertEquals(TransactionStatus.COMPLETED, json.decodeFromString(TransactionStatus.serializer(), "\"completed\""))
        assertEquals(TransactionStatus.REFUNDED, json.decodeFromString(TransactionStatus.serializer(), "\"refunded\""))
        assertEquals(TransactionStatus.FAILED, json.decodeFromString(TransactionStatus.serializer(), "\"failed\""))
    }

    @Test
    fun `TransactionStatus isTerminal correct`() {
        assertTrue(TransactionStatus.COMPLETED.isTerminal)
        assertTrue(TransactionStatus.REFUNDED.isTerminal)
        assertTrue(TransactionStatus.CANCELLED.isTerminal)
        assertTrue(TransactionStatus.FAILED.isTerminal)
        assertFalse(TransactionStatus.PENDING.isTerminal)
        assertFalse(TransactionStatus.AUTHORIZED.isTerminal)
        assertFalse(TransactionStatus.CAPTURED.isTerminal)
    }

    // -- RefundStatus --

    @Test
    fun `RefundStatus serialization round-trip`() {
        for (status in RefundStatus.entries) {
            val encoded = json.encodeToString(RefundStatus.serializer(), status)
            val decoded = json.decodeFromString(RefundStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `RefundStatus has 4 cases`() {
        assertEquals(4, RefundStatus.entries.size)
    }

    // -- PayoutStatus --

    @Test
    fun `PayoutStatus serialization round-trip`() {
        for (status in PayoutStatus.entries) {
            val encoded = json.encodeToString(PayoutStatus.serializer(), status)
            val decoded = json.decodeFromString(PayoutStatus.serializer(), encoded)
            assertEquals(status, decoded)
        }
    }

    @Test
    fun `PayoutStatus has 6 cases`() {
        assertEquals(6, PayoutStatus.entries.size)
    }

    // -- RefundReason --

    @Test
    fun `RefundReason OTHER requires custom input`() {
        assertTrue(RefundReason.OTHER.requiresCustomInput)
    }

    @Test
    fun `RefundReason preset reasons do not require custom input`() {
        assertFalse(RefundReason.DAMAGED.requiresCustomInput)
        assertFalse(RefundReason.NOT_DELIVERED.requiresCustomInput)
        assertFalse(RefundReason.WRONG_ITEM.requiresCustomInput)
        assertFalse(RefundReason.LATE_DELIVERY.requiresCustomInput)
        assertFalse(RefundReason.PARTIAL_DELIVERY.requiresCustomInput)
    }

    // -- TransactionRole --

    @Test
    fun `TransactionRole apiValue correct`() {
        assertEquals(null, TransactionRole.ALL.apiValue)
        assertEquals("shipper", TransactionRole.SHIPPER.apiValue)
        assertEquals("carrier", TransactionRole.CARRIER.apiValue)
    }
}
