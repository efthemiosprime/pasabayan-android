package com.efthemiosprime.pasabayan.features.locations.services

import com.efthemiosprime.pasabayan.core.network.location.LocationCatalogApi
import com.efthemiosprime.pasabayan.features.locations.model.LocationCatalogSnapshot
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

class LocationCatalogRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var store: InMemoryStore
    private lateinit var repo: LocationCatalogRepositoryImpl
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))
            .build()
        store = InMemoryStore()
        repo = LocationCatalogRepositoryImpl(
            api = retrofit.create(LocationCatalogApi::class.java),
            store = store,
            json = json,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `refreshIfNeeded populates snapshot from API and persists`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"version":"v1","data":[
                  {"name":"Canada","iso2":"CA","iso3":"CAN","states":[
                    {"name":"Ontario","code":"ON","cities":[{"id":1,"name":"Toronto"}]}
                  ]}
                ]}
                """.trimIndent(),
            ),
        )
        val snapshot = repo.refreshIfNeeded().getOrThrow()
        assertEquals("v1", snapshot.version)
        assertEquals(1, snapshot.totalCityCount)
        assertTrue(repo.isLoaded)
        assertEquals(snapshot, store.saved)
    }

    @Test
    fun `refreshIfNeeded is in-memory cached once loaded`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"version":"v1","data":[]}""",
            ),
        )
        repo.refreshIfNeeded().getOrThrow()
        // No second response enqueued — the call must short-circuit.
        val again = repo.refreshIfNeeded().getOrThrow()
        assertEquals("v1", again.version)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `refreshIfNeeded skips persistence when version unchanged`() = runBlocking {
        store.preset = LocationCatalogSnapshot(
            version = "v1",
            cityData = mapOf("CA" to listOf("Toronto, ON")),
            aliasData = emptyMap(),
            metroAreas = emptyMap(),
            cityIds = mapOf("Toronto, ON" to 1),
        )
        // Rebuild repo so it picks up the preset snapshot from store.
        repo = LocationCatalogRepositoryImpl(
            api = repoApi(),
            store = store,
            json = json,
        )

        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"version":"v1","data":[
                  {"name":"Canada","iso2":"CA","iso3":"CAN","states":[
                    {"name":"Ontario","code":"ON","cities":[{"id":42,"name":"Toronto"}]}
                  ]}
                ]}""".trimIndent(),
            ),
        )
        val snapshot = repo.refreshIfNeeded(forceRefresh = true).getOrThrow()
        // Same version → existing snapshot is preserved, no save call.
        assertEquals(1, snapshot.cityIds["Toronto, ON"])
        assertEquals(0, store.saveCount)
    }

    @Test
    fun `refreshIfNeeded surfaces failure on 500`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"down"}"""))
        val result = repo.refreshIfNeeded()
        assertTrue(result.isFailure)
        assertEquals(false, repo.isLoaded)
    }

    private fun repoApi(): LocationCatalogApi = Retrofit.Builder()
        .baseUrl(server.url("/api/"))
        .client(OkHttpClient())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))
        .build()
        .create(LocationCatalogApi::class.java)
}

/** In-memory store double tracking save calls. */
private class InMemoryStore : LocationCatalogStore {
    var preset: LocationCatalogSnapshot? = null
    var saved: LocationCatalogSnapshot? = null
    var saveCount = 0

    override fun load(): LocationCatalogSnapshot? = preset
    override fun save(snapshot: LocationCatalogSnapshot) {
        saved = snapshot
        saveCount++
    }
    override fun clear() {
        preset = null
        saved = null
        saveCount = 0
    }
}
