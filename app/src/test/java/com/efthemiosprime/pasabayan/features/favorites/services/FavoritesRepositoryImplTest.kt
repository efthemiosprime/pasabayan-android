package com.efthemiosprime.pasabayan.features.favorites.services

import com.efthemiosprime.pasabayan.core.network.favorites.FavoritesApi
import com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson
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

class FavoritesRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: FavoritesRepositoryImpl

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
        val api = retrofit.create(FavoritesApi::class.java)
        repo = FavoritesRepositoryImpl(api, json)
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
    fun `fetchFavorites parses list and sends sort query`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": [
                    {
                      "id": 1,
                      "carrier": { "id": 42, "name": "Alex" },
                      "total_deliveries_together": 3,
                      "has_upcoming_trips": true,
                      "upcoming_trips_count": 2
                    }
                  ],
                  "total": 1, "limit": 50, "remaining": 49
                }
                """.trimIndent(),
            ),
        )
        val r = repo.fetchFavorites(sort = "most_used", hasUpcomingTrips = true)
        assertTrue(r.isSuccess)
        assertEquals(42, r.getOrNull()?.firstOrNull()?.carrier?.id)
        val req = server.takeRequest()
        assertTrue(req.path?.contains("sort=most_used") == true)
        assertTrue(req.path?.contains("has_upcoming_trips=true") == true)
    }

    @Test
    fun `addFavorite 201 succeeds and sends notes plus notification_enabled`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201))
        val r = repo.addFavorite(carrierId = 7, notes = "Reliable", notificationEnabled = true)
        assertTrue(r.isSuccess)
        val req = server.takeRequest()
        assertTrue(req.path?.endsWith("/carriers/7/favorite") == true)
        val text = req.body.readUtf8()
        assertTrue(text.contains("Reliable"))
        assertTrue(text.contains("notification_enabled"))
    }

    @Test
    fun `addFavorite 409 maps to AlreadyFavorited`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(409))
        val r = repo.addFavorite(carrierId = 7)
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is FavoritesError.AlreadyFavorited)
    }

    @Test
    fun `addFavorite 400 maps to CannotFavorite with parsed message`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"success": false, "message": "Can't favorite yourself"}"""),
        )
        val r = repo.addFavorite(carrierId = 7)
        assertTrue(r.isFailure)
        val err = r.exceptionOrNull()
        assertTrue(err is FavoritesError.CannotFavorite)
        assertEquals("Can't favorite yourself", (err as FavoritesError.CannotFavorite).reason)
    }

    @Test
    fun `addFavorite 404 maps to NotFound`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        val r = repo.addFavorite(carrierId = 9)
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is FavoritesError.NotFound)
    }

    @Test
    fun `removeFavorite success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        val r = repo.removeFavorite(carrierId = 11)
        assertTrue(r.isSuccess)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `isFavorite parses boolean`() = runBlocking {
        server.enqueue(json200("""{ "success": true, "is_favorite": true }"""))
        val r = repo.isFavorite(carrierId = 11)
        assertTrue(r.isSuccess)
        assertEquals(true, r.getOrNull())
    }

    @Test
    fun `sendDeliveryRequest posts payload`() = runBlocking {
        server.enqueue(json200("""{ "success": true }"""))
        val r = repo.sendDeliveryRequest(
            carrierId = 11,
            request = SendDeliveryRequestJson(
                pickupCity = "Montreal",
                pickupDatePreferred = "2026-06-01",
                deliveryCity = "Toronto",
                deliveryDateNeeded = "2026-06-03",
                packageDescription = "Books",
                packageWeightKg = 2.0,
                packageType = "general",
                offeredPrice = 50.0,
            ),
        )
        assertTrue(r.isSuccess)
        val req = server.takeRequest()
        assertTrue(req.path?.endsWith("/carriers/11/request-delivery") == true)
        val text = req.body.readUtf8()
        assertTrue(text.contains("Montreal"))
        assertTrue(text.contains("offered_price"))
    }

    @Test
    fun `fetchSentRequests returns list`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": [ { "id": 1, "status": "pending" } ],
                  "total": 1
                }
                """.trimIndent(),
            ),
        )
        val r = repo.fetchSentRequests()
        assertTrue(r.isSuccess)
        assertEquals(1, r.getOrNull()?.size)
    }

    @Test
    fun `fetchFavorites maps 401`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("""{ "message": "nope" }"""),
        )
        val r = repo.fetchFavorites()
        assertTrue(r.isFailure)
        assertNotNull(r.exceptionOrNull())
    }
}
