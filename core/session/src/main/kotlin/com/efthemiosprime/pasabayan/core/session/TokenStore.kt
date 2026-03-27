package com.efthemiosprime.pasabayan.core.session

/**
 * Persisted bearer token (parity with iOS Keychain session).
 */
interface TokenStore {
    fun getToken(): String?
    fun setToken(token: String?)
    fun clear()
}
