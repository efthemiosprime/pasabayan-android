package com.efthemiosprime.pasabayan.core.network.auth

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden JSON decode tests — parity with [android-spec/API-SHAPES-REFERENCE.md] auth rows
 * and Retrofit [com.efthemiosprime.pasabayan.core.network.di.NetworkModule] Json config.
 */
class AuthJsonModelsDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `POST auth provider login decodes BackendAuthResponseJson`() {
        val raw = fixture("provider_login_success.json")
        val decoded = json.decodeFromString<BackendAuthResponseJson>(raw)
        assertTrue(decoded.success)
        assertEquals("Authenticated", decoded.message)
        val data = requireNotNull(decoded.data)
        assertEquals("jwt-access-token-abc", data.token)
        assertEquals("Bearer", data.tokenType)
        val user = data.user
        assertEquals(42L, user.id)
        assertEquals("Test User", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("https://cdn.example.com/a.png", user.avatar)
        assertEquals("+15551234567", user.phone)
        assertEquals(true, user.phoneVerified)
        assertEquals(true, user.profileCompleted)
        assertEquals("google", user.provider)
        assertEquals(listOf("shipper", "carrier"), user.userTypes)
        assertEquals(false, user.isActiveCarrier)
        assertEquals(true, user.isActiveShipper)
    }

    @Test
    fun `GET auth me decodes UserResponseJson`() {
        val raw = fixture("auth_me_success.json")
        val decoded = json.decodeFromString<UserResponseJson>(raw)
        assertTrue(decoded.success)
        val user = decoded.data.user
        assertEquals(7L, user.id)
        assertEquals("Me User", user.name)
        assertEquals("me@example.com", user.email)
        assertNull(user.avatar)
        assertNull(user.phone)
    }

    @Test
    fun `POST auth logout decodes LogoutResponseJson`() {
        val raw = fixture("logout_success.json")
        val decoded = json.decodeFromString<LogoutResponseJson>(raw)
        assertEquals(true, decoded.success)
        assertEquals("Successfully logged out", decoded.message)
    }

    @Test
    fun `ProviderLoginRequestJson encodes snake_case access_token`() {
        val body = ProviderLoginRequestJson(accessToken = "id-token-xyz")
        val encoded = json.encodeToString(ProviderLoginRequestJson.serializer(), body)
        assertTrue(encoded.contains("\"access_token\":\"id-token-xyz\""))
    }

    private fun fixture(name: String): String =
        javaClass.getResourceAsStream("/api-fixtures/auth/$name")!!.bufferedReader().use { it.readText() }
}
