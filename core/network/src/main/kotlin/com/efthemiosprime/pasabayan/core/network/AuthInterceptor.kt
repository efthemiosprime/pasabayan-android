package com.efthemiosprime.pasabayan.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches `Authorization: Bearer` when a token exists, per iOS APIService:
 * paths under `/auth/` **except** `/auth/me` are called **without** Bearer.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val token = tokenProvider.currentToken()
        if (token.isNullOrBlank() || !shouldAttachBearer(path)) {
            return chain.proceed(request)
        }
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build(),
        )
    }

    companion object {
        fun shouldAttachBearer(encodedPath: String): Boolean {
            if (!encodedPath.contains("/auth/")) return true
            return encodedPath.contains("/auth/me")
        }
    }
}
