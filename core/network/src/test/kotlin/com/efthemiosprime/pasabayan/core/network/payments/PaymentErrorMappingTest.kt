package com.efthemiosprime.pasabayan.core.network.payments

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentErrorMappingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `403 maps to server error with message`() {
        val body = """{"message":"Permission denied for this transaction"}""".encodeToByteArray()
        val result = ApiErrorMapper.map(statusCode = 403, body = body, json = json)

        assertTrue(result is DomainError.ServerError)
        assertEquals("Permission denied for this transaction", (result as DomainError.ServerError).message)
    }

    @Test
    fun `422 maps validation errors with field map`() {
        val body = """
            {
              "message": "The given data was invalid.",
              "errors": {
                "delivery_match_id": ["The delivery match id field is required."],
                "amount": ["The amount must be at least 1."]
              }
            }
        """.trimIndent().encodeToByteArray()

        val result = ApiErrorMapper.map(statusCode = 422, body = body, json = json)

        assertTrue(result is DomainError.ValidationError)
        result as DomainError.ValidationError
        assertEquals("The given data was invalid.", result.message)
        assertEquals(2, result.fieldErrors.size)
        assertEquals("The amount must be at least 1.", result.fieldErrors["amount"]?.first())
    }
}
