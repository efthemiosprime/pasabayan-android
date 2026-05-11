package com.efthemiosprime.pasabayan.features.legal.services

import com.efthemiosprime.pasabayan.core.network.legal.LegalApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class LegalRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: LegalRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = LegalRepositoryImpl(retrofit.create(LegalApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchStatus returns mapped domain status`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"data":{"all_agreed":false,"pending_count":1,
                  "pending_documents":[{"id":7,"type":"terms_of_service","title":"Terms","version":"1.0"}]}}
                """.trimIndent(),
            ),
        )
        val status = repo.fetchStatus().getOrThrow()
        assertFalse(status.allAgreed)
        assertEquals(1, status.pendingCount)
        assertEquals(1, status.pendingDocuments.size)
        assertEquals("terms_of_service", status.pendingDocuments.first().type)
    }

    @Test
    fun `fetchStatus failure when success false`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":false,"message":"server rejection"}""",
            ),
        )
        val result = repo.fetchStatus()
        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchStatus failure on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"down"}"""))
        val result = repo.fetchStatus()
        assertTrue(result.isFailure)
    }

    @Test
    fun `agree posts document_ids and returns outcome`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"all_required_agreed":true,"pending_required_count":0}}""",
            ),
        )
        val outcome = repo.agree(listOf(1, 2), deviceId = "Android_abc12345").getOrThrow()
        assertTrue(outcome.allRequiredAgreed)
        assertEquals(0, outcome.pendingRequiredCount)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val body = request.body.readUtf8()
        assertTrue("expected document_ids", body.contains("\"document_ids\":[1,2]"))
        assertTrue("expected device_id", body.contains("\"device_id\":\"Android_abc12345\""))
    }

    @Test
    fun `agree requires at least one document id`() = runBlocking {
        val result = repo.agree(emptyList())
        assertTrue(result.isFailure)
        // No HTTP call fired
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `withdraw posts document_type and surfaces warning`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"document_type":"marketing_communications","warning":"Some services may be limited"}}""",
            ),
        )
        val outcome = repo.withdraw("marketing_communications").getOrThrow()
        assertEquals("marketing_communications", outcome.documentType)
        assertEquals("Some services may be limited", outcome.warning)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(
            "expected document_type body",
            request.body.readUtf8().contains("\"document_type\":\"marketing_communications\""),
        )
    }

    @Test
    fun `withdraw requires non-blank document_type`() = runBlocking {
        val result = repo.withdraw("   ")
        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }
}
