package com.efthemiosprime.pasabayan.features.packages.services

import com.efthemiosprime.pasabayan.core.network.packages.PackagesApi
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

class PackagesRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: PackagesRepositoryImpl

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
        val api = retrofit.create(PackagesApi::class.java)
        repo = PackagesRepositoryImpl(api, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `loadPackages returns packages on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "pickup_city": "Toronto", "delivery_city": "Montreal", "request_status": "open"},
                            {"id": 2, "pickup_city": "Ottawa", "delivery_city": "Calgary", "request_status": "matched"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadPackages()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertEquals("Toronto", result.getOrThrow()[0].pickupCity)
    }

    @Test
    fun `loadPackages returns failure on error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Unauthenticated"}"""))

        val result = repo.loadPackages()
        assertTrue(result.isFailure)
    }

    @Test
    fun `loadPackages returns empty list on no data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message": "OK", "data": {"data": [], "current_page": 1, "last_page": 1, "total": 0, "per_page": 15}}""",
            ),
        )

        val result = repo.loadPackages()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `getPackage returns package on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message": "OK", "data": {"id": 10, "pickup_city": "Vancouver", "delivery_city": "Toronto"}}""",
            ),
        )

        val result = repo.getPackage(10)
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrThrow().id)
    }

    @Test
    fun `getPackage returns failure on 404`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Not found"}"""))

        val result = repo.getPackage(999)
        assertTrue(result.isFailure)
    }

    @Test
    fun `cancelPackage returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Cancelled", "data": {"id": 10, "request_status": "cancelled"}}""",
            ),
        )

        val result = repo.cancelPackage(10)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelPackage returns failure on 403`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"message":"Not authorized"}"""))

        val result = repo.cancelPackage(10)
        assertTrue(result.isFailure)
    }
}
