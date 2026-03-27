package com.efthemiosprime.pasabayan.core.network

/**
 * Supplies the bearer token for authenticated requests.
 * Phase 1 will back this with secure storage; default is no token.
 */
fun interface AuthTokenProvider {
    fun currentToken(): String?
}
