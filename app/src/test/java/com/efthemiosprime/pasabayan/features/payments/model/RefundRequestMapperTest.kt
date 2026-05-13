package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundStatus
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefundRequestMapperTest {

    @Test
    fun `maps core scalars`() {
        val json = RefundRequestDataJson(
            id = 99,
            transactionId = 500,
            amount = 50.0,
            description = "Customer dispute",
            status = "pending",
            adminNotes = "Awaiting review",
            reviewedAt = "2026-04-01T10:00:00Z",
            processedAt = null,
            createdAt = "2026-03-30T09:00:00Z",
        )
        val refund = json.toDomain()

        assertEquals(99, refund.id)
        assertEquals(500, refund.transactionId)
        assertEquals(50.0, refund.amount!!, 0.001)
        assertEquals("Customer dispute", refund.description)
        assertEquals("Awaiting review", refund.adminNotes)
        assertEquals("2026-04-01T10:00:00Z", refund.reviewedAt)
        assertNull(refund.processedAt)
        assertEquals("2026-03-30T09:00:00Z", refund.createdAt)
    }

    @Test
    fun `each preset reason text maps to its enum`() {
        RefundReason.entries
            .filterNot { it == RefundReason.OTHER }
            .forEach { preset ->
                val json = baseJson(reason = preset.displayText)
                val refund = json.toDomain()
                assertEquals("reason=${preset.displayText}", preset, refund.reason)
                assertEquals(preset.displayText, refund.reasonText)
            }
    }

    @Test
    fun `OTHER preset display text maps to OTHER enum`() {
        val json = baseJson(reason = RefundReason.OTHER.displayText)
        assertEquals(RefundReason.OTHER, json.toDomain().reason)
    }

    @Test
    fun `unmatched freeform reason yields null enum and preserves text`() {
        val json = baseJson(reason = "Driver was rude")
        val refund = json.toDomain()
        assertNull(refund.reason)
        assertEquals("Driver was rude", refund.reasonText)
    }

    @Test
    fun `null reason yields null enum and null text`() {
        val json = baseJson(reason = null)
        val refund = json.toDomain()
        assertNull(refund.reason)
        assertNull(refund.reasonText)
    }

    @Test
    fun `each wire status string maps to enum`() {
        val cases = mapOf(
            "pending" to RefundStatus.PENDING,
            "approved" to RefundStatus.APPROVED,
            "rejected" to RefundStatus.REJECTED,
            "processed" to RefundStatus.PROCESSED,
        )
        cases.forEach { (raw, expected) ->
            assertEquals("status=$raw", expected, baseJson(status = raw).toDomain().status)
        }
    }

    @Test
    fun `unknown status defaults to PENDING`() {
        val refund = baseJson(status = "weirdo").toDomain()
        assertEquals(RefundStatus.PENDING, refund.status)
    }

    @Test
    fun `empty status defaults to PENDING`() {
        val refund = baseJson(status = "").toDomain()
        assertEquals(RefundStatus.PENDING, refund.status)
    }

    @Test
    fun `isFullRefund is true when amount is null`() {
        assertTrue(baseJson(amount = null).toDomain().isFullRefund)
    }

    @Test
    fun `isFullRefund is false when amount is present`() {
        assertFalse(baseJson(amount = 25.0).toDomain().isFullRefund)
    }

    @Test
    fun `defaults handle missing id and transactionId`() {
        val refund = RefundRequestDataJson(status = "pending").toDomain()
        assertEquals(0, refund.id)
        assertEquals(0, refund.transactionId)
        assertEquals(RefundStatus.PENDING, refund.status)
    }

    private fun baseJson(
        reason: String? = null,
        status: String = "pending",
        amount: Double? = null,
    ) = RefundRequestDataJson(
        id = 1,
        transactionId = 100,
        amount = amount,
        reason = reason,
        status = status,
    )
}
