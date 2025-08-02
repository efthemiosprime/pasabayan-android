package com.efthemiosprime.pasabayan.data.repository

import android.content.Context
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.efthemiosprime.pasabayan.domain.repository.DeliveryMatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * DeliveryMatch Repository Implementation
 * Exactly matching iOS DeliveryMatch functionality
 * Simple matching without complex bidding system
 */
class DeliveryMatchRepositoryImpl(
    private val apiService: APIService
) : DeliveryMatchRepository {
    
    override suspend fun createMatch(request: MatchCreationRequest): Result<DeliveryMatch> = try {
        val response = apiService.createMatch(request)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to create match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun confirmMatch(matchId: Int): Result<DeliveryMatch> = try {
        val response = apiService.confirmMatch(matchId)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to confirm match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun pickupMatch(matchId: Int, updateRequest: MatchUpdateRequest?): Result<DeliveryMatch> = try {
        val response = apiService.pickupMatch(matchId, updateRequest)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to pickup match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun transitMatch(matchId: Int): Result<DeliveryMatch> = try {
        val response = apiService.transitMatch(matchId)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to transit match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun deliverMatch(matchId: Int, updateRequest: MatchUpdateRequest?): Result<DeliveryMatch> = try {
        val response = apiService.deliverMatch(matchId, updateRequest)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to deliver match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun cancelMatch(matchId: Int): Result<Unit> = try {
        apiService.cancelMatch(matchId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to cancel match: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun getCompatiblePackages(tripId: Int): Result<PaginatedResponse<PackageRequest>> = try {
        val response = apiService.getCompatiblePackages(tripId)
        // Convert PackageRequestApiData to PackageRequest
        val convertedData = PaginatedResponse<PackageRequest>(
            currentPage = response.data.currentPage,
            data = response.data.data.map { it.toPackageRequest() },
            firstPageUrl = response.data.firstPageUrl,
            from = response.data.from,
            lastPage = response.data.lastPage,
            lastPageUrl = response.data.lastPageUrl,
            links = response.data.links,
            nextPageUrl = response.data.nextPageUrl,
            path = response.data.path,
            perPage = response.data.perPage,
            prevPageUrl = response.data.prevPageUrl,
            to = response.data.to,
            total = response.data.total
        )
        Result.Success(convertedData)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to get compatible packages: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun getCarrierMatches(): Result<PaginatedResponse<DeliveryMatch>> = try {
        val response = apiService.getCarrierMatches()
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to get carrier matches: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun getShipperMatches(): Result<PaginatedResponse<DeliveryMatch>> = try {
        val response = apiService.getShipperMatches()
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to get shipper matches: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun getAllMatches(): Result<PaginatedResponse<DeliveryMatch>> = try {
        val response = apiService.getAllMatches()
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to get all matches: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun acceptPackageRequest(tripId: Int, packageId: Int, agreedPrice: Double): Result<DeliveryMatch> = try {
        val request = PackageAcceptRequest(agreedPrice)
        val response = apiService.acceptPackageRequest(packageId, request)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to accept package request: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun rejectPackageRequest(tripId: Int, packageId: Int, reason: String?): Result<PackageRejectionResponse> = try {
        val request = PackageRejectRequest(reason)
        val response = apiService.rejectPackageRequest(packageId, request)
        // Convert PackageRejectResponse to PackageRejectionResponse
        val rejectionResponse = PackageRejectionResponse(
            success = true,
            message = response.message
        )
        Result.Success(rejectionResponse)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to reject package request: ${e.message ?: "Unknown error"}"))
    }
    
    override suspend fun requestToCarryPackage(packageId: Int, tripId: Int, proposedPrice: Double, message: String?): Result<DeliveryMatch> = try {
        val request = RequestToCarryRequest(
            tripId = tripId,
            proposedPrice = proposedPrice,
            message = message
        )
        val response = apiService.requestToCarryPackage(packageId, request)
        Result.Success(response.data)
    } catch (e: Exception) {
        Result.Failure(AppError.UnknownError("Failed to request to carry package: ${e.message ?: "Unknown error"}"))
    }
    
    override fun getCarrierMatchesFlow(): Flow<Result<PaginatedResponse<DeliveryMatch>>> = flow {
        try {
            val matches = getCarrierMatches()
            emit(matches)
        } catch (e: Exception) {
            emit(Result.Failure(AppError.UnknownError("Failed to load carrier matches: ${e.message ?: "Unknown error"}")))
        }
    }
    
    override fun getShipperMatchesFlow(): Flow<Result<PaginatedResponse<DeliveryMatch>>> = flow {
        try {
            val matches = getShipperMatches()
            emit(matches)
        } catch (e: Exception) {
            emit(Result.Failure(AppError.UnknownError("Failed to load shipper matches: ${e.message ?: "Unknown error"}")))
        }
    }
    
    companion object {
        fun create(context: Context): DeliveryMatchRepositoryImpl {
            val authService = AuthService.getInstance(context)
            val apiService = authService.createApiService()
            return DeliveryMatchRepositoryImpl(apiService)
        }
    }
} 