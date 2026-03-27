package com.efthemiosprime.pasabayan.core.network

/**
 * Invoked when a response is **401 Unauthorized** so the app can clear stored credentials
 * (parity with iOS `AuthService.removeToken()` on 401).
 */
fun interface SessionInvalidationHandler {
    fun onUnauthorized()
}
