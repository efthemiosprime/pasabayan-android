package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.network.trips.TripsApi
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

class TripsRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: TripsRepositoryImpl

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
        val tripsApi = retrofit.create(TripsApi::class.java)
        repo = TripsRepositoryImpl(tripsApi, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- loadCarrierTrips --

    @Test
    fun `loadCarrierTrips returns trips on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "origin_city": "Toronto", "destination_city": "Vancouver",
                             "trip_status": "active", "transportation_method": "flight"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 1, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadCarrierTrips()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("Toronto", result.getOrThrow()[0].originCity)
    }

    @Test
    fun `loadCarrierTrips falls back to carrier trips on role error`() = runBlocking {
        // First call returns role error
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"message": "You are not registered as both carrier and shipper"}""",
            ),
        )
        // Fallback call succeeds
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 2, "origin_city": "Montreal", "destination_city": "Ottawa",
                             "trip_status": "planning", "transportation_method": "car"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 1, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadCarrierTrips()
        assertTrue(result.isSuccess)
        assertEquals("Montreal", result.getOrThrow()[0].originCity)

        // Verify fallback endpoint was called
        val firstRequest = server.takeRequest()
        assertTrue(firstRequest.path!!.contains("/trips"))
        val secondRequest = server.takeRequest()
        assertTrue(secondRequest.path!!.contains("/carrier/trips"))
    }

    @Test
    fun `loadCarrierTrips returns failure on non-role error`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(401).setBody(
                """{"message": "Unauthenticated"}""",
            ),
        )

        val result = repo.loadCarrierTrips()
        assertTrue(result.isFailure)
    }

    // -- loadAvailableTrips --

    @Test
    fun `loadAvailableTrips returns trips on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "data": {
                        "data": [
                            {"id": 3, "origin_city": "Calgary", "destination_city": "Edmonton",
                             "trip_status": "active", "transportation_method": "bus"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 1, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadAvailableTrips()
        assertTrue(result.isSuccess)
        assertEquals("Calgary", result.getOrThrow()[0].originCity)
    }

    @Test
    fun `loadAvailableTrips returns empty list on no results`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "data": {
                        "data": [],
                        "current_page": 1, "last_page": 1, "total": 0, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.loadAvailableTrips()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    // -- getTrip --

    @Test
    fun `getTrip returns trip on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "OK",
                    "data": {"id": 5, "origin_city": "Winnipeg", "destination_city": "Regina",
                             "trip_status": "completed", "transportation_method": "train"}
                }""",
            ),
        )

        val result = repo.getTrip(5)
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow().id)
    }

    @Test
    fun `getTrip returns failure on 404`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(404).setBody(
                """{"message": "Trip not found"}""",
            ),
        )

        val result = repo.getTrip(999)
        assertTrue(result.isFailure)
    }

    // -- deleteTrip --

    @Test
    fun `deleteTrip returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"message": "Trip deleted", "success": true}""",
            ),
        )

        val result = repo.deleteTrip(1)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteTrip returns failure on 403`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"message": "Not authorized"}""",
            ),
        )

        val result = repo.deleteTrip(1)
        assertTrue(result.isFailure)
    }
}
