package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.network.notifications.NotificationApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class NotificationRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: NotificationRepositoryImpl
    private lateinit var fakeConsent: FakeConsentIntent

    private val deviceInfo = object : DeviceInfoProvider {
        override val appVersion = "1.0.0"
        override val deviceModel = "Pixel-Test"
        override val osVersion = "14"
        override val deviceName: String? = "Test Device"
    }

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
        fakeConsent = FakeConsentIntent()
        repo = NotificationRepositoryImpl(
            notificationApi = retrofit.create(NotificationApi::class.java),
            profileApi = retrofit.create(ProfileApi::class.java),
            consentIntent = fakeConsent,
            deviceInfo = deviceInfo,
            json = json,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // -- registerDeviceToken --

    @Test
    fun `register happy path returns Registered`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"device_token":{"id":42,"platform":"android","is_active":true,"created_at":"x"}}}""",
            ),
        )
        val result = repo.registerDeviceToken("fcm_token_abc")
        assertTrue(result is DeviceTokenRegisterResult.Registered)
        assertEquals(42, (result as DeviceTokenRegisterResult.Registered).info.id)
    }

    @Test
    fun `register on 403 push_notifications consent auto-opts and retries`() = runBlocking {
        // 1st call → 403 consent_required
        server.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"success":false,"message":"need consent","consent_required":"push_notifications"}""",
            ),
        )
        // 2nd call: PUT /profile/consent-preferences → success
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"push_notifications":true,"location_tracking":false,"analytics":false,"marketing_communications":false}}""",
            ),
        )
        // 3rd call: retry register → success
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"device_token":{"id":5,"platform":"android","is_active":true,"created_at":"x"}}}""",
            ),
        )
        val result = repo.registerDeviceToken("fcm_token_abc")
        assertTrue(
            "expected Registered after retry, got $result",
            result is DeviceTokenRegisterResult.Registered,
        )
        assertEquals(5, (result as DeviceTokenRegisterResult.Registered).info.id)
        assertTrue("opt-in intent should be recorded", fakeConsent.isOptedIn())

        // Order of requests
        assertEquals("/api/device-tokens", server.takeRequest().path)
        val consentReq = server.takeRequest()
        assertEquals("/api/profile/consent-preferences", consentReq.path)
        assertTrue(
            "consent body should set push_notifications true",
            consentReq.body.readUtf8().contains("\"push_notifications\":true"),
        )
        assertEquals("/api/device-tokens", server.takeRequest().path)
    }

    @Test
    fun `register on 403 other consent does not retry`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(403).setBody(
                """{"success":false,"message":"forbidden","consent_required":"location_tracking"}""",
            ),
        )
        val result = repo.registerDeviceToken("fcm_token_abc")
        assertTrue(result is DeviceTokenRegisterResult.ConsentRequired)
        assertEquals(
            "location_tracking",
            (result as DeviceTokenRegisterResult.ConsentRequired).purpose,
        )
        assertFalse("only push_notifications opt-in should be recorded", fakeConsent.isOptedIn())
        // Only one request fired (no retry)
        server.takeRequest()
        assertEquals(0, server.requestCount - 1)
    }

    @Test
    fun `register with blank token short-circuits`() = runBlocking {
        val result = repo.registerDeviceToken("")
        assertTrue(result is DeviceTokenRegisterResult.ServerError)
        assertEquals(0, server.requestCount)
    }

    // -- unregisterDeviceToken --

    @Test
    fun `unregister url-encodes the token`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true,"message":"ok"}"""))
        val result = repo.unregisterDeviceToken("abc/def+ghi=")
        assertTrue(result.isSuccess)
        val req: RecordedRequest = server.takeRequest()
        assertEquals("DELETE", req.method)
        // Verify the path is url-encoded
        assertTrue(
            "expected encoded slash/plus/equals in path, got ${req.path}",
            req.path!!.endsWith("abc%2Fdef%2Bghi%3D"),
        )
    }

    // -- fetchNotifications --

    @Test
    fun `fetchNotifications returns paged domain notifications`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"data":{
                  "notifications":[
                    {"id":1,"title":"a","body":"b","type":"chat_message","sent_at":"x","is_read":false,"created_at":"x"},
                    {"id":2,"title":"c","body":"d","type":"future_unknown","sent_at":"y","is_read":true,"created_at":"y"}
                  ],
                  "pagination":{"current_page":1,"last_page":3,"per_page":15,"total":40}
                }}
                """.trimIndent(),
            ),
        )
        val page = repo.fetchNotifications(page = 1, role = "carrier").getOrThrow()
        assertEquals(2, page.notifications.size)
        assertEquals(NotificationType.CHAT_MESSAGE, page.notifications[0].type)
        assertEquals(NotificationType.UNKNOWN, page.notifications[1].type)
        assertTrue(page.hasMore)
        assertEquals(40, page.total)
        // Query param wiring
        val req = server.takeRequest()
        assertTrue("path should include role=carrier", req.path!!.contains("role=carrier"))
    }

    // -- getUnreadCount --

    @Test
    fun `getUnreadCount parses canonical shape`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"unread_count":11}}""",
            ),
        )
        val count = repo.getUnreadCount().getOrThrow()
        assertEquals(11, count)
    }

    @Test
    fun `getUnreadCount parses bare number`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("9"))
        val count = repo.getUnreadCount().getOrThrow()
        assertEquals(9, count)
    }

    @Test
    fun `getUnreadCount sends role query param`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("3"))
        repo.getUnreadCount(role = "shipper").getOrThrow()
        val req = server.takeRequest()
        assertTrue(req.path!!.contains("role=shipper"))
    }

    // -- markAsRead + markAllAsRead --

    @Test
    fun `markAsRead returns success`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"notification_id":42,"read_at":"x"}}""",
            ),
        )
        val result = repo.markAsRead(42)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `markAllAsRead prefers marked_count`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"marked_count":7,"updated_count":99}}""",
            ),
        )
        val count = repo.markAllAsRead().getOrThrow()
        assertEquals(7, count)
    }

    @Test
    fun `markAllAsRead falls back to updated_count`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":{"updated_count":4}}""",
            ),
        )
        val count = repo.markAllAsRead().getOrThrow()
        assertEquals(4, count)
    }

    // -- sendTestNotification --

    @Test
    fun `sendTestNotification returns success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true,"message":"sent"}"""))
        val result = repo.sendTestNotification()
        assertTrue(result.isSuccess)
    }

    // -- Failure paths --

    @Test
    fun `fetchNotifications returns failure on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"server down"}"""))
        val result = repo.fetchNotifications()
        assertTrue(result.isFailure)
    }
}

/** Test double — does not touch SharedPreferences. */
private class FakeConsentIntent : PushNotificationsConsentIntent {
    private var optedIn = false
    override fun isOptedIn(): Boolean = optedIn
    override fun setOptedIn(value: Boolean) { optedIn = value }
}
