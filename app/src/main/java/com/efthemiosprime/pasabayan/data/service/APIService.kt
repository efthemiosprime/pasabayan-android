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
import com.efthemiosprime.pasabayan.data.model.AvailablePackagesResponse
import com.efthemiosprime.pasabayan.data.model.CompatibleTripsResponse
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequestApi
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripResponse
import com.efthemiosprime.pasabayan.data.model.TripsResponse
import com.efthemiosprime.pasabayan.data.model.CreateTripRequestApi
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchCreationRequest
import com.efthemiosprime.pasabayan.data.model.MatchUpdateRequest
import com.efthemiosprime.pasabayan.data.model.DataResponse
import com.efthemiosprime.pasabayan.data.model.EmptyResponse
import com.efthemiosprime.pasabayan.data.model.PackageAcceptRequest
import com.efthemiosprime.pasabayan.data.model.AcceptPackageRequest
import com.efthemiosprime.pasabayan.data.model.AcceptPackageResponse
import com.efthemiosprime.pasabayan.data.model.PackageAcceptResponse
import com.efthemiosprime.pasabayan.data.model.PackageRejectRequest
import com.efthemiosprime.pasabayan.data.model.PackageRejectResponse
import com.efthemiosprime.pasabayan.data.model.RequestToCarryRequest
import com.efthemiosprime.pasabayan.data.model.DirectBookingRequest
import com.efthemiosprime.pasabayan.data.model.DirectBookingResponse
import com.efthemiosprime.pasabayan.data.model.MatchesResponse
import com.efthemiosprime.pasabayan.data.model.ShipperTripRequest
import com.efthemiosprime.pasabayan.data.model.CompatibilityResult
import com.efthemiosprime.pasabayan.data.model.ShipperAcceptRequest
import com.efthemiosprime.pasabayan.data.model.ShipperDeclineRequest
import com.efthemiosprime.pasabayan.data.model.CarrierAcceptRequest
import com.efthemiosprime.pasabayan.data.model.CarrierDeclineRequest
import com.efthemiosprime.pasabayan.data.model.ShipperMatchResponse
import com.efthemiosprime.pasabayan.data.model.CarrierResponseResult
import com.efthemiosprime.pasabayan.data.model.PhoneVerificationStatus


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
     * Get phone verification status
     * iOS endpoint: /api/profile/phone-verification-status (assumed)
     * Returns current user's phone verification status
     */
    @GET("api/profile/phone-verification-status")
    suspend fun getPhoneVerificationStatus(): DataResponse<PhoneVerificationStatus>
    
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
    
    @GET("api/packages/available")
    suspend fun getAvailablePackages(
        @Query("page") page: Int = 1,
        @Query("origin") origin: String? = null,
        @Query("destination") destination: String? = null,
        @Query("max_weight") maxWeight: Double? = null,
        @Query("urgency") urgency: String? = null,
        @Query("min_budget") minBudget: Double? = null,
        @Query("max_budget") maxBudget: Double? = null,
        @Query("fragile") fragile: Boolean? = null,
        @Query("pickup_date_from") pickupDateFrom: String? = null,
        @Query("pickup_date_to") pickupDateTo: String? = null
    ): AvailablePackagesResponse
    
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
    
    /**
     * Cancel package request (changes status to cancelled)
     * Matches iOS APIService.cancelPackageRequest method
     * iOS endpoint: PUT /api/packages/{id} with {"request_status": "cancelled"}
     */
    @PUT("api/packages/{id}")
    suspend fun cancelPackageRequest(
        @Path("id") packageId: Int,
        @Body request: Map<String, String>
    ): EmptyResponse
    
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
    
    /**
     * Delivery Match Management - matching iOS delivery match functionality
     */
    @POST("api/matches")
    suspend fun createMatch(
        @Body request: MatchCreationRequest
    ): DataResponse<DeliveryMatch>
    
    @POST("api/matches/{id}/confirm")
    suspend fun confirmMatch(
        @Path("id") matchId: Int
    ): DataResponse<DeliveryMatch>
    
    @POST("api/matches/{id}/pickup")
    suspend fun pickupMatch(
        @Path("id") matchId: Int,
        @Body updateRequest: MatchUpdateRequest?
    ): DataResponse<DeliveryMatch>
    
    @POST("api/matches/{id}/transit")
    suspend fun transitMatch(
        @Path("id") matchId: Int
    ): DataResponse<DeliveryMatch>
    
    @POST("api/matches/{id}/deliver")
    suspend fun deliverMatch(
        @Path("id") matchId: Int,
        @Body updateRequest: MatchUpdateRequest?
    ): DataResponse<DeliveryMatch>
    
    @POST("api/matches/{id}/cancel")
    suspend fun cancelMatch(
        @Path("id") matchId: Int
    ): EmptyResponse
    
    @GET("api/trips/{id}/compatible-packages")
    suspend fun getCompatiblePackages(
        @Path("id") tripId: Int
    ): PackageRequestsResponse
    
    @GET("api/matches")
    suspend fun getCarrierMatches(
        @Query("role") role: String = "carrier",
        @Query("status") status: String? = null
    ): MatchesResponse
    
    @GET("api/matches")
    suspend fun getShipperMatches(
        @Query("role") role: String = "shipper", 
        @Query("status") status: String? = null
    ): MatchesResponse
    
    @GET("api/matches")
    suspend fun getAllMatches(
        @Query("status") status: String? = null
    ): MatchesResponse
    
    @POST("api/packages/{id}/accept")
    suspend fun acceptPackageRequest(
        @Path("id") packageId: Int,
        @Body request: PackageAcceptRequest
    ): PackageAcceptResponse
    
    @POST("api/packages/{id}/reject")
    suspend fun rejectPackageRequest(
        @Path("id") packageId: Int,
        @Body request: PackageRejectRequest
    ): PackageRejectResponse
    
    // Accept Package Request (Create Match) - New endpoint
    @POST("api/trips/{trip_id}/packages/{package_id}/accept")
    suspend fun acceptPackageForTrip(
        @Path("trip_id") tripId: Int,
        @Path("package_id") packageId: Int,
        @Body request: AcceptPackageRequest
    ): AcceptPackageResponse
    
    @POST("api/trips/{trip_id}/packages/{package_id}/request")
    suspend fun requestToCarryPackage(
        @Path("trip_id") tripId: Int,
        @Path("package_id") packageId: Int,
        @Body request: RequestToCarryRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * Shipper requests trip for package
     * Matches iOS APIService.sendShipperTripRequest method
     * iOS endpoint: POST /api/packages/{id}/request-trip/{trip_id}
     */
    @POST("api/packages/{package_id}/request-trip/{trip_id}")
    suspend fun sendShipperTripRequest(
        @Path("package_id") packageId: Int,
        @Path("trip_id") tripId: Int,
        @Body request: ShipperTripRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * MISSING ENDPOINTS - Essential iOS functionality
     */
    
    /**
     * Check trip compatibility with package
     * Matches iOS: GET /trips/{id}/compatibility/{package_id}
     */
    @GET("api/trips/{trip_id}/compatibility/{package_id}")
    suspend fun checkTripCompatibility(
        @Path("trip_id") tripId: Int,
        @Path("package_id") packageId: Int
    ): DataResponse<CompatibilityResult>
    
    /**
     * Carrier requests to carry package
     * Matches iOS: POST /packages/{id}/request-to-carry
     */
    @POST("api/packages/{package_id}/request-to-carry")
    suspend fun requestToCarryPackage(
        @Path("package_id") packageId: Int,
        @Body request: RequestToCarryRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * Get pending requests for carrier
     * Matches iOS: GET /matches/pending-requests
     */
    @GET("api/matches/pending-requests")
    suspend fun getCarrierPendingRequests(): MatchesResponse
    
    /**
     * SHIPPER MATCH RESPONSES - Accept/Decline Carrier Requests
     */
    
    /**
     * Shipper accepts carrier request
     * Matches iOS: PUT /matches/{id}/accept
     */
    @PUT("api/matches/{match_id}/accept")
    suspend fun acceptCarrierRequest(
        @Path("match_id") matchId: Int,
        @Body request: ShipperAcceptRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * Shipper declines carrier request  
     * Matches iOS: PUT /matches/{id}/decline
     */
    @PUT("api/matches/{match_id}/decline")
    suspend fun declineCarrierRequest(
        @Path("match_id") matchId: Int,
        @Body request: ShipperDeclineRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * CARRIER MATCH RESPONSES - Accept/Decline Shipper Requests
     */
    
    /**
     * Carrier accepts shipper request
     * Matches iOS: PUT /matches/{id}/accept-shipper-request
     */
    @PUT("api/matches/{match_id}/accept-shipper-request")
    suspend fun acceptShipperRequest(
        @Path("match_id") matchId: Int,
        @Body request: CarrierAcceptRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * Carrier declines shipper request
     * Matches iOS: PUT /matches/{id}/decline-shipper-request
     */
    @PUT("api/matches/{match_id}/decline-shipper-request")
    suspend fun declineShipperRequest(
        @Path("match_id") matchId: Int,
        @Body request: CarrierDeclineRequest
    ): DataResponse<DeliveryMatch>
    
    /**
     * FIX HTTP METHODS - Ensure iOS compatibility
     */
    
    @PUT("api/matches/{id}/confirm")
    suspend fun confirmMatchFixed(
        @Path("id") matchId: Int
    ): DataResponse<DeliveryMatch>
    
    @PUT("api/matches/{id}/pickup")
    suspend fun pickupMatchFixed(
        @Path("id") matchId: Int,
        @Body updateRequest: MatchUpdateRequest?
    ): DataResponse<DeliveryMatch>
    
    @PUT("api/matches/{id}/transit")
    suspend fun transitMatchFixed(
        @Path("id") matchId: Int
    ): DataResponse<DeliveryMatch>
    
    @PUT("api/matches/{id}/deliver")
    suspend fun deliverMatchFixed(
        @Path("id") matchId: Int,
        @Body updateRequest: MatchUpdateRequest?
    ): DataResponse<DeliveryMatch>
    
    @DELETE("api/matches/{id}")
    suspend fun cancelMatchFixed(
        @Path("id") matchId: Int
    ): EmptyResponse
    
    /**
     * Direct Trip Booking - matching iOS functionality
     */
    @POST("api/trips/{id}/book")
    suspend fun bookTripDirectly(
        @Path("id") tripId: String,
        @Body request: DirectBookingRequest
    ): DirectBookingResponse
    
    companion object {
        // URLs for different environments - mirrors iOS APIService
        const val PRODUCTION_URL = "https://api.pasabayan.com"
        const val LOCAL_URL = "http://localhost:8001"
        const val LOCAL_DEVICE_URL = "http://10.0.2.2:8001" // Android emulator localhost
        
        // Current base URL - switch between environments
        // Use production for auth, local for other APIs while debugging
        const val BASE_URL = PRODUCTION_URL // Temporary: use production until local auth is fixed
        // const val BASE_URL = LOCAL_DEVICE_URL // Use when local server auth is working
    }
} 