package com.efthemiosprime.pasabayan.data.service

import retrofit2.http.*
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.LoginRequest

/**
 * API Service interface for authentication endpoints
 * Mirrors iOS APIService authentication methods
 * Base URL: https://api.pasabayan.com/api (same as iOS)
 */
interface APIService {
    
    /**
     * Login with OAuth provider (Google, Apple, Facebook)
     * Matches iOS APIService.loginWithToken method
     * iOS endpoint: /auth/{provider}/login
     */
    @POST("auth/{provider}/login")
    suspend fun loginWithToken(
        @Path("provider") provider: String,
        @Body request: LoginRequest
    ): AuthResponse
    
    /**
     * Get current authenticated user
     * iOS endpoint: /auth/me
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): AuthResponse
    
    /**
     * Logout current session
     * iOS endpoint: /auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(): Map<String, String>
    
    /**
     * Refresh authentication token
     * iOS endpoint: /auth/refresh (if needed)
     */
    @POST("auth/refresh")
    suspend fun refreshToken(): AuthResponse
    
    /**
     * Get current user profile
     * iOS endpoint: /profile
     */
    @GET("profile")
    suspend fun getUserProfile(): AuthResponse
    
    companion object {
        // Same base URL as iOS: https://api.pasabayan.com/api
        const val BASE_URL = "https://api.pasabayan.com/api/"
    }
} 