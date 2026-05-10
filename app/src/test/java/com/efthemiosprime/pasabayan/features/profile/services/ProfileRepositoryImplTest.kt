package com.efthemiosprime.pasabayan.features.profile.services

import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.CreateCarrierProfileRequestJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileRequestJson
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

    @Test
    fun `updateProfile success replaces cached profile`() = runBlocking {
        val initial =
            """
            {
              "success": true,
              "data": {
                "profile": { "id": 1, "full_name": "Old", "verification_level": "basic" },
                "is_complete": false
              }
            }
            """.trimIndent()
        val updated =
            """
            {
              "success": true,
              "data": {
                "profile": { "id": 1, "full_name": "New", "verification_level": "basic" },
                "is_complete": true
              }
            }
            """.trimIndent()
        server.enqueue(json200(initial))
        server.enqueue(json200(updated))
        // Warm cache.
        repo.fetchProfile(forceRefresh = false)
        val r = repo.updateProfile(UpdateProfileRequestJson(fullName = "New"))
        assertTrue(r.isSuccess)
        assertEquals("New", r.getOrNull()?.profile?.fullName)
        // Next non-forced fetch returns the cache replacement, no new request.
        val third = repo.fetchProfile(forceRefresh = false)
        assertEquals("New", third.getOrNull()?.profile?.fullName)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `uploadProfileAvatar sends multipart with image and default contact method`() = runBlocking {
        val body =
            """
            {
              "success": true,
              "data": {
                "profile": { "id": 1, "full_name": "U", "verification_level": "verified" },
                "is_complete": true
              }
            }
            """.trimIndent()
        server.enqueue(json200(body))
        val r = repo.uploadProfileAvatar(
            imageBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47),
            mimeType = "image/png",
            fileName = "avatar.png",
        )
        assertTrue(r.isSuccess)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path?.endsWith("/profile") == true)
        val ct = req.getHeader("Content-Type") ?: ""
        assertTrue("multipart/form-data not set: $ct", ct.startsWith("multipart/form-data"))
        val text = req.body.readUtf8()
        assertTrue("missing profile_picture part", text.contains("name=\"profile_picture\""))
        assertTrue(text.contains("filename=\"avatar.png\""))
        assertTrue(text.contains("image/png"))
        assertTrue("default contact method missing", text.contains("name=\"preferred_contact_method\"") && text.contains("app_notification"))
    }

    @Test
    fun `deleteProfilePicture invalidates cache`() = runBlocking {
        val initial =
            """
            {
              "success": true,
              "data": {
                "profile": { "id": 1, "full_name": "U", "verification_level": "basic" },
                "is_complete": true
              }
            }
            """.trimIndent()
        server.enqueue(json200(initial))
        server.enqueue(json200("""{ "success": true, "message": "deleted" }"""))
        server.enqueue(json200(initial))
        repo.fetchProfile(forceRefresh = false)
        val d = repo.deleteProfilePicture()
        assertTrue(d.isSuccess)
        repo.fetchProfile(forceRefresh = false)
        assertEquals("cache should be invalidated; expected 3 calls", 3, server.requestCount)
    }

    @Test
    fun `requestAccountDeletion accepts 202 and decodes data`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "success": true,
                      "message": "Account deletion requested",
                      "data": {
                        "request_id": "42",
                        "status": "pending",
                        "requested_at": "2026-03-01T12:00:00.123456Z"
                      }
                    }
                    """.trimIndent(),
                ),
        )
        val r = repo.requestAccountDeletion(reason = "No longer using the app")
        assertTrue(r.isSuccess)
        assertEquals("pending", r.getOrNull()?.status)
        val req = server.takeRequest()
        val text = req.body.readUtf8()
        assertTrue("confirm true missing", text.contains("\"confirm\":true"))
        assertTrue("reason missing", text.contains("No longer using the app"))
    }

    @Test
    fun `fetchDisclaimerAcknowledgments returns empty data when absent`() = runBlocking {
        server.enqueue(json200("""{ "success": true }"""))
        val r = repo.fetchDisclaimerAcknowledgments()
        assertTrue(r.isSuccess)
        val data = r.getOrNull()!!
        assertEquals(null, data.carrierTrip)
        assertEquals(null, data.shipper)
    }

    @Test
    fun `acknowledgeDisclaimer posts type and returns acknowledged data`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": { "disclaimer_type": "carrier_trip", "acknowledged_at": "2026-03-01T12:00:00Z" }
                }
                """.trimIndent(),
            ),
        )
        val r = repo.acknowledgeDisclaimer("carrier_trip")
        assertTrue(r.isSuccess)
        assertEquals("carrier_trip", r.getOrNull()?.disclaimerType)
        val req = server.takeRequest()
        assertTrue(req.body.readUtf8().contains("\"disclaimer_type\":\"carrier_trip\""))
    }

    @Test
    fun `updateConsentPreferences sends only changed key`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "success": true,
                  "data": {
                    "push_notifications": true,
                    "location_tracking": false,
                    "analytics": false,
                    "marketing_communications": false
                  }
                }
                """.trimIndent(),
            ),
        )
        val r = repo.updateConsentPreferences(ConsentPreferencesUpdateJson(pushNotifications = true))
        assertTrue(r.isSuccess)
        assertTrue(r.getOrNull()!!.pushNotifications)
        val req = server.takeRequest()
        val text = req.body.readUtf8()
        assertTrue("push_notifications not sent: $text", text.contains("push_notifications"))
        assertFalse("scoped update should omit untouched keys: $text", text.contains("location_tracking"))
        assertFalse(text.contains("analytics"))
        assertFalse(text.contains("marketing_communications"))
    }

    @Test
    fun `exportUserData returns raw bytes`() = runBlocking {
        val payload = """{"hello":"world"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(payload),
        )
        val r = repo.exportUserData()
        assertTrue(r.isSuccess)
        assertEquals(payload, String(r.getOrNull()!!))
    }

    @Test
    fun `updateCarrierProfile success returns carrier data`() = runBlocking {
        server.enqueue(
            json200(
                """
                {
                  "message": "ok",
                  "data": {
                    "id": 9,
                    "user_id": 1,
                    "max_weight_capacity_kg": 100.0,
                    "max_space_capacity_liters": 0.0,
                    "default_price_per_kg": 5.0
                  }
                }
                """.trimIndent(),
            ),
        )
        val r = repo.updateCarrierProfile(
            CreateCarrierProfileRequestJson(
                maxWeightCapacityKg = 100.0,
                maxSpaceCapacityLiters = 0.0,
                defaultPricePerKg = 5.0,
            ),
        )
        assertTrue(r.isSuccess)
        assertEquals(9, r.getOrNull()?.id)
        assertEquals("PUT", server.takeRequest().method)
    }

    @Test
    fun `enableCarrier returns success on 200`() = runBlocking {
        server.enqueue(json200("""{ "message": "ok" }"""))
        val r = repo.enableCarrier()
        assertTrue(r.isSuccess)
    }
}
