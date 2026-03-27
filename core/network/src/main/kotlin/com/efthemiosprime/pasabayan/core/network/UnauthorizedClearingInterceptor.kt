package com.efthemiosprime.pasabayan.core.network

import okhttp3.Interceptor
import javax.inject.Inject

/**
 * Clears the session when the server returns 401 (after the response is received).
 */
class UnauthorizedClearingInterceptor @Inject constructor(
    private val handler: SessionInvalidationHandler,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            handler.onUnauthorized()
        }
        return response
    }
}
