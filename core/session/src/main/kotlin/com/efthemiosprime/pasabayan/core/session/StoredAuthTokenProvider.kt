package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoredAuthTokenProvider @Inject constructor(
    private val tokenStore: TokenStore,
) : AuthTokenProvider {
    override fun currentToken(): String? = tokenStore.getToken()
}
