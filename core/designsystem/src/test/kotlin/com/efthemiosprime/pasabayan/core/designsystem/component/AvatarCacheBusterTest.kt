package com.efthemiosprime.pasabayan.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure-logic coverage for [cacheBustedAvatarUrl]. iOS parity:
 * `UserProfileAvatar.cacheBustedURL` in `UserProfileHeader.swift`.
 */
class AvatarCacheBusterTest {

    @Test
    fun `null URL returns null regardless of cacheBuster`() {
        assertNull(cacheBustedAvatarUrl(url = null, cacheBuster = "xyz"))
        assertNull(cacheBustedAvatarUrl(url = null, cacheBuster = null))
    }

    @Test
    fun `blank URL returns null`() {
        assertNull(cacheBustedAvatarUrl(url = "", cacheBuster = "xyz"))
        assertNull(cacheBustedAvatarUrl(url = "   ", cacheBuster = "xyz"))
    }

    @Test
    fun `null or blank cacheBuster passes the URL through unchanged`() {
        val url = "https://cdn.example.com/avatars/42.jpg"
        assertEquals(url, cacheBustedAvatarUrl(url = url, cacheBuster = null))
        assertEquals(url, cacheBustedAvatarUrl(url = url, cacheBuster = ""))
    }

    @Test
    fun `appends cb query parameter when URL has no existing query`() {
        val url = "https://cdn.example.com/avatars/42.jpg"
        val busted = cacheBustedAvatarUrl(url = url, cacheBuster = "abc123")
        assertEquals("https://cdn.example.com/avatars/42.jpg?cb=abc123", busted)
    }

    @Test
    fun `appends cb with ampersand when URL already has a query string`() {
        val url = "https://cdn.example.com/avatars/42.jpg?v=2"
        val busted = cacheBustedAvatarUrl(url = url, cacheBuster = "abc123")
        assertEquals("https://cdn.example.com/avatars/42.jpg?v=2&cb=abc123", busted)
    }
}
