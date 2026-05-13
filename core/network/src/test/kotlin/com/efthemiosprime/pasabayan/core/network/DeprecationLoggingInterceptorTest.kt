package com.efthemiosprime.pasabayan.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeprecationLoggingInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var logger: RecordingLogger
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        logger = RecordingLogger()
        client = OkHttpClient.Builder()
            .addInterceptor(DeprecationLoggingInterceptor(logger))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `logs warning when response carries Deprecation true`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Deprecation", "true")
                .setHeader("Link", "</api/trips/1/packages/2/request>; rel=\"successor-version\"")
                .setBody("{}"),
        )

        client.newCall(Request.Builder().url(server.url("/legacy/path")).build())
            .execute()
            .close()

        assertEquals(1, logger.messages.size)
        val msg = logger.messages.single()
        assertTrue("expected message to include path, got: $msg", msg.contains("/legacy/path"))
        assertTrue("expected successor link in message, got: $msg", msg.contains("successor-version"))
    }

    @Test
    fun `does not log when Deprecation header is absent`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        client.newCall(Request.Builder().url(server.url("/normal/path")).build())
            .execute()
            .close()

        assertEquals(emptyList<String>(), logger.messages)
    }

    @Test
    fun `does not log when Deprecation header is not true`() {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Deprecation", "false").setBody("{}"))

        client.newCall(Request.Builder().url(server.url("/normal/path")).build())
            .execute()
            .close()

        assertEquals(emptyList<String>(), logger.messages)
    }

    private class RecordingLogger : DeprecationLogger {
        val messages = mutableListOf<String>()
        override fun warn(message: String) {
            messages += message
        }
    }
}
