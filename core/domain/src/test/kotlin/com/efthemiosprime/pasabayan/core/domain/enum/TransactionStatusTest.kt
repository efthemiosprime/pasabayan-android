package com.efthemiosprime.pasabayan.core.domain.`enum`

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionStatusTest {

    @Test
    fun `all wire statuses are represented`() {
        assertEquals(8, TransactionStatus.entries.size)
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.PENDING))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.AUTHORIZED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.CAPTURED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.COMPLETED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.REFUNDED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.CANCELLED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.FAILED))
        assertTrue(TransactionStatus.entries.contains(TransactionStatus.UNKNOWN))
    }

    @Test
    fun `terminal statuses are correct`() {
        assertFalse(TransactionStatus.PENDING.isTerminal)
        assertFalse(TransactionStatus.AUTHORIZED.isTerminal)
        assertFalse(TransactionStatus.CAPTURED.isTerminal)
        assertTrue(TransactionStatus.COMPLETED.isTerminal)
        assertTrue(TransactionStatus.REFUNDED.isTerminal)
        assertTrue(TransactionStatus.CANCELLED.isTerminal)
        assertTrue(TransactionStatus.FAILED.isTerminal)
        assertFalse(TransactionStatus.UNKNOWN.isTerminal)
    }
}
