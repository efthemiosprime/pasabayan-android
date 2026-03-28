package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.network.SessionInvalidationHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenClearingHandler @Inject constructor(
    private val tokenStore: TokenStore,
    private val unauthorizedSessionNotifier: UnauthorizedSessionNotifier,
) : SessionInvalidationHandler {
    override fun onUnauthorized() {
        tokenStore.clear()
        unauthorizedSessionNotifier.notifyUnauthorized()
    }
}
