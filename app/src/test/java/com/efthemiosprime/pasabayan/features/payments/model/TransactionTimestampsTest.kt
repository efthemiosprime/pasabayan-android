package com.efthemiosprime.pasabayan.features.payments.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionTimestampsTest {

    @Test
    fun `updatedAt defaults to createdAt`() {
        val ts = TransactionTimestamps(createdAt = "2026-05-01T10:00:00Z")
        assertEquals("2026-05-01T10:00:00Z", ts.updatedAt)
    }

    @Test
    fun `defaults are empty strings and nulls`() {
        val ts = TransactionTimestamps()
        assertEquals("", ts.createdAt)
        assertEquals("", ts.updatedAt)
        assertFalse(ts.hasAny)
    }

    @Test
    fun `hasAny is true when only authorizedAt is present`() {
        val ts = TransactionTimestamps(authorizedAt = "2026-05-01T10:00:00Z")
        assertTrue(ts.hasAny)
    }

    @Test
    fun `hasAny is true when createdAt is non-empty`() {
        val ts = TransactionTimestamps(createdAt = "2026-05-01T10:00:00Z")
        assertTrue(ts.hasAny)
    }

    @Test
    fun `each optional timestamp is independently accessible`() {
        val ts = TransactionTimestamps(
            createdAt = "c",
            updatedAt = "u",
            authorizedAt = "a",
            capturedAt = "cap",
            completedAt = "comp",
            failedAt = "f",
            payoutCompletedAt = "p",
        )
        assertEquals("a", ts.authorizedAt)
        assertEquals("cap", ts.capturedAt)
        assertEquals("comp", ts.completedAt)
        assertEquals("f", ts.failedAt)
        assertEquals("p", ts.payoutCompletedAt)
        assertEquals("u", ts.updatedAt)
    }
}
