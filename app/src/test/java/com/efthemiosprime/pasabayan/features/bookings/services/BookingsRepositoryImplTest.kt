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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `cancelMatch returns CancelMatchResult with refund and conversation`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "message": "Cancelled and refunded",
                    "data": {"id": 100, "match_status": "cancelled", "agreed_price": "150.50"},
                    "chat_conversation_id": 77,
                    "refund": {"processed": true, "amount": 150.50, "transaction_id": 555}
                }""",
            ),
        )

        val result = repo.cancelMatch(100)
        assertTrue(result.isSuccess)
        val cancel = result.getOrThrow()
        assertEquals(100, cancel.match.id)
        assertEquals(77, cancel.chatConversationId)
        assertNotNull(cancel.refund)
        assertTrue(cancel.refund!!.processed)
        assertEquals(555, cancel.refund!!.transactionId)
    }

    @Test
    fun `cancelMatch returns failure when response is missing match data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"message": "Cancelled"}"""),
        )

        val result = repo.cancelMatch(100)
        assertTrue(result.isFailure)
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

    // -- shipperAcceptCarrierRequest (new contract — PUT /matches/{id}/accept-carrier-request) --

    @Test
    fun `shipperAcceptCarrierRequest hits PUT accept-carrier-request`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        val result = repo.shipperAcceptCarrierRequest(100)
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/matches/100/accept-carrier-request", request.path)
    }

    @Test
    fun `shipperAcceptCarrierRequest omits acknowledge_overage when not requested`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        repo.shipperAcceptCarrierRequest(100)

        val body = server.takeRequest().body.readUtf8()
        assertFalse("payload should not include acknowledge_overage", body.contains("acknowledge_overage"))
    }

    @Test
    fun `shipperAcceptCarrierRequest sends acknowledge_overage true when acknowledged`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        repo.shipperAcceptCarrierRequest(100, acknowledgeOverage = true)

        val body = server.takeRequest().body.readUtf8()
        assertTrue("payload should include acknowledge_overage=true", body.contains("\"acknowledge_overage\":true"))
    }

    // -- carrierAcceptShipperRequest (PUT /matches/{id}/accept-shipper-request) --

    @Test
    fun `carrierAcceptShipperRequest hits PUT accept-shipper-request`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "carrier_accepted"}}""",
            ),
        )

        val result = repo.carrierAcceptShipperRequest(100)
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/matches/100/accept-shipper-request", request.path)
    }

    @Test
    fun `carrierAcceptShipperRequest sends acknowledge_overage true when acknowledged`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "carrier_accepted"}}""",
            ),
        )

        repo.carrierAcceptShipperRequest(100, acknowledgeOverage = true)

        val body = server.takeRequest().body.readUtf8()
        assertTrue("payload should include acknowledge_overage=true", body.contains("\"acknowledge_overage\":true"))
    }

    // -- shipperRequestTrip envelope (iOS parity 8c9646d) --

    @Test
    fun `shipperRequestTrip returns RequestMatchResult with negotiation metadata`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                    "success": true,
                    "message": "Counter-offer submitted",
                    "data": {"id": 301, "match_status": "shipper_requested", "agreed_price": "135.00"},
                    "warnings": ["dates_misaligned"],
                    "negotiation_needed": true,
                    "is_counter_offer": true
                }""",
            ),
        )

        val result = repo.shipperRequestTrip(
            packageId = 10,
            tripId = 1,
            offeredPrice = 135.0,
            message = "Can you do less?",
            isCounterOffer = true,
            originalMatchId = 300,
            originalPrice = 150.0,
        )
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals(301, payload.match.id)
        assertEquals(listOf("dates_misaligned"), payload.negotiation.warnings)
        assertTrue(payload.negotiation.negotiationNeeded)
        assertTrue(payload.negotiation.isCounterOffer)
    }

    @Test
    fun `shipperRequestTrip surfaces compatibility when over capacity`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                    "success": true,
                    "message": "Trip request sent to carrier successfully",
                    "data": {"id": 301, "match_status": "shipper_requested", "agreed_price": "135.00"},
                    "compatibility": {
                        "weight_over_capacity": true,
                        "package_weight_kg": 2.0,
                        "trip_available_weight_kg": 1.0,
                        "overage_kg": 1.0,
                        "dates_misaligned": false,
                        "route_uncertain": false,
                        "requires_capacity_acknowledgment": true
                    },
                    "is_counter_offer": false
                }""",
            ),
        )

        val result = repo.shipperRequestTrip(
            packageId = 10, tripId = 1, offeredPrice = 135.0, message = null,
        )
        assertTrue(result.isSuccess)
        val compat = result.getOrThrow().compatibility
        assertNotNull(compat)
        assertTrue(compat!!.weightOverCapacity)
        assertTrue(compat.requiresCapacityAcknowledgment)
        assertEquals(2.0, compat.packageWeightKg!!, 0.001)
        assertEquals(1.0, compat.tripAvailableWeightKg!!, 0.001)
        assertEquals(1.0, compat.overageKg!!, 0.001)
    }

    @Test
    fun `shipperRequestTrip leaves compatibility null when envelope omits it`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                    "success": true,
                    "message": "OK",
                    "data": {"id": 302, "match_status": "shipper_requested", "agreed_price": "100.00"}
                }""",
            ),
        )

        val result = repo.shipperRequestTrip(
            packageId = 10, tripId = 1, offeredPrice = 100.0, message = null,
        )
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow().compatibility)
    }

    @Test
    fun `shipperRequestTrip fails when envelope omits data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"success": false, "message": "Validation failed"}"""),
        )

        val result = repo.shipperRequestTrip(
            packageId = 10, tripId = 1, offeredPrice = 100.0, message = null,
        )
        assertTrue(result.isFailure)
    }

    // -- carrierRequestPackage envelope --

    @Test
    fun `carrierRequestPackage returns RequestMatchResult and warnings`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "Request sent",
                    "data": {"id": 300, "match_status": "carrier_requested", "agreed_price": "150.00"},
                    "warnings": ["pickup_address_outside_range"],
                    "negotiation_needed": true,
                    "is_counter_offer": false
                }""",
            ),
        )

        val result = repo.carrierRequestPackage(
            tripId = 1,
            packageId = 10,
            proposedPrice = 150.0,
            message = "I can carry this",
        )
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals(300, payload.match.id)
        assertEquals(listOf("pickup_address_outside_range"), payload.negotiation.warnings)
        assertFalse(payload.negotiation.isCounterOffer)
    }
}
