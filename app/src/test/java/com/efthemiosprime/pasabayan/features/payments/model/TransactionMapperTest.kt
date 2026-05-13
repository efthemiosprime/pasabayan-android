package com.efthemiosprime.pasabayan.features.payments.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PayoutStatus
import com.efthemiosprime.pasabayan.core.network.payments.PayoutJson
import com.efthemiosprime.pasabayan.core.network.payments.RefundInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.StripeInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.TipInfoJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionAmountsJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionTimestampsJson
import com.efthemiosprime.pasabayan.core.network.payments.TransactionUserJson
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun `maps all nested types end-to-end`() {
        val json = TransactionJson(
            id = 500,
            shipper = TransactionUserJson(id = 5, name = "Alice", email = "alice@example.com"),
            carrier = TransactionUserJson(id = 42, name = "John", avatar = "https://img/john.png"),
            deliveryMatchId = 100,
            amounts = TransactionAmountsJson(
                total = 175.50, subtotal = 150.0, platformFee = 25.50, carrierReceives = 124.50,
                currency = "cad", tip = 5.0, carrierTotal = 129.50, baseAmount = 150.0,
            ),
            stripe = StripeInfoJson(paymentIntentId = "pi_123", chargeId = "ch_789"),
            status = "completed",
            refund = RefundInfoJson(amount = 50.0, reason = "duplicate"),
            tip = TipInfoJson(amount = 5.0, paidAt = "2026-03-29T12:00:00Z"),
            payout = PayoutJson(status = "completed", notes = "Sent via ACH"),
            payoutStatus = "scheduled",
            createdAt = "2026-03-28T10:00:00Z",
            updatedAt = "2026-03-29T10:00:00Z",
        )
        val tx = json.toDomain()

        // Users
        assertEquals(5, tx.shipper!!.id)
        assertEquals("alice@example.com", tx.shipper!!.email)
        assertEquals("https://img/john.png", tx.carrier!!.avatar)

        // Amounts
        assertEquals(175.50, tx.amounts!!.total, 0.001)
        assertEquals(150.0, tx.amounts!!.baseAmount!!, 0.001)
        assertTrue(tx.amounts!!.hasTwoSidedBreakdown)

        // Stripe info
        assertEquals("pi_123", tx.stripe!!.paymentIntentId)
        assertEquals("ch_789", tx.stripe!!.chargeId)
        assertNull(tx.stripe!!.refundId)

        // Refund info
        assertEquals(50.0, tx.refund!!.amount!!, 0.001)
        assertEquals("duplicate", tx.refund!!.reason)

        // Tip info
        assertEquals(5.0, tx.tip!!.amount, 0.001)
        assertEquals("2026-03-29T12:00:00Z", tx.tip!!.paidAt)

        // Payout — nested object wins over top-level for effective
        assertEquals(PayoutStatus.COMPLETED, tx.payout!!.status)
        assertEquals(PayoutStatus.SCHEDULED, tx.payoutStatus)
        assertEquals(PayoutStatus.COMPLETED, tx.effectivePayoutStatus)
        assertEquals("Sent via ACH", tx.effectivePayoutNotes)
    }

    @Test
    fun `nested timestamps preferred over top-level fields`() {
        val json = TransactionJson(
            id = 1,
            status = "pending",
            timestamps = TransactionTimestampsJson(
                authorizedAt = "2026-04-01T09:00:00Z",
                completedAt = "2026-04-01T10:00:00Z",
                createdAt = "2026-04-01T08:00:00Z",
                updatedAt = "2026-04-01T11:00:00Z",
            ),
            // Top-level should be ignored when nested is present.
            createdAt = "OLD",
            updatedAt = "OLD",
        )
        val tx = json.toDomain()

        assertEquals("2026-04-01T08:00:00Z", tx.timestamps.createdAt)
        assertEquals("2026-04-01T11:00:00Z", tx.timestamps.updatedAt)
        assertEquals("2026-04-01T09:00:00Z", tx.timestamps.authorizedAt)
        assertEquals("2026-04-01T10:00:00Z", tx.timestamps.completedAt)
    }

    @Test
    fun `timestamps fall back to top-level fields when nested is null`() {
        val json = TransactionJson(
            id = 1,
            status = "pending",
            createdAt = "2026-04-01T08:00:00Z",
            updatedAt = "2026-04-01T09:00:00Z",
            payoutCompletedAt = "2026-04-02T10:00:00Z",
        )
        val tx = json.toDomain()

        assertEquals("2026-04-01T08:00:00Z", tx.timestamps.createdAt)
        assertEquals("2026-04-01T09:00:00Z", tx.timestamps.updatedAt)
        assertEquals("2026-04-02T10:00:00Z", tx.timestamps.payoutCompletedAt)
    }

    @Test
    fun `updatedAt defaults to createdAt when only created is present`() {
        val json = TransactionJson(id = 1, status = "pending", createdAt = "2026-04-01T08:00:00Z")
        val tx = json.toDomain()
        assertEquals("2026-04-01T08:00:00Z", tx.timestamps.updatedAt)
    }

    @Test
    fun `payout_status string maps to enum including on_hold`() {
        val ts = listOf("pending", "processing", "on_hold", "scheduled", "completed", "failed")
        val expected = listOf(
            PayoutStatus.PENDING, PayoutStatus.PROCESSING, PayoutStatus.ON_HOLD,
            PayoutStatus.SCHEDULED, PayoutStatus.COMPLETED, PayoutStatus.FAILED,
        )
        ts.forEachIndexed { idx, raw ->
            val tx = TransactionJson(id = 1, payoutStatus = raw).toDomain()
            assertEquals("payout_status $raw", expected[idx], tx.payoutStatus)
        }
    }

    @Test
    fun `unknown payout_status falls back to null`() {
        val tx = TransactionJson(id = 1, payoutStatus = "gibberish").toDomain()
        assertNull(tx.payoutStatus)
    }

    @Test
    fun `unknown status falls back to UNKNOWN`() {
        val tx = TransactionJson(id = 1, status = "garbage").toDomain()
        assertEquals(
            com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus.UNKNOWN,
            tx.transactionStatus,
        )
    }

    @Test
    fun `transaction_status field is preferred over status`() {
        val tx = TransactionJson(id = 1, status = "pending", transactionStatus = "captured").toDomain()
        assertEquals(
            com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus.CAPTURED,
            tx.transactionStatus,
        )
    }

    @Test
    fun `metadata map is preserved`() {
        val json = TransactionJson(
            id = 1,
            metadata = mapOf(
                "origin" to JsonPrimitive("web"),
                "retry_count" to JsonPrimitive(2),
            ),
        )
        val tx = json.toDomain()
        assertNotNull(tx.metadata)
        assertEquals(JsonPrimitive("web"), tx.metadata!!["origin"])
        assertEquals(JsonPrimitive(2), tx.metadata!!["retry_count"])
    }

    @Test
    fun `amounts mapper supplies defaults for missing optional fees`() {
        val json = TransactionJson(
            id = 1,
            amounts = TransactionAmountsJson(total = 100.0),
        )
        val tx = json.toDomain()
        val amounts = tx.amounts!!
        assertEquals(100.0, amounts.total, 0.001)
        assertEquals(0.0, amounts.platformFee, 0.001)
        // carrierReceives = max(0, total - platformFee) = 100
        assertEquals(100.0, amounts.carrierReceives, 0.001)
        assertEquals(0.0, amounts.tip, 0.001)
    }
}
