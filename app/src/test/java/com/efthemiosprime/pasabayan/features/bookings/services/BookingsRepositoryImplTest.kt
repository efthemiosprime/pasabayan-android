package com.efthemiosprime.pasabayan.features.bookings.services

import com.efthemiosprime.pasabayan.core.network.bookings.BookingsApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class BookingsRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: BookingsRepositoryImpl

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
        val api = retrofit.create(BookingsApi::class.java)
        repo = BookingsRepositoryImpl(api, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- loadMatches --

    @Test
    fun `loadMatches returns matches on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "match_status": "confirmed", "agreed_price": 150.0},
                            {"id": 2, "match_status": "pending", "agreed_price": 100.0}
                        ],
                        "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadMatches(role = "carrier")
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertEquals(1, result.getOrThrow()[0].id)
    }

    @Test
    fun `loadMatches returns failure on 401`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Unauthenticated"}"""))

        val result = repo.loadMatches(role = "carrier")
        assertTrue(result.isFailure)
    }

    // -- getMatch --

    @Test
    fun `getMatch returns match on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "OK", "data": {"id": 100, "match_status": "confirmed", "agreed_price": 150.0}}""",
            ),
        )

        val result = repo.getMatch(100)
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrThrow().id)
    }

    @Test
    fun `getMatch returns failure on 404`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Not found"}"""))

        val result = repo.getMatch(999)
        assertTrue(result.isFailure)
    }

    // -- confirmMatch --

    @Test
    fun `confirmMatch returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Confirmed", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        val result = repo.confirmMatch(100)
        assertTrue(result.isSuccess)
    }

    // -- cancelMatch --

    @Test
    fun `cancelMatch returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Cancelled"}""",
            ),
        )

        val result = repo.cancelMatch(100)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelMatch returns failure on 403`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"message":"Cannot cancel"}"""))

        val result = repo.cancelMatch(100)
        assertTrue(result.isFailure)
    }

    // -- generatePickupCode --

    @Test
    fun `generatePickupCode returns code on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Code generated", "data": {"code": "123456", "expires_at": "2026-04-01T18:00:00Z"}}""",
            ),
        )

        val result = repo.generatePickupCode(100)
        assertTrue(result.isSuccess)
        assertEquals("123456", result.getOrThrow())
    }

    // -- shipperAccept --

    @Test
    fun `shipperAccept returns match on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        val result = repo.shipperAccept(100)
        assertTrue(result.isSuccess)
    }
}
