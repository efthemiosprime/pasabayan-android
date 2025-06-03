package com.efthemiosprime.pasabayan.data.service

import retrofit2.http.*
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.LoginRequest
import com.efthemiosprime.pasabayan.data.model.CarrierStatusResponse
import com.efthemiosprime.pasabayan.data.model.UserDataResponse
import com.efthemiosprime.pasabayan.data.model.CarrierProfileResponse

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
    suspend fun getCurrentUser(): UserDataResponse
    
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
    
    /**
     * Toggle carrier status (activate/deactivate)
     * Matches iOS APIService.toggleCarrierStatus method
     * iOS endpoint: /carrier/toggle-status
     * Returns carrier status data with is_active_carrier boolean
     */
    @POST("carrier/toggle-status")
    suspend fun toggleCarrierStatus(): CarrierStatusResponse
    
    /**
     * Get carrier profile to check if user is already a carrier
     * Returns 200 if user is a carrier, 403 if not
     * iOS endpoint: /carrier/profile
     */
    @GET("carrier/profile")
    suspend fun getCarrierProfile(): CarrierProfileResponse
    
    companion object {
        // Same base URL as iOS: https://api.pasabayan.com/api (no trailing slash)
        const val BASE_URL = "https://api.pasabayan.com/api"
    }
} 