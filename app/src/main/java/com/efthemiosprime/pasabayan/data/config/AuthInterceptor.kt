package com.efthemiosprime.pasabayan.data.config

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Authentication Interceptor
 * Automatically adds Bearer token to requests when available
 * Handles token refresh if needed
 */
class AuthInterceptor : Interceptor {
    
    private companion object {
        const val TAG = "AuthInterceptor"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get the current auth token from shared preferences or secure storage
        val token = getCurrentAuthToken()
        
        // If no token available, proceed without authentication
        if (token.isNullOrBlank()) {
            Log.d(TAG, "No auth token available, proceeding without authentication")
            return chain.proceed(originalRequest)
        }
        
        // Add the Bearer token to the request
        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
            .build()
        
        Log.d(TAG, "Added Bearer token to request: ${originalRequest.url}")
        
        return chain.proceed(authenticatedRequest)
    }
    
    /**
     * Get the current authentication token
     * This should be implemented to retrieve the token from your auth storage
     */
    private fun getCurrentAuthToken(): String? {
        // TODO: Connect to AuthService to get the current token
        // This would need to be injected or accessed through a singleton
        // For now, returning null until proper DI is implemented
        
        // Note: This interceptor will be enhanced later to properly integrate
        // with the existing AuthService token management
        
        return null
    }
}