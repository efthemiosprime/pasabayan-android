package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.trips.TripsApi
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
import java.util.concurrent.TimeUnit

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

    @Test
    fun `createTrip returns trip on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message":"Created",
                    "data":{"id": 11, "origin_city":"Toronto", "destination_city":"Ottawa",
                            "trip_status":"planning", "transportation_method":"car"}
                }""",
            ),
        )

        val result = repo.createTrip(
            com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson(
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Ottawa",
                destinationCountry = "Canada",
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T12:00:00Z",
                availableWeightKg = 10.0,
                transportationMethod = "car",
                flatTripPrice = 25.0,
            ),
        )
        assertTrue(result.isSuccess)
        assertEquals(11, result.getOrThrow().id)
    }

    @Test
    fun `createTrip returns failure on validation error`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(422).setBody(
                """{
                    "message":"Validation failed",
                    "errors":{"departure_date":["Departure required"]}
                }""",
            ),
        )
        val result = repo.createTrip(
            com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson(
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Ottawa",
                destinationCountry = "Canada",
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T12:00:00Z",
                availableWeightKg = 10.0,
                transportationMethod = "car",
                flatTripPrice = 25.0,
            ),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `createTrip maps mixed transport message to MixedTransportTypes`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"message":"Trip must be cargo-only or passenger-only, not both"}""",
            ),
        )
        val result = repo.createTrip(testCreateTripRequest())
        assertTrue(result.isFailure)
        assertDomainError(result, DomainError.MixedTransportTypes)
    }

    @Test
    fun `createTrip maps missing transport message to NoTransportTypeSpecified`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"message":"You must specify either cargo transport or passenger transport"}""",
            ),
        )
        val result = repo.createTrip(testCreateTripRequest())
        assertTrue(result.isFailure)
        assertDomainError(result, DomainError.NoTransportTypeSpecified)
    }

    @Test
    fun `createTrip maps unauthenticated response to Unauthenticated`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(401).setBody(
                """{"message":"Unauthenticated"}""",
            ),
        )
        val result = repo.createTrip(testCreateTripRequest())
        assertTrue(result.isFailure)
        assertDomainError(result, DomainError.Unauthenticated)
    }

    @Test
    fun `createTrip maps not registered carrier response to UserNotCarrier`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"message":"You are not registered as a carrier"}""",
            ),
        )
        val result = repo.createTrip(testCreateTripRequest())
        assertTrue(result.isFailure)
        assertDomainError(result, DomainError.UserNotCarrier)
    }

    @Test
    fun `createTrip returns timeout network error when request exceeds timeout`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeadersDelay(10, TimeUnit.SECONDS)
                .setBody(
                    """{
                        "message":"Created",
                        "data":{"id": 12, "origin_city":"A", "destination_city":"B",
                        "trip_status":"planning", "transportation_method":"car"}
                    }""",
                ),
        )

        val result = repo.createTrip(
            com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson(
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Ottawa",
                destinationCountry = "Canada",
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T12:00:00Z",
                availableWeightKg = 10.0,
                transportationMethod = "car",
                flatTripPrice = 25.0,
            ),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainErrorMapperException)
        val error = (result.exceptionOrNull() as DomainErrorMapperException).domainError
        assertTrue(error is DomainError.NetworkError)
        val cause = (error as DomainError.NetworkError).cause
        assertTrue(cause is java.net.SocketTimeoutException)
    }

    @Test
    fun `updateTrip returns trip on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message":"Updated",
                    "data":{"id": 7, "origin_city":"Toronto", "destination_city":"Montreal",
                            "trip_status":"active", "transportation_method":"flight"}
                }""",
            ),
        )
        val result = repo.updateTrip(
            id = 7,
            request = com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson(tripStatus = "active"),
        )
        assertTrue(result.isSuccess)
        assertEquals("Montreal", result.getOrThrow().destinationCity)
    }

    @Test
    fun `loadPopularPackageRoutes returns mapped routes`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "ok",
                    "data": [
                        {"origin_city":"Toronto","destination_city":"Montreal","package_count":6,"average_price":52.5}
                    ]
                }""",
            ),
        )
        val result = repo.loadPopularPackageRoutes()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("Toronto", result.getOrThrow().first().originCity)
        assertEquals(6, result.getOrThrow().first().packageCount)
    }

    @Test
    fun `loadRouteActivitySummary returns zero-safe summary`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"message":"ok","data":{"total_trips":2,"active_trips":1,"completed_trips":4,"total_earnings":89.75,"currency":"CAD"}}""",
            ),
        )
        val result = repo.loadRouteActivitySummary()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().totalTrips)
        assertEquals(1, result.getOrThrow().activeTrips)
        assertEquals(4, result.getOrThrow().completedTrips)
    }

    @Test
    fun `loadTripTemplate returns mapped template data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message":"ok",
                    "data":{
                        "origin_city":"Toronto",
                        "origin_country":"Canada",
                        "destination_city":"Ottawa",
                        "destination_country":"Canada",
                        "suggested_departure_date":"2026-05-01T10:00:00Z",
                        "suggested_arrival_date":"2026-05-01T15:00:00Z",
                        "suggested_weight_kg":15,
                        "suggested_space_liters":40,
                        "package_details":{"id":99,"description":"Books","weight_kg":3}
                    }
                }""",
            ),
        )
        val result = repo.loadTripTemplate(packageId = 99)
        assertTrue(result.isSuccess)
        assertEquals(99, result.getOrThrow().packageId)
        assertEquals("Books", result.getOrThrow().packageDescription)
    }

    @Test
    fun `loadTripMatches returns mapped matches`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "trip":{"id":1,"trip_status":"active","transportation_method":"car"},
                    "matches":[
                        {"id":5,"match_status":"confirmed","package":{"id":9,"description":"Parcel","package_weight_kg":2.5}}
                    ]
                }""",
            ),
        )
        val result = repo.loadTripMatches(1)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(9, result.getOrThrow().first().packageId)
    }

    @Test
    fun `createTripFromPackage injects auto request fields`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message":"Created",
                    "data":{"id": 19, "origin_city":"Toronto", "destination_city":"Ottawa",
                            "trip_status":"planning", "transportation_method":"car"}
                }""",
            ),
        )
        val result = repo.createTripFromPackage(
            com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest(
                packageId = 77,
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Ottawa",
                destinationCountry = "Canada",
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T12:00:00Z",
                availableWeightKg = 10.0,
                availableSpaceLiters = 20.0,
                transportationMethod = "car",
                pricePerKg = null,
                flatTripPrice = 30.0,
                specialNotes = "Handle with care",
                pickupAddress = "1 A St",
                dropoffAddress = "2 B St",
                proposedPrice = 100.0,
                requestMessage = "I can carry this",
            ),
        )
        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"auto_request_package_id\":77"))
        assertTrue(body.contains("\"proposed_price\":100.0"))
    }

    // -- activateTrip --

    @Test
    fun `activateTrip posts to activate endpoint with empty body`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message":"Trip activated",
                    "data":{"id": 21, "origin_city":"Toronto", "destination_city":"Montreal",
                            "trip_status":"active", "transportation_method":"car"}
                }""",
            ),
        )

        val result = repo.activateTrip(21)

        assertTrue(result.isSuccess)
        assertEquals(21, result.getOrThrow().id)
        assertEquals(
            com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.ACTIVE,
            result.getOrThrow().tripStatus,
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(
            "expected /trips/{id}/activate path, got ${request.path}",
            request.path!!.endsWith("/trips/21/activate"),
        )
        assertEquals(0L, request.bodySize)
    }

    @Test
    fun `activateTrip returns failure on 4xx`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"message":"Trip cannot be activated in this state"}""",
            ),
        )
        val result = repo.activateTrip(21)
        assertTrue(result.isFailure)
    }

    @Test
    fun `activateTrip wraps network failure as NetworkError`() = runBlocking {
        server.shutdown()
        val result = repo.activateTrip(21)
        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as DomainErrorMapperException).domainError
        assertTrue(error is DomainError.NetworkError)
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

    private fun testCreateTripRequest() = com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson(
        originCity = "Toronto",
        originCountry = "Canada",
        destinationCity = "Ottawa",
        destinationCountry = "Canada",
        departureDate = "2026-04-01T08:00:00Z",
        arrivalDate = "2026-04-01T12:00:00Z",
        availableWeightKg = 10.0,
        transportationMethod = "car",
        flatTripPrice = 25.0,
    )

    private fun assertDomainError(result: Result<*>, expected: DomainError) {
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainErrorMapperException)
        val mapped = (exception as DomainErrorMapperException).domainError
        assertFalse(mapped is DomainError.ServerError)
        assertEquals(expected, mapped)
    }
}
