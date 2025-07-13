package com.efthemiosprime.pasabayan.data.service

import retrofit2.http.*
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.LoginRequest
import com.efthemiosprime.pasabayan.data.model.CarrierStatusResponse
import com.efthemiosprime.pasabayan.data.model.UserDataResponse
import com.efthemiosprime.pasabayan.data.model.CarrierProfileResponse
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.data.model.PackageRequestResponse
import com.efthemiosprime.pasabayan.data.model.PackageRequestsResponse
import com.efthemiosprime.pasabayan.data.model.CompatibleTripsResponse
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequestApi

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
    
    /**
     * Package Request Management - matching iOS endpoints
     */
    @GET("packages")
    suspend fun getPackageRequests(): PackageRequestsResponse
    
    @POST("packages")
    suspend fun createPackageRequest(
        @Body request: CreatePackageRequestApi
    ): PackageRequestResponse
    
    @GET("packages/{id}")
    suspend fun getPackageRequest(
        @Path("id") packageId: Int
    ): PackageRequestResponse
    
    @PUT("packages/{id}")
    suspend fun updatePackageRequest(
        @Path("id") packageId: Int,
        @Body request: CreatePackageRequestApi
    ): PackageRequestResponse
    
    @DELETE("packages/{id}")
    suspend fun deletePackageRequest(
        @Path("id") packageId: Int
    ): Map<String, String>
    
    @GET("packages/{id}/compatible-trips")
    suspend fun getCompatibleTrips(
        @Path("id") packageId: Int
    ): CompatibleTripsResponse
    
    companion object {
        // Same base URL as iOS: https://api.pasabayan.com/api (no trailing slash)
        const val BASE_URL = "https://api.pasabayan.com/api"
        const val BASE_URL_LOCAL = "http://10.0.2.2:8000/api"
    }
} 