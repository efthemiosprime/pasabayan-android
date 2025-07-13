package com.efthemiosprime.pasabayan.domain.repository

import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip

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
} 