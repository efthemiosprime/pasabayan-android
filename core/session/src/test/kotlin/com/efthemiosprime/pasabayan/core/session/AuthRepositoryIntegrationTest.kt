package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.network.auth.AuthApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AuthRepositoryIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var json: Json
    private lateinit var authApi: AuthApi
    private lateinit var tokenStore: MemoryTokenStore

    private class MemoryTokenStore : TokenStore {
        private var token: String? = null
        override fun getToken(): String? = token
        override fun setToken(token: String?) {
            this.token = token
        }
        override fun clear() {
            token = null
        }
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        authApi = retrofit.create(AuthApi::class.java)
        tokenStore = MemoryTokenStore()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginWithProvider_success_storesTokenAndMapsUser() = runBlocking {
        val body = javaClass.getResourceAsStream("/api-fixtures/auth/provider_login_success.json")!!
            .bufferedReader().use { it.readText() }
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"),
        )
        val repo = AuthRepositoryImpl(authApi, json, tokenStore)
        val result = repo.loginWithProviderAccessToken("google", "id-token-xyz")

        val recorded = server.takeRequest()
        assertEquals("/api/auth/google/login", recorded.path)

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals(42L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("jwt-access-token-abc", tokenStore.getToken())
    }

    @Test
    fun loadCurrentUser_success() = runBlocking {
        val body = javaClass.getResourceAsStream("/api-fixtures/auth/auth_me_success.json")!!
            .bufferedReader().use { it.readText() }
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"),
        )
        val repo = AuthRepositoryImpl(authApi, json, tokenStore)
        val result = repo.loadCurrentUser()

        assertTrue(result.isSuccess)
        assertEquals(7L, result.getOrThrow().id)
        assertEquals("/api/auth/me", server.takeRequest().path)
    }

    @Test
    fun logout_success_clearsToken() = runBlocking {
        tokenStore.setToken("old")
        val body = """{"success":true,"message":"ok"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"),
        )
        val repo = AuthRepositoryImpl(authApi, json, tokenStore)
        val result = repo.logout()

        assertTrue(result.isSuccess)
        assertNull(tokenStore.getToken())
    }
}
