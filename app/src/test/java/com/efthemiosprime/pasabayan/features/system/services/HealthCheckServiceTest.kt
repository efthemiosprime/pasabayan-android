package com.efthemiosprime.pasabayan.features.system.services

import com.efthemiosprime.pasabayan.core.network.system.SystemApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class HealthCheckServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: DefaultHealthCheckService

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
            .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))
            .build()
        service = DefaultHealthCheckService(retrofit.create(SystemApi::class.java))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `isReachable returns true for success body`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true,"status":"ok"}"""))
        assertTrue(service.isReachable().getOrThrow())
    }

    @Test
    fun `isReachable returns true when only status is ok`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"ok"}"""))
        assertTrue(service.isReachable().getOrThrow())
    }

    @Test
    fun `isReachable returns false when success is false and status missing`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":false}"""))
        assertFalse(service.isReachable().getOrThrow())
    }

    @Test
    fun `isReachable returns false on non-2xx response`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody(""))
        assertFalse(service.isReachable().getOrThrow())
    }
}
