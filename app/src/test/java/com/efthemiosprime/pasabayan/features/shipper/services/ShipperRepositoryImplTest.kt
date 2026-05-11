package com.efthemiosprime.pasabayan.features.shipper.services

import com.efthemiosprime.pasabayan.core.network.shipper.ShipperApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ShipperRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: ShipperRepositoryImpl

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
        repo = ShipperRepositoryImpl(retrofit.create(ShipperApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchNearbyCarriers returns mapped domain on object payload`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"message":"ok","data":{
                  "home_city_id":87,"home_city_name":"Toronto","radius_km":25.0,
                  "carriers":[{"id":1,"name":"Alex","avatar":null,"completed_deliveries":10,"distance_km":3.4}]
                }}
                """.trimIndent(),
            ),
        )
        val result = repo.fetchNearbyCarriers().getOrThrow()
        assertEquals(87, result.homeCityId)
        assertEquals("Toronto", result.homeCityName)
        assertTrue(result.isHomeCitySet)
        assertEquals(1, result.carriers.size)
        assertEquals("Alex", result.carriers.first().name)
    }

    @Test
    fun `fetchNearbyCarriers maps array payload to empty list with no home city`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"message":"no home city","data":[]}""",
            ),
        )
        val result = repo.fetchNearbyCarriers().getOrThrow()
        assertFalse(result.isHomeCitySet)
        assertNull(result.homeCityId)
        assertTrue(result.carriers.isEmpty())
    }

    @Test
    fun `fetchNearbyCarriers fails on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"down"}"""))
        val result = repo.fetchNearbyCarriers()
        assertTrue(result.isFailure)
    }
}
