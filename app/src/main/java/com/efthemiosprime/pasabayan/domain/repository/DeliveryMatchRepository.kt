package com.efthemiosprime.pasabayan.domain.repository

import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * DeliveryMatch Repository Interface
 * Exactly matching iOS DeliveryMatch functionality
 * Simple matching without complex bidding system
 */
interface DeliveryMatchRepository {
    
    /**
     * Create a match (iOS: createMatch)
     */
    suspend fun createMatch(request: MatchCreationRequest): Result<DeliveryMatch>
    
    /**
     * Confirm a match (iOS: confirmMatch)
     */
    suspend fun confirmMatch(matchId: Int): Result<DeliveryMatch>
    
    /**
     * Pickup a match (iOS: pickupMatch)
     */
    suspend fun pickupMatch(matchId: Int, updateRequest: MatchUpdateRequest? = null): Result<DeliveryMatch>
    
    /**
     * Transit a match (iOS: transitMatch)
     */
    suspend fun transitMatch(matchId: Int): Result<DeliveryMatch>
    
    /**
     * Deliver a match (iOS: deliverMatch)
     */
    suspend fun deliverMatch(matchId: Int, updateRequest: MatchUpdateRequest? = null): Result<DeliveryMatch>
    
    /**
     * Cancel a match (iOS: cancelMatch)
     */
    suspend fun cancelMatch(matchId: Int): Result<Unit>
    
    /**
     * Get compatible packages for a trip (iOS: getCompatiblePackages)
     */
    suspend fun getCompatiblePackages(tripId: Int): Result<PaginatedResponse<PackageRequest>>
    
    /**
     * Get carrier matches (iOS: getCarrierMatches)
     */
    suspend fun getCarrierMatches(): Result<PaginatedResponse<DeliveryMatch>>
    
    /**
     * Get shipper matches (iOS: getShipperMatches)
     */
    suspend fun getShipperMatches(): Result<PaginatedResponse<DeliveryMatch>>
    
    /**
     * Get all matches (iOS: getAllMatches)
     */
    suspend fun getAllMatches(): Result<PaginatedResponse<DeliveryMatch>>
    
    /**
     * Accept package request (iOS: acceptPackageRequest)
     */
    suspend fun acceptPackageRequest(tripId: Int, packageId: Int, agreedPrice: Double): Result<DeliveryMatch>
    
    /**
     * Reject package request (iOS: rejectPackageRequest)
     */
    suspend fun rejectPackageRequest(tripId: Int, packageId: Int, reason: String?): Result<PackageRejectionResponse>
    
    /**
     * Request to carry package (iOS: requestToCarryPackage)
     */
    suspend fun requestToCarryPackage(packageId: Int, tripId: Int, proposedPrice: Double, message: String?): Result<DeliveryMatch>
    
    /**
     * Reactive Flow for carrier matches
     */
    fun getCarrierMatchesFlow(): Flow<Result<PaginatedResponse<DeliveryMatch>>>
    
    /**
     * Reactive Flow for shipper matches
     */
    fun getShipperMatchesFlow(): Flow<Result<PaginatedResponse<DeliveryMatch>>>
    
    /**
     * Carrier accepts shipper request (iOS: acceptShipperRequest)
     */
    suspend fun acceptShipperRequest(matchId: Int, message: String): Result<DeliveryMatch>
    
    /**
     * Carrier declines shipper request (iOS: declineShipperRequest)
     */
    suspend fun declineShipperRequest(matchId: Int, reason: String, message: String): Result<DeliveryMatch>
    
    /**
     * Shipper accepts carrier request (iOS: acceptCarrierRequest)
     */
    suspend fun acceptCarrierRequest(matchId: Int, message: String?): Result<DeliveryMatch>
    
    /**
     * Shipper declines carrier request (iOS: declineCarrierRequest)
     */
    suspend fun declineCarrierRequest(matchId: Int, reason: String?, message: String?): Result<DeliveryMatch>
} 