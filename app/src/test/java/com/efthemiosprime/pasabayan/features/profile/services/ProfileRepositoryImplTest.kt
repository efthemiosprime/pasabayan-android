package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ProfileRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: ProfileRepositoryImpl

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
        val api = retrofit.create(ProfileApi::class.java)
        repo = ProfileRepositoryImpl(api, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun json200(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    @Test
    fun `fetchProfile returns data and caches`() = runBlocking {
        val body =
            """
            {
              "success": true,
              "data": {
                "profile": { "id": 1, "full_name": "A", "verification_level": "basic" },
                "home_city_id": 1,
                "is_complete": false
              }
            }
            """.trimIndent()
        server.enqueue(json200(body))
        val first = repo.fetchProfile(forceRefresh = false)
        assertTrue(first.isSuccess)
        assertEquals("basic", first.getOrNull()?.profile?.verificationLevel)
        val second = repo.fetchProfile(forceRefresh = false)
        assertTrue(second.isSuccess)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `fetchProfile with force refresh bypasses cache`() = runBlocking {
        val body1 =
            """
            {
              "success": true,
              "data": {
                "profile": { "full_name": "One" },
                "is_complete": false
              }
            }
            """.trimIndent()
        val body2 =
            """
            {
              "success": true,
              "data": {
                "profile": { "full_name": "Two" },
                "is_complete": true
              }
            }
            """.trimIndent()
        server.enqueue(json200(body1))
        server.enqueue(json200(body2))
        val a = repo.fetchProfile(forceRefresh = true)
        assertEquals("One", a.getOrNull()?.profile?.fullName)
        val b = repo.fetchProfile(forceRefresh = true)
        assertEquals("Two", b.getOrNull()?.profile?.fullName)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `createCarrierProfile on 409 falls back to get carrier profile`() = runBlocking {
        val carrierBody =
            """
            {
              "message": "ok",
              "data": {
                "id": 9,
                "user_id": 1,
                "max_weight_capacity_kg": 1.0,
                "default_price_per_kg": 1.0,
                "max_space_capacity_liters": 0.0
              }
            }
            """.trimIndent()
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "message": "exists" }"""),
        )
        server.enqueue(json200(carrierBody))
        val create = CreateCarrierProfileRequestJson(
            maxWeightCapacityKg = 50.0,
            maxSpaceCapacityLiters = 0.0,
            defaultPricePerKg = 2.0,
        )
        val result = repo.createCarrierProfile(create)
        assertTrue(result.isSuccess)
        assertEquals(9, result.getOrNull()?.id)
        assertTrue(server.requestCount == 2)
    }

    @Test
    fun `fetchUserStats success`() = runBlocking {
        server.enqueue(
            json200("""{ "success": true, "data": { "packages_count": 3, "delivered_count": 1 } }"""),
        )
        val r = repo.fetchUserStats()
        assertTrue(r.isSuccess)
        assertEquals(3, r.getOrNull()?.packagesCount)
    }

    @Test
    fun `fetchProfile maps 401`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "message": "nope" }"""),
        )
        val r = repo.fetchProfile(forceRefresh = true)
        assertTrue(r.isFailure)
        assertNotNull(r.exceptionOrNull())
    }
}
