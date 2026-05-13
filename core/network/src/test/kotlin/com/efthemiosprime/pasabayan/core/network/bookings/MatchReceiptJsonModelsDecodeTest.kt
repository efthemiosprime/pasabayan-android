package com.efthemiosprime.pasabayan.core.network.bookings

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Wire-contract test for the match-service receipt endpoints. Mirrors iOS
 * `ReceiptUploadResponse` and `ReceiptResponse` shapes — distinct from the
 * payment-level `PaymentReceiptJson` used by `ReceiptApi`.
 */
class MatchReceiptJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `MatchReceiptUploadResponseJson decodes upload success body`() {
        val raw = """
            {
              "success": true,
              "message": "Receipt uploaded",
              "data": {
                "receipt_photo": "receipts/match-100.jpg",
                "receipt_url": "https://cdn.pasabayan.com/receipts/match-100.jpg"
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString(MatchReceiptUploadResponseJson.serializer(), raw)

        assertTrue(decoded.success)
        assertEquals("Receipt uploaded", decoded.message)
        assertNotNull(decoded.data)
        assertEquals("receipts/match-100.jpg", decoded.data!!.receiptPhoto)
        assertEquals("https://cdn.pasabayan.com/receipts/match-100.jpg", decoded.data!!.receiptUrl)
    }

    @Test
    fun `MatchReceiptResponseJson decodes fetch body with uploaded_at`() {
        val raw = """
            {
              "success": true,
              "data": {
                "receipt_photo": "receipts/match-100.jpg",
                "receipt_url": "https://cdn.pasabayan.com/receipts/match-100.jpg",
                "uploaded_at": "2026-04-01T12:34:56Z"
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString(MatchReceiptResponseJson.serializer(), raw)

        assertTrue(decoded.success)
        assertNotNull(decoded.data)
        assertEquals("2026-04-01T12:34:56Z", decoded.data!!.uploadedAt)
    }

    @Test
    fun `MatchReceiptResponseJson leaves data null when body has no receipt yet`() {
        // Backend behaviour mirrors iOS: 404 → no body or `data: null`.
        val raw = """{"success": false, "message": "No receipt uploaded"}"""

        val decoded = json.decodeFromString(MatchReceiptResponseJson.serializer(), raw)

        assertNull(decoded.data)
    }
}
