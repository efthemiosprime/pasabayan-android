package com.efthemiosprime.pasabayan.domain.repository

import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.CompatibilityResult

/**
 * Package Repository Interface - matching existing repository pattern
 * Follows the same structure as AuthRepository and TripRepository
 */
interface PackageRepository {
    /**
     * Get all package requests for the current user
     */
    suspend fun getPackageRequests(): Result<List<PackageRequest>>
    
    /**
     * Get available package requests that need carriers (for shipper browse functionality)
     * Mirrors iOS getAvailablePackages functionality
     */
    suspend fun getAvailablePackages(
        page: Int = 1,
        origin: String? = null,
        destination: String? = null,
        maxWeight: Double? = null,
        urgency: String? = null,
        minBudget: Double? = null,
        maxBudget: Double? = null,
        fragile: Boolean? = null,
        pickupDateFrom: String? = null,
        pickupDateTo: String? = null
    ): Result<List<PackageRequest>>
    
    /**
     * Create a new package request
     */
    suspend fun createPackageRequest(request: CreatePackageRequest): Result<PackageRequest>
    
    /**
     * Get a specific package request by ID
     */
    suspend fun getPackageRequest(packageId: Int): Result<PackageRequest>
    
    /**
     * Update an existing package request
     */
    suspend fun updatePackageRequest(packageId: Int, request: CreatePackageRequest): Result<PackageRequest>
    
    /**
     * Delete a package request
     */
    suspend fun deletePackageRequest(packageId: Int): Result<Unit>
    
    /**
     * Get compatible trips for a package request
     */
    suspend fun getCompatibleTrips(packageId: Int): Result<List<CompatibleTrip>>
    
    /**
     * Cancel a package request (changes status to cancelled)
     * Matches iOS APIService.cancelPackageRequest method
     */
    suspend fun cancelPackageRequest(packageId: Int): Result<Unit>
    
    /**
     * Shipper requests trip for package
     * Matches iOS APIService.sendShipperTripRequest method
     */
    suspend fun sendShipperTripRequest(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String
    ): Result<DeliveryMatch>
    
    /**
     * Check trip compatibility with package
     * GET /api/trips/{trip_id}/compatibility/{package_id}
     */
    suspend fun checkTripCompatibility(
        tripId: Int,
        packageId: Int
    ): Result<CompatibilityResult>
    
    /**
     * Accept a package request for a specific trip (Create Match)
     * POST /api/trips/{trip_id}/packages/{package_id}/accept
     */
    suspend fun acceptPackageForTrip(
        tripId: Int,
        packageId: Int,
        agreedPrice: Double
    ): Result<DeliveryMatch>
} 