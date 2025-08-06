package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.repository.DeliveryMatchRepositoryImpl
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.config.NetworkConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * MatchViewModel - Manages delivery match operations for shippers
 * Mirrors iOS MatchViewModel functionality with reactive state management
 * Follows Pasabayan functional programming patterns
 */
class MatchViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repository for match operations
    private val authService = AuthService.getInstance(application)
    private val apiService by lazy { createApiService() }
    private val matchRepository by lazy { DeliveryMatchRepositoryImpl(apiService) }
    
    // Internal state for shipper matches
    private val _shipperMatches = MutableStateFlow<List<DeliveryMatch>>(emptyList())
    val shipperMatches: StateFlow<List<DeliveryMatch>> = _shipperMatches.asStateFlow()
    
    // Internal state for carrier matches
    private val _carrierMatches = MutableStateFlow<List<DeliveryMatch>>(emptyList())
    val carrierMatches: StateFlow<List<DeliveryMatch>> = _carrierMatches.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val TAG = "MatchViewModel"
    
    init {
        // Load initial data
        loadShipperMatches()
    }
    
    /**
     * Load shipper matches - mirrors iOS getShipperMatches
     */
    fun loadShipperMatches() {
        Log.d(TAG, "🔄 Loading shipper matches...")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.getShipperMatches()
                result.fold(
                    onSuccess = { paginatedResponse ->
                        _shipperMatches.value = paginatedResponse.data
                        Log.d(TAG, "✅ Loaded ${paginatedResponse.data.size} shipper matches")
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to load matches"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to load shipper matches: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception loading shipper matches: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load carrier matches - mirrors iOS getCarrierMatches
     */
    fun loadCarrierMatches() {
        Log.d(TAG, "🔄 Loading carrier matches...")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.getCarrierMatches()
                result.fold(
                    onSuccess = { paginatedResponse ->
                        _carrierMatches.value = paginatedResponse.data
                        Log.d(TAG, "✅ Loaded ${paginatedResponse.data.size} carrier matches")
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to load carrier matches"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to load carrier matches: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception loading carrier matches: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cancel a match - mirrors iOS cancelMatch
     */
    fun cancelMatch(matchId: Int) {
        Log.d(TAG, "❌ Cancelling match ID: $matchId")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.cancelMatch(matchId)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ Match $matchId cancelled successfully")
                        // Remove cancelled match from local state
                        _shipperMatches.value = _shipperMatches.value.filter { it.id != matchId }
                        _carrierMatches.value = _carrierMatches.value.filter { it.id != matchId }
                        // Refresh matches to get updated state
                        loadShipperMatches()
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to cancel match"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to cancel match: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception cancelling match: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Confirm a match - mirrors iOS confirmMatch
     */
    fun confirmMatch(matchId: Int) {
        Log.d(TAG, "✅ Confirming match ID: $matchId")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.confirmMatch(matchId)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Match $matchId confirmed successfully")
                        // Update match in local state
                        updateMatchInState(updatedMatch)
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to confirm match"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to confirm match: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception confirming match: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mark match as picked up - mirrors iOS pickupMatch
     */
    fun pickupMatch(matchId: Int, confirmationCode: String? = null) {
        Log.d(TAG, "📦 Marking match $matchId as picked up")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val updateRequest = confirmationCode?.let {
                    com.efthemiosprime.pasabayan.data.model.MatchUpdateRequest(confirmationCode = it)
                }
                
                val result = matchRepository.pickupMatch(matchId, updateRequest)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Match $matchId marked as picked up")
                        updateMatchInState(updatedMatch)
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to mark as picked up"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to pickup match: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception picking up match: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mark match as delivered - mirrors iOS deliverMatch
     */
    fun deliverMatch(matchId: Int, confirmationCode: String? = null) {
        Log.d(TAG, "🎯 Marking match $matchId as delivered")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val updateRequest = confirmationCode?.let {
                    com.efthemiosprime.pasabayan.data.model.MatchUpdateRequest(confirmationCode = it)
                }
                
                val result = matchRepository.deliverMatch(matchId, updateRequest)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Match $matchId marked as delivered")
                        updateMatchInState(updatedMatch)
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to mark as delivered"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to deliver match: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception delivering match: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get match by package request ID
     */
    fun getMatchForPackage(packageRequestId: Int): DeliveryMatch? {
        return _shipperMatches.value.find { it.packageRequestId == packageRequestId }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Refresh all matches
     */
    fun refreshMatches() {
        Log.d(TAG, "🔄 Refreshing all matches...")
        loadShipperMatches()
        loadCarrierMatches()
    }
    
    /**
     * Carrier accepts shipper request - mirrors iOS acceptShipperRequest
     */
    fun acceptShipperRequest(matchId: Int, message: String) {
        Log.d(TAG, "✅ Accepting shipper request for match $matchId")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.acceptShipperRequest(matchId, message)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Shipper request accepted successfully")
                        Log.d(TAG, "   📊 Match Status: ${updatedMatch.status}")
                        Log.d(TAG, "   💬 Carrier Message: $message")
                        updateMatchInState(updatedMatch)
                        // Refresh all matches to get latest state
                        refreshMatches()
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to accept shipper request"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to accept shipper request: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception accepting shipper request: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carrier declines shipper request - mirrors iOS declineShipperRequest
     */
    fun declineShipperRequest(matchId: Int, reason: String, message: String) {
        Log.d(TAG, "❌ Declining shipper request for match $matchId")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.declineShipperRequest(matchId, reason, message)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Shipper request declined successfully")
                        Log.d(TAG, "   📊 Match Status: ${updatedMatch.status}")
                        Log.d(TAG, "   🚫 Decline Reason: $reason")
                        Log.d(TAG, "   💬 Carrier Message: $message")
                        updateMatchInState(updatedMatch)
                        // Refresh all matches to get latest state
                        refreshMatches()
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to decline shipper request"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to decline shipper request: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception declining shipper request: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Shipper accepts carrier request - mirrors iOS acceptCarrierRequest
     */
    fun acceptCarrierRequest(matchId: Int, message: String?) {
        Log.d(TAG, "✅ Accepting carrier request for match $matchId")
        if (!message.isNullOrBlank()) {
            Log.d(TAG, "   💬 Shipper Message: $message")
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.acceptCarrierRequest(matchId, message)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Carrier request accepted successfully")
                        Log.d(TAG, "   📊 Match Status: ${updatedMatch.status}")
                        Log.d(TAG, "   💬 Shipper Message: ${message ?: "No message"}")
                        updateMatchInState(updatedMatch)
                        // Refresh all matches to get latest state
                        refreshMatches()
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to accept carrier request"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to accept carrier request: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception accepting carrier request: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Shipper declines carrier request - mirrors iOS declineCarrierRequest
     */
    fun declineCarrierRequest(matchId: Int, reason: String?, message: String?) {
        Log.d(TAG, "❌ Declining carrier request for match $matchId")
        if (!reason.isNullOrBlank()) {
            Log.d(TAG, "   🚫 Decline Reason: $reason")
        }
        if (!message.isNullOrBlank()) {
            Log.d(TAG, "   💬 Shipper Message: $message")
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = matchRepository.declineCarrierRequest(matchId, reason, message)
                result.fold(
                    onSuccess = { updatedMatch ->
                        Log.d(TAG, "✅ Carrier request declined successfully")
                        Log.d(TAG, "   📊 Match Status: ${updatedMatch.status}")
                        Log.d(TAG, "   🚫 Decline Reason: ${reason ?: "No reason"}")
                        Log.d(TAG, "   💬 Shipper Message: ${message ?: "No message"}")
                        updateMatchInState(updatedMatch)
                        // Refresh all matches to get latest state
                        refreshMatches()
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to decline carrier request"
                        _errorMessage.value = errorMsg
                        Log.e(TAG, "❌ Failed to decline carrier request: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _errorMessage.value = errorMsg
                Log.e(TAG, "❌ Exception declining carrier request: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update match in local state - helper function
     */
    private fun updateMatchInState(updatedMatch: DeliveryMatch) {
        // Update in shipper matches
        _shipperMatches.value = _shipperMatches.value.map { match ->
            if (match.id == updatedMatch.id) updatedMatch else match
        }
        
        // Update in carrier matches
        _carrierMatches.value = _carrierMatches.value.map { match ->
            if (match.id == updatedMatch.id) updatedMatch else match
        }
    }
    
    /**
     * Get match statistics for shipper
     */
    fun getShipperMatchStats(): MatchStats {
        val matches = _shipperMatches.value
        return MatchStats(
            totalMatches = matches.size,
            pendingMatches = matches.count { it.status == com.efthemiosprime.pasabayan.data.model.MatchStatus.PENDING },
            confirmedMatches = matches.count { it.status == com.efthemiosprime.pasabayan.data.model.MatchStatus.CONFIRMED },
            inTransitMatches = matches.count { 
                it.status in listOf(
                    com.efthemiosprime.pasabayan.data.model.MatchStatus.PICKED_UP,
                    com.efthemiosprime.pasabayan.data.model.MatchStatus.IN_TRANSIT
                )
            },
            deliveredMatches = matches.count { it.status == com.efthemiosprime.pasabayan.data.model.MatchStatus.DELIVERED },
            cancelledMatches = matches.count { it.status == com.efthemiosprime.pasabayan.data.model.MatchStatus.CANCELLED }
        )
    }
    
    /**
     * Create Retrofit API service with existing auth pattern and centralized URL
     */
    private fun createApiService(): APIService {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                
                // Get auth token from AuthService - existing pattern
                try {
                    val token = kotlinx.coroutines.runBlocking { authService.getToken() }
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                        Log.d(TAG, "🔐 Added Authorization header to match API request")
                        Log.d(TAG, "   Token length: ${token.length}")
                    } else {
                        Log.w(TAG, "⚠️ No auth token available for match API request")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get token for match API request: ${e.message}")
                }
                
                chain.proceed(requestBuilder.build())
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl("${NetworkConfig.baseUrl}/") // Use centralized URL config
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(APIService::class.java)
    }
}

/**
 * Data class for match statistics
 */
data class MatchStats(
    val totalMatches: Int = 0,
    val pendingMatches: Int = 0,
    val confirmedMatches: Int = 0,
    val inTransitMatches: Int = 0,
    val deliveredMatches: Int = 0,
    val cancelledMatches: Int = 0
)