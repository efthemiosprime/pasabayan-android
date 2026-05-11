package com.efthemiosprime.pasabayan.core.network.legal

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `LegalStatusResponseJson decodes with pending documents`() {
        val raw = """
        {
          "success": true,
          "data": {
            "all_agreed": false,
            "pending_count": 2,
            "pending_documents": [
              {"id": 1, "type": "terms_of_service", "title": "Terms of Service", "version": "1.0"},
              {"id": 2, "type": "privacy_policy", "title": "Privacy Policy", "version": "2.1"}
            ]
          }
        }
        """.trimIndent()
        val response = json.decodeFromString<LegalStatusResponseJson>(raw)
        assertTrue(response.success)
        val data = response.data!!
        assertEquals(false, data.allAgreed)
        assertEquals(2, data.pendingCount)
        assertEquals(2, data.pendingDocuments?.size)
        assertEquals("terms_of_service", data.pendingDocuments?.get(0)?.type)
    }

    @Test
    fun `LegalStatusResponseJson decodes when all agreed`() {
        val raw = """{"success": true, "data": {"all_agreed": true, "pending_count": 0, "pending_documents": []}}"""
        val response = json.decodeFromString<LegalStatusResponseJson>(raw)
        assertTrue(response.data!!.allAgreed == true)
        assertTrue(response.data!!.pendingDocuments!!.isEmpty())
    }

    @Test
    fun `AgreementRequestJson encodes document_ids list`() {
        val raw = json.encodeToString(
            AgreementRequestJson.serializer(),
            AgreementRequestJson(documentIds = listOf(1, 2), deviceId = "Android_abc12345"),
        )
        assertTrue("expected document_ids snake_case", raw.contains("\"document_ids\":[1,2]"))
        assertTrue(raw.contains("\"device_id\":\"Android_abc12345\""))
    }

    @Test
    fun `AgreementResponseJson decodes all required agreed`() {
        val raw = """
        {
          "success": true,
          "message": "Recorded",
          "data": {
            "all_required_agreed": true,
            "pending_required_count": 0,
            "agreements": [
              {"document_type": "terms_of_service", "document_version": "1.0", "agreed_at": "2026-05-11T03:00:00Z"}
            ]
          }
        }
        """.trimIndent()
        val response = json.decodeFromString<AgreementResponseJson>(raw)
        assertTrue(response.success)
        assertEquals(true, response.data!!.allRequiredAgreed)
        assertEquals(0, response.data!!.pendingRequiredCount)
        assertEquals("terms_of_service", response.data!!.agreements!![0].documentType)
    }

    @Test
    fun `AgreementResponseJson decodes partial agreement`() {
        val raw = """
        {"success": true, "data": {"all_required_agreed": false, "pending_required_count": 1}}
        """.trimIndent()
        val response = json.decodeFromString<AgreementResponseJson>(raw)
        assertFalse(response.data!!.allRequiredAgreed == true)
        assertEquals(1, response.data!!.pendingRequiredCount)
    }

    @Test
    fun `WithdrawalRequestJson encodes document_type`() {
        val raw = json.encodeToString(
            WithdrawalRequestJson.serializer(),
            WithdrawalRequestJson(documentType = "marketing_communications"),
        )
        assertEquals("""{"document_type":"marketing_communications"}""", raw)
    }

    @Test
    fun `WithdrawalResponseJson decodes warning`() {
        val raw = """
        {"success": true, "data": {"document_type": "marketing_communications", "warning": "Some services may be limited"}}
        """.trimIndent()
        val response = json.decodeFromString<WithdrawalResponseJson>(raw)
        assertEquals("marketing_communications", response.data!!.documentType)
        assertEquals("Some services may be limited", response.data!!.warning)
    }

    @Test
    fun `WithdrawalResponseJson decodes without data`() {
        val raw = """{"success": true, "message": "withdrawn"}"""
        val response = json.decodeFromString<WithdrawalResponseJson>(raw)
        assertTrue(response.success)
        assertNull(response.data)
    }
}
