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
    fun `confirmMatch returns ConfirmMatchResult with match`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Confirmed", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        val result = repo.confirmMatch(100)
        assertTrue(result.isSuccess)
        val confirm = result.getOrThrow()
        assertEquals(100, confirm.match.id)
        assertNull(confirm.autoCharge)
        assertNull(confirm.chatConversationId)
    }

    @Test
    fun `confirmMatch decodes auto_charge and chat_conversation_id when present`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "Confirmed",
                    "data": {"id": 100, "match_status": "confirmed"},
                    "chat_conversation_id": 77,
                    "auto_charge": {"queued": true, "shipper_has_default_payment_method": true}
                }""",
            ),
        )

        val confirm = repo.confirmMatch(100).getOrThrow()
        assertEquals(77, confirm.chatConversationId)
        assertNotNull(confirm.autoCharge)
        assertTrue(confirm.autoCharge!!.queued)
        assertTrue(confirm.autoCharge!!.shipperHasDefaultPaymentMethod)
    }

    @Test
    fun `confirmMatch surfaces auto_charge with no default payment method`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "Confirmed",
                    "data": {"id": 100, "match_status": "confirmed"},
                    "auto_charge": {"queued": false, "shipper_has_default_payment_method": false}
                }""",
            ),
        )

        val confirm = repo.confirmMatch(100).getOrThrow()
        assertNotNull(confirm.autoCharge)
        assertFalse(confirm.autoCharge!!.queued)
        assertFalse(confirm.autoCharge!!.shipperHasDefaultPaymentMethod)
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

    // -- shipperAcceptCarrierRequest (iOS parity — PUT /matches/{id}/accept) --

    @Test
    fun `shipperAcceptCarrierRequest hits PUT accept`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Accepted", "data": {"id": 100, "match_status": "confirmed"}}""",
            ),
        )

        val result = repo.shipperAcceptCarrierRequest(100)
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/matches/100/accept", request.path)
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

    // -- shipperDeclineCarrierRequest (iOS parity — PUT /matches/{id}/decline) --

    @Test
    fun `shipperDeclineCarrierRequest hits PUT decline`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "carrier_declined"}}""",
            ),
        )

        val result = repo.shipperDeclineCarrierRequest(100)
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/matches/100/decline", request.path)
    }

    @Test
    fun `shipperDeclineCarrierRequest omits reason when null`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "carrier_declined"}}""",
            ),
        )

        repo.shipperDeclineCarrierRequest(100, reason = null)

        val body = server.takeRequest().body.readUtf8()
        assertFalse("payload should not include reason when null", body.contains("reason"))
    }

    @Test
    fun `shipperDeclineCarrierRequest sends reason when provided`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "carrier_declined"}}""",
            ),
        )

        repo.shipperDeclineCarrierRequest(100, reason = "Schedule conflict")

        val body = server.takeRequest().body.readUtf8()
        assertTrue("payload should include reason", body.contains("\"reason\":\"Schedule conflict\""))
    }

    @Test
    fun `shipperDeclineCarrierRequest treats blank reason as null`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "carrier_declined"}}""",
            ),
        )

        repo.shipperDeclineCarrierRequest(100, reason = "   ")

        val body = server.takeRequest().body.readUtf8()
        assertFalse("blank reason should be omitted", body.contains("reason"))
    }

    // -- carrierDeclineShipperRequest (PUT /matches/{id}/decline-shipper-request, with optional reason) --

    @Test
    fun `carrierDeclineShipperRequest hits PUT decline-shipper-request`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "shipper_declined"}}""",
            ),
        )

        val result = repo.carrierDeclineShipperRequest(100)
        assertTrue(result.isSuccess)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/matches/100/decline-shipper-request", request.path)
    }

    @Test
    fun `carrierDeclineShipperRequest sends reason when provided`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "shipper_declined"}}""",
            ),
        )

        repo.carrierDeclineShipperRequest(100, reason = "Capacity full")

        val body = server.takeRequest().body.readUtf8()
        assertTrue("payload should include reason", body.contains("\"reason\":\"Capacity full\""))
    }

    @Test
    fun `carrierDeclineShipperRequest omits reason when null`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Declined", "data": {"id": 100, "match_status": "shipper_declined"}}""",
            ),
        )

        repo.carrierDeclineShipperRequest(100, reason = null)

        val body = server.takeRequest().body.readUtf8()
        assertFalse("payload should not include reason when null", body.contains("reason"))
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

    // -- getCarrierLocation --

    @Test
    fun `getCarrierLocation maps current location plus String delivery coords`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "data": {
                        "match_id": 100,
                        "carrier": {"id": 42, "name": "Carrie"},
                        "current_location": {
                            "latitude": 14.5995,
                            "longitude": 120.9842,
                            "last_updated_at": "2026-05-13T10:15:00Z",
                            "is_stale": false
                        },
                        "delivery_address": {
                            "address": "123 Main St",
                            "city": "Manila",
                            "latitude": "14.6760",
                            "longitude": "121.0437"
                        },
                        "match_status": "in_transit"
                    }
                }""",
            ),
        )

        val result = repo.getCarrierLocation(100)
        assertTrue(result.isSuccess)
        val snapshot = result.getOrThrow()
        assertEquals(100, snapshot.matchId)
        assertEquals(14.5995, snapshot.carrierLat!!, 0.0001)
        assertEquals(120.9842, snapshot.carrierLng!!, 0.0001)
        assertEquals(false, snapshot.isStale)
        assertEquals(14.6760, snapshot.deliveryLat!!, 0.0001)
        assertEquals(121.0437, snapshot.deliveryLng!!, 0.0001)
        assertTrue(snapshot.hasCarrierLocation)
        assertTrue(snapshot.hasDeliveryLocation)
    }

    @Test
    fun `getCarrierLocation returns failure on 404`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Not found"}"""))

        val result = repo.getCarrierLocation(404)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getCarrierLocation returns InvalidResponse when data missing`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"success": true}"""),
        )

        val result = repo.getCarrierLocation(100)
        assertTrue(result.isFailure)
    }

    // -- getCompatibleTrips --

    @Test
    fun `getCompatibleTrips returns trips on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "carrier_id": 10, "origin_city": "Manila",
                             "destination_city": "Cebu", "trip_status": "active",
                             "transportation_method": "flight",
                             "available_weight_kg": "20.0", "price_per_kg": "5.50",
                             "can_request": true},
                            {"id": 2, "carrier_id": 20, "origin_city": "Manila",
                             "destination_city": "Cebu", "trip_status": "active",
                             "transportation_method": "flight",
                             "available_weight_kg": "10.0", "price_per_kg": "6.00",
                             "shipper_request_status": "shipper_requested"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                    }
                }""",
            ),
        )

        val result = repo.getCompatibleTrips(packageRequestId = 42)
        assertTrue(result.isSuccess)
        val trips = result.getOrThrow()
        assertEquals(2, trips.size)
        assertEquals(1, trips[0].id)
        assertTrue(trips[0].canRequestTrip)
        assertFalse(trips[0].hasActiveRequest)
        assertTrue(trips[1].hasActiveRequest)
        assertEquals(20.0, trips[0].availableWeightKgDouble!!, 0.0001)
    }

    @Test
    fun `getCompatibleTrips applies client-side carrierId filter`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true, "message": "OK",
                    "data": {
                        "data": [
                            {"id": 1, "carrier_id": 10, "origin_city": "Manila",
                             "destination_city": "Cebu", "trip_status": "active",
                             "transportation_method": "flight"},
                            {"id": 2, "carrier_id": 20, "origin_city": "Manila",
                             "destination_city": "Cebu", "trip_status": "active",
                             "transportation_method": "flight"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                    }
                }""",
            ),
        )

        val trips = repo.getCompatibleTrips(packageRequestId = 42, carrierId = 20).getOrThrow()
        assertEquals(1, trips.size)
        assertEquals(20, trips[0].carrierId)
    }

    @Test
    fun `getCompatibleTrips returns failure on 401`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Unauthenticated"}"""))

        val result = repo.getCompatibleTrips(packageRequestId = 42)
        assertTrue(result.isFailure)
    }

    // -- getCompatiblePackages --

    @Test
    fun `getCompatiblePackages handles nested compatible_packages shape`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true, "message": "OK",
                    "data": {
                        "compatible_packages": {
                            "data": [
                                {"id": 100, "pickup_city": "Manila", "delivery_city": "Cebu"},
                                {"id": 101, "pickup_city": "Manila", "delivery_city": "Davao"}
                            ],
                            "current_page": 1, "last_page": 1, "total": 2, "per_page": 15
                        }
                    }
                }""",
            ),
        )

        val packages = repo.getCompatiblePackages(tripId = 7).getOrThrow()
        assertEquals(2, packages.size)
        assertEquals(100, packages[0].id)
        assertEquals("Cebu", packages[0].deliveryCity)
    }

    @Test
    fun `getCompatiblePackages handles flat data shape`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true, "message": "OK",
                    "data": {
                        "data": [
                            {"id": 200, "pickup_city": "Toronto", "delivery_city": "Montreal"}
                        ],
                        "current_page": 1, "last_page": 1, "total": 1, "per_page": 15
                    }
                }""",
            ),
        )

        val packages = repo.getCompatiblePackages(tripId = 7).getOrThrow()
        assertEquals(1, packages.size)
        assertEquals(200, packages[0].id)
    }

    @Test
    fun `getCompatiblePackages returns failure on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"server"}"""))

        assertTrue(repo.getCompatiblePackages(tripId = 7).isFailure)
    }

    // -- Receiver access --

    @Test
    fun `getReceiverAccess returns tokens on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{
                    "success": true,
                    "data": [
                        {"id": 1, "short_code": "ABC123", "short_url": "https://p.bay/ABC123",
                         "has_pin": true, "pin": "9182", "is_active": true,
                         "created_at": "2026-05-13T10:00:00Z"},
                        {"id": 2, "short_code": "XYZ789", "short_url": "https://p.bay/XYZ789",
                         "has_pin": false, "is_active": false,
                         "created_at": "2026-05-12T10:00:00Z"}
                    ]
                }""",
            ),
        )

        val tokens = repo.getReceiverAccess(matchId = 100).getOrThrow()
        assertEquals(2, tokens.size)
        assertEquals("ABC123", tokens[0].shortCode)
        assertEquals("9182", tokens[0].pin)
        assertTrue(tokens[0].hasPin)
        assertTrue(tokens[0].isActive)
        assertFalse(tokens[1].isActive)
    }

    @Test
    fun `getReceiverAccess returns empty list when data missing`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"success": true}"""),
        )

        val tokens = repo.getReceiverAccess(matchId = 100).getOrThrow()
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `createReceiverAccess returns new token`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                    "success": true,
                    "data": {
                        "id": 99, "short_code": "NEW999", "short_url": "https://p.bay/NEW999",
                        "has_pin": true, "pin": "1234", "is_active": true,
                        "created_at": "2026-05-14T12:00:00Z"
                    }
                }""",
            ),
        )

        val token = repo.createReceiverAccess(matchId = 100, generatePin = true).getOrThrow()
        assertEquals(99, token.id)
        assertEquals("NEW999", token.shortCode)
        assertEquals("1234", token.pin)
    }

    @Test
    fun `createReceiverAccess returns InvalidResponse when data missing`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody("""{"success": true}"""),
        )

        assertTrue(repo.createReceiverAccess(matchId = 100).isFailure)
    }

    @Test
    fun `createReceiverAccess returns failure on 422`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"message":"unavailable"}"""))

        assertTrue(repo.createReceiverAccess(matchId = 100).isFailure)
    }

    @Test
    fun `revokeReceiverAccess returns success on 200`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"success": true, "message": "Revoked"}"""),
        )

        val result = repo.revokeReceiverAccess(matchId = 100, tokenId = 99)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `revokeReceiverAccess returns failure on 404`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"not found"}"""))

        assertTrue(repo.revokeReceiverAccess(matchId = 100, tokenId = 0).isFailure)
    }

    // -- bookTripDirect --

    @Test
    fun `bookTripDirect returns booking on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                    "success": true,
                    "message": "Booked",
                    "data": {
                        "booking": {
                            "id": 9001,
                            "trip_id": 42,
                            "booker_id": 7,
                            "booking_type": "space_only",
                            "status": "pending",
                            "price_agreed": 120.50,
                            "created_at": "2026-05-14T11:30:00Z"
                        }
                    }
                }""",
            ),
        )

        val payload = com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload(
            bookingType = "space_only",
            spaceNeededLiters = 10.0,
            weightNeededKg = 5.0,
            pickupLocation = "1234 Mock St",
            deliveryLocation = "4321 Test Ave",
            priceAgreed = 120.50,
            specialRequirements = "Handle with care",
        )

        val result = repo.bookTripDirect(tripId = 42, payload = payload)
        assertTrue(result.isSuccess)
        val booking = result.getOrThrow()
        assertEquals(9001, booking.bookingId)
        assertEquals(42, booking.tripId)
        assertEquals(120.50, booking.agreedPrice, 0.0001)
        assertEquals("pending", booking.status)
    }

    @Test
    fun `bookTripDirect returns InvalidResponse when booking missing`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody("""{"success": true, "message": "ok"}"""),
        )
        val payload = com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload(
            priceAgreed = 50.0,
        )
        assertTrue(repo.bookTripDirect(tripId = 42, payload = payload).isFailure)
    }

    @Test
    fun `bookTripDirect returns failure on 422 validation`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"message":"trip already full"}"""))
        val payload = com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload(
            priceAgreed = 50.0,
        )
        assertTrue(repo.bookTripDirect(tripId = 42, payload = payload).isFailure)
    }

    // -- submitRating --

    @Test
    fun `submitRating returns match on success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Rated", "data": {"id": 42, "match_status": "delivered", "agreed_price": 150.0}}""",
            ),
        )

        val result = repo.submitRating(matchId = 42, rating = 5, reviewText = "Great delivery!")
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrThrow().id)
    }

    @Test
    fun `submitRating accepts blank review and posts null`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success": true, "message": "Rated", "data": {"id": 42, "match_status": "delivered"}}""",
            ),
        )

        val result = repo.submitRating(matchId = 42, rating = 4, reviewText = "   ")
        assertTrue(result.isSuccess)
        // Body inspection: ensure the request did not include a non-null review_text.
        val recorded = server.takeRequest().body.readUtf8()
        assertTrue(
            "Blank review_text should be omitted or null on the wire; got body=$recorded",
            !recorded.contains("\"review_text\":\"") || recorded.contains("\"review_text\":null"),
        )
    }

    @Test
    fun `submitRating returns failure on 422`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"message":"already rated"}"""))

        assertTrue(repo.submitRating(matchId = 42, rating = 5, reviewText = null).isFailure)
    }

    @Test
    fun `submitRating returns InvalidResponse when body missing data`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"success": true, "message": "ok"}"""),
        )

        assertTrue(repo.submitRating(matchId = 42, rating = 5, reviewText = null).isFailure)
    }
}
