package com.efthemiosprime.pasabayan.data.service

import retrofit2.http.*
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.LoginRequest
import com.efthemiosprime.pasabayan.data.model.CarrierStatusResponse
import com.efthemiosprime.pasabayan.data.model.UserDataResponse
import com.efthemiosprime.pasabayan.data.model.CarrierProfileResponse
import com.efthemiosprime.pasabayan.data.model.CarrierStatsResponse
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.data.model.PackageRequestResponse
import com.efthemiosprime.pasabayan.data.model.PackageRequestsResponse
import com.efthemiosprime.pasabayan.data.model.CompatibleTripsResponse
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequestApi
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripResponse
import com.efthemiosprime.pasabayan.data.model.TripsResponse
import com.efthemiosprime.pasabayan.data.model.CreateTripRequestApi

/**
 * API Service interface for authentication endpoints
 * Mirrors iOS APIService authentication methods
 * Base URL: https://api.pasabayan.com/api (same as iOS)
 */
interface APIService {
    
    /**
     * Login with OAuth provider (Google, Apple, Facebook)
     * Matches iOS APIService.loginWithToken method
     * iOS endpoint: /api/auth/{provider}/login
     */
    @POST("api/auth/{provider}/login")
    suspend fun loginWithToken(
        @Path("provider") provider: String,
        @Body request: LoginRequest
    ): AuthResponse
    
    /**
     * Get current authenticated user
     * iOS endpoint: /api/auth/me
     */
    @GET("api/auth/me")
    suspend fun getCurrentUser(): UserDataResponse
    
    /**
     * Logout current session
     * iOS endpoint: /api/auth/logout
     */
    @POST("api/auth/logout")
    suspend fun logout(): Map<String, String>
    
    /**
     * Refresh authentication token
     * iOS endpoint: /api/auth/refresh (if needed)
     */
    @POST("api/auth/refresh")
    suspend fun refreshToken(): AuthResponse
    
    /**
     * Get current user profile
     * iOS endpoint: /api/profile
     */
    @GET("api/profile")
    suspend fun getUserProfile(): AuthResponse
    
    /**
     * Toggle carrier status (activate/deactivate)
     * Matches iOS APIService.toggleCarrierStatus method
     * iOS endpoint: /api/carrier/toggle-status
     * Returns carrier status data with is_active_carrier boolean
     */
    @POST("api/carrier/toggle-status")
    suspend fun toggleCarrierStatus(): CarrierStatusResponse
    
    /**
     * Get carrier profile to check if user is already a carrier
     * Returns 200 if user is a carrier, 403 if not
     * iOS endpoint: /api/carrier/profile
     */
    @GET("api/carrier/profile")
    suspend fun getCarrierProfile(): CarrierProfileResponse
    
    /**
     * Get carrier statistics
     * Returns carrier performance metrics, earnings, and ratings
     * iOS endpoint: /api/carrier/stats
     */
    @GET("api/carrier/stats")
    suspend fun getCarrierStats(): CarrierStatsResponse
    
    /**
     * Package Request Management - matching iOS endpoints
     */
    @GET("api/packages")
    suspend fun getPackageRequests(): PackageRequestsResponse
    
    @POST("api/packages")
    suspend fun createPackageRequest(
        @Body request: CreatePackageRequestApi
    ): PackageRequestResponse
    
    @GET("api/packages/{id}")
    suspend fun getPackageRequest(
        @Path("id") packageId: Int
    ): PackageRequestResponse
    
    @PUT("api/packages/{id}")
    suspend fun updatePackageRequest(
        @Path("id") packageId: Int,
        @Body request: CreatePackageRequestApi
    ): PackageRequestResponse
    
    @DELETE("api/packages/{id}")
    suspend fun deletePackageRequest(
        @Path("id") packageId: Int
    ): Map<String, String>
    
    @GET("api/packages/{id}/compatible-trips")
    suspend fun getCompatibleTrips(
        @Path("id") packageId: Int
    ): CompatibleTripsResponse
    
    /**
     * Trip Management - matching iOS endpoints
     */
    @GET("api/trips")
    suspend fun getTrips(): TripsResponse
    
    @POST("api/trips")
    suspend fun createTrip(
        @Body request: CreateTripRequestApi
    ): TripResponse
    
    @GET("api/trips/{id}")
    suspend fun getTrip(
        @Path("id") tripId: Int
    ): TripResponse
    
    @PUT("api/trips/{id}")
    suspend fun updateTrip(
        @Path("id") tripId: Int,
        @Body request: CreateTripRequestApi
    ): TripResponse
    
    @DELETE("api/trips/{id}")
    suspend fun deleteTrip(
        @Path("id") tripId: Int
    ): Map<String, String>
    
    @GET("api/trips")
    suspend fun getCarrierTrips(
        @Query("carrier_id") carrierId: Int
    ): TripsResponse
    
    @GET("api/trips/available")
    suspend fun getAvailableTrips(
        @Query("page") page: Int = 1
    ): TripsResponse
    
    companion object {
        // Base URL should be https://api.pasabayan.com since endpoints already include /api
        const val BASE_URL = "https://api.pasabayan.com"
        const val BASE_URL_LOCAL = "http://10.0.2.2:8000"
    }
} 