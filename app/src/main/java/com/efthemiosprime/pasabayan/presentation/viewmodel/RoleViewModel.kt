package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import android.content.Context

/**
 * RoleViewModel manages user role switching between Shipper and Carrier
 * Mirrors iOS RoleViewModel structure with API validation
 */
class RoleViewModel : ViewModel() {
    
    private val _currentRole = MutableStateFlow(UserRole.SHIPPER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()
    
    private val _availableRoles = MutableStateFlow<List<UserRole>>(listOf(UserRole.SHIPPER))
    val availableRoles: StateFlow<List<UserRole>> = _availableRoles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Track if user explicitly chose a role (prevents auto-switching)
    private var userExplicitlySetRole = false
    
    // Cache carrier profile status to avoid repeated API calls
    private var carrierProfileCache: Boolean? = null
    private var carrierProfileCacheTime: Long = 0
    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes
    
    private val TAG = "RoleViewModel"
    
    // AuthService will be injected and provides authenticated API service
    private lateinit var authService: AuthService
    private var isInitialized = false
    
    init {
        setupDefaultRoles()
    }
    
    /**
     * Set up default roles - all users can be shippers
     * Carrier role availability will be determined by API/user data
     */
    private fun setupDefaultRoles() {
        Log.d(TAG, "🔧 Setting up default roles")
        _availableRoles.value = listOf(UserRole.SHIPPER, UserRole.CARRIER)
        _currentRole.value = UserRole.SHIPPER
        Log.d(TAG, "   ✅ Default roles set: ${_availableRoles.value}")
    }
    
    /**
     * Initialize with context - called from UI
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        Log.d(TAG, "🔧 Initializing RoleViewModel with context")
        authService = AuthService.getInstance(context)
        isInitialized = true
        setupUserObserver()
    }
    
    private fun setupUserObserver() {
        if (!::authService.isInitialized) {
            Log.w(TAG, "⚠️ AuthService not initialized, skipping user observer setup")
            return
        }
        
        viewModelScope.launch {
            authService.currentUser.collect { user ->
                Log.d(TAG, "👤 User data updated: ${user?.name}")
                if (user != null) {
                    updateAvailableRoles(user)
                } else {
                    Log.d(TAG, "   No user logged in, keeping default roles")
                    // Keep default roles when no user is logged in
                    _availableRoles.value = listOf(UserRole.SHIPPER, UserRole.CARRIER)
                }
            }
        }
    }
    
    private fun updateAvailableRoles(user: User) {
        Log.d(TAG, "🔍 Updating available roles for user")
        Log.d(TAG, "   User types: ${user.userTypes}")
        Log.d(TAG, "   Is active carrier: ${user.isActiveCarrier}")
        Log.d(TAG, "   Is active shipper: ${user.isActiveShipper}")
        Log.d(TAG, "   Current role before update: ${_currentRole.value}")
        Log.d(TAG, "   User explicitly set role: $userExplicitlySetRole")
        
        // Use user's available roles like iOS (if available), otherwise default to both
        val roles = if (user.availableRoles.isNotEmpty()) {
            user.availableRoles
        } else {
            // Fallback to both roles if server doesn't provide availableRoles
            listOf(UserRole.SHIPPER, UserRole.CARRIER)
        }
        
        _availableRoles.value = roles
        Log.d(TAG, "   Available roles: ${_availableRoles.value}")
        
        // Only auto-set role if user hasn't explicitly chosen one
        if (!userExplicitlySetRole) {
            when {
                user.isActiveCarrier && user.isCarrier -> {
                    _currentRole.value = UserRole.CARRIER
                    Log.d(TAG, "   Auto-setting role to carrier (is active carrier)")
                }
                user.isActiveShipper || user.isShipper -> {
                    _currentRole.value = UserRole.SHIPPER
                    Log.d(TAG, "   Auto-setting role to shipper")
                }
                roles.isNotEmpty() -> {
                    _currentRole.value = roles.first()
                    Log.d(TAG, "   Auto-setting role to first available: ${roles.first()}")
                }
                else -> {
                    _currentRole.value = UserRole.SHIPPER
                    Log.d(TAG, "   Auto-setting role to default shipper")
                }
            }
        } else {
            Log.d(TAG, "   Preserving user's explicitly chosen role: ${_currentRole.value}")
        }
        
        Log.d(TAG, "   Final current role: ${_currentRole.value}")
    }
    
    /**
     * Switch between Shipper and Carrier roles
     * For carrier role, validates with API first
     */
    fun switchRole(role: UserRole) {
        Log.d(TAG, "🔄 Switching to role: $role")
        Log.d(TAG, "   Available roles: ${_availableRoles.value}")
        
        // REMOVED: Server-dependent restriction that was preventing role switching
        // Users can always switch between roles - this matches iOS behavior
        Log.d(TAG, "✅ Role switch allowed (unrestricted access)")
        
        when (role) {
            UserRole.SHIPPER -> {
                // Always instant switch to shipper
                Log.d(TAG, "   📱 Switching to shipper role - instant switch")
                _currentRole.value = UserRole.SHIPPER
                userExplicitlySetRole = true
                _errorMessage.value = null
            }
            UserRole.CARRIER -> {
                // Use coroutine for carrier profile checking
                viewModelScope.launch {
                    val currentUser = authService.currentUser.value
                    Log.d(TAG, "   🚛 Switching to carrier role...")
                    Log.d(TAG, "   👤 Current user active carrier status: ${currentUser?.isActiveCarrier}")
                    
                    // First check user's current active status for instant switching
                    if (currentUser?.isActiveCarrier == true) {
                        Log.d(TAG, "   ⚡ User already active carrier - instant switch")
                        _currentRole.value = UserRole.CARRIER
                        userExplicitlySetRole = true
                        _errorMessage.value = null
                        return@launch
                    }
                    
                    // Check carrier profile to see if user is already a carrier
                    Log.d(TAG, "   🔍 Checking carrier profile first...")
                    _isLoading.value = true
                    
                    val isCarrier = isUserAlreadyCarrier()
                    when (isCarrier) {
                        true -> {
                            // User IS already a carrier - switch locally
                            Log.d(TAG, "   ✅ User IS already a carrier (200) - switching locally")
                            _currentRole.value = UserRole.CARRIER
                            userExplicitlySetRole = true
                            _errorMessage.value = null
                            _isLoading.value = false
                        }
                        false -> {
                            // User is NOT a carrier (403) - call toggle API
                            Log.d(TAG, "   🚫 User is NOT a carrier (403) - calling toggle API")
                            toggleCarrierStatus()
                            // toggleCarrierStatus will handle loading state
                        }
                        null -> {
                            // Profile check failed - fall back to toggle API
                            Log.d(TAG, "   ⚠️ Profile check failed - falling back to toggle API")
                            toggleCarrierStatus()
                            // toggleCarrierStatus will handle loading state
                        }
                    }
                }
            }
        }
        
        Log.d(TAG, "   🎯 Final role: ${_currentRole.value}")
    }
    
    /**
     * Toggle carrier status with backend validation
     * Mirrors iOS toggleCarrierStatus method
     * ALWAYS calls API regardless of auth state - handles auth errors gracefully
     */
    private fun toggleCarrierStatus() {
        Log.d(TAG, "🚛 Enabling carrier role via API...")
        Log.d(TAG, "   📡 Making POST request to /carrier/toggle-status")
        
        // Check if user is authenticated before making API call
        if (!::authService.isInitialized) {
            Log.e(TAG, "❌ AuthService not initialized")
            _errorMessage.value = "Please log in to activate carrier mode"
            _currentRole.value = UserRole.SHIPPER
            userExplicitlySetRole = true
            return
        }
        
        // Debug authentication status
        Log.d(TAG, "🔍 Checking authentication status...")
        Log.d(TAG, "   AuthService initialized: ${::authService.isInitialized}")
        Log.d(TAG, "   IsAuthenticated: ${authService.isAuthenticated.value}")
        
        // Check token availability
        viewModelScope.launch {
            val token = try {
                authService.getToken()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to get token: ${e.message}")
                null
            }
            
            Log.d(TAG, "   Token available: ${token != null}")
            Log.d(TAG, "   Token length: ${token?.length ?: 0}")
            if (token != null) {
                Log.d(TAG, "   Token preview: ${token.take(20)}...")
            } else {
                Log.w(TAG, "   ⚠️ NO TOKEN FOUND - User needs to log in!")
                _errorMessage.value = "Please log in to activate carrier mode"
                _currentRole.value = UserRole.SHIPPER
                userExplicitlySetRole = true
                _isLoading.value = false
                return@launch
            }
            
            // Continue with API call only if token exists
            proceedWithApiCall()
        }
    }
    
    private suspend fun proceedWithApiCall() {
        if (!authService.isAuthenticated.value) {
            Log.e(TAG, "❌ User not authenticated")
            _errorMessage.value = "Please log in to activate carrier mode"
            _currentRole.value = UserRole.SHIPPER
            userExplicitlySetRole = true
            return
        }
        
        Log.d(TAG, "   ✅ User is authenticated, proceeding with API call")
        
        val currentUser = authService.currentUser.value
        Log.d(TAG, "   👤 Current user: ${currentUser?.name ?: "null"}")
        Log.d(TAG, "   📧 User email: ${currentUser?.email ?: "null"}")
        Log.d(TAG, "   🏷️ User types: ${currentUser?.userTypes ?: "null"}")
        Log.d(TAG, "   🚛 Is carrier: ${currentUser?.isCarrier ?: "false"}")
        Log.d(TAG, "   🚛 Is active carrier: ${currentUser?.isActiveCarrier ?: "false"}")
        
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val carrierStatusResponse = authService.toggleCarrierStatus()
            
            Log.d(TAG, "📨 Received carrierStatusData from API")
            Log.d(TAG, "   🚛 isActiveCarrier: ${carrierStatusResponse.data.isActiveCarrier}")
            Log.d(TAG, "   📈 carrierStatus: ${carrierStatusResponse.data.carrierStatus}")
            Log.d(TAG, "   📝 hasCarrierProfile: ${carrierStatusResponse.data.hasCarrierProfile}")
            Log.d(TAG, "   💡 profileRecommended: ${carrierStatusResponse.data.profileRecommended}")
            Log.d(TAG, "   👥 userTypes: ${carrierStatusResponse.data.userTypes}")
            
            // Only switch to carrier role if API confirms user is active carrier
            if (carrierStatusResponse.data.isActiveCarrier) {
                Log.d(TAG, "   ✅ Switching to carrier role - user is active carrier")
                _currentRole.value = UserRole.CARRIER
                userExplicitlySetRole = true
                
                // Clear cache since user is now definitely a carrier
                clearCarrierProfileCache()
                
                // Show helpful message about profile setup if recommended
                if (carrierStatusResponse.data.profileRecommended && !carrierStatusResponse.data.hasCarrierProfile) {
                    _errorMessage.value = "Carrier mode activated! Consider completing your carrier profile for better trip visibility."
                } else {
                    _errorMessage.value = null
                }
                
                // Refresh user data to get updated status
                Log.d(TAG, "   🔄 Refreshing user data...")
                refreshUserData()
            } else {
                Log.d(TAG, "   ❌ User is not active carrier, staying on shipper role")
                _currentRole.value = UserRole.SHIPPER
                userExplicitlySetRole = true
                _errorMessage.value = "Carrier status could not be activated. Staying on shipper role."
            }
            
            Log.d(TAG, "   🎯 Final role after API response: ${_currentRole.value}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ API call failed", e)
            
            // Extract actual error message from API response
            val errorMsg = when (e) {
                is retrofit2.HttpException -> {
                    try {
                        // Try to get the response body which contains the actual error message
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.d(TAG, "   📝 API error response body: $errorBody")
                        
                        // Parse the JSON error body to get the message
                        if (errorBody?.contains("message") == true) {
                            // Extract message from JSON like {"message":"User is not registered as a carrier"}
                            val messageStart = errorBody.indexOf("\"message\":\"") + 11
                            val messageEnd = errorBody.indexOf("\"", messageStart)
                            if (messageStart > 10 && messageEnd > messageStart) {
                                errorBody.substring(messageStart, messageEnd)
                            } else {
                                errorBody
                            }
                        } else {
                            errorBody ?: e.message ?: "HTTP ${e.code()}"
                        }
                    } catch (parseError: Exception) {
                        Log.w(TAG, "   ⚠️ Failed to parse error body: ${parseError.message}")
                        e.message ?: "HTTP ${e.code()}"
                    }
                }
                else -> e.message ?: "Unknown error"
            }
            
            Log.d(TAG, "   🔍 Parsed error message: $errorMsg")
            
            // Handle specific error cases - check specific messages BEFORE generic HTTP codes
            when {
                errorMsg.contains("401") || errorMsg.contains("unauthorized", ignoreCase = true) -> {
                    Log.d(TAG, "   🔐 Authentication error - user needs to log in")
                    _errorMessage.value = "Please log in to activate carrier mode"
                }
                errorMsg.contains("not registered as a carrier", ignoreCase = true) ||
                errorMsg.contains("404") || 
                errorMsg.contains("not found", ignoreCase = true) ||
                errorMsg.contains("profile", ignoreCase = true) -> {
                    Log.d(TAG, "   📝 Carrier profile/registration error - API should now auto-enable")
                    Log.d(TAG, "   💡 Using local fallback for backward compatibility")
                    Log.d(TAG, "   📝 User can complete profile when creating trips")
                    _currentRole.value = UserRole.CARRIER  // ← MATCH iOS: Switch to carrier locally
                    userExplicitlySetRole = true
                    _errorMessage.value = "Carrier mode activated! You can complete your carrier profile later when creating trips."
                    _isLoading.value = false
                    return // Return early, don't set to shipper role
                }
                errorMsg.contains("403") || errorMsg.contains("forbidden", ignoreCase = true) -> {
                    Log.d(TAG, "   🚫 Permission denied - user not authorized for carrier role")
                    _errorMessage.value = "You don't have permission to activate carrier mode. Please contact support or complete carrier registration."
                }
                errorMsg.contains("network", ignoreCase = true) || 
                errorMsg.contains("timeout", ignoreCase = true) ||
                errorMsg.contains("connection", ignoreCase = true) -> {
                    Log.d(TAG, "   📶 Network error - will retry later")
                    _errorMessage.value = "Network error. Please check your connection and try again."
                }
                else -> {
                    Log.d(TAG, "   ⚠️ Showing error to user: $errorMsg")
                    _errorMessage.value = "Unable to activate carrier mode: $errorMsg"
                }
            }
            
            // ALWAYS stay on shipper role when API call fails - EXCEPT for carrier profile errors (handled above)
            Log.d(TAG, "   ❌ Staying on shipper role due to API failure")
            _currentRole.value = UserRole.SHIPPER
            userExplicitlySetRole = true
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Update carrier active status
     * Mirrors iOS updateCarrierActiveStatus method
     */
    fun updateCarrierActiveStatus(isActive: Boolean) {
        if (!::authService.isInitialized) {
            Log.e(TAG, "❌ AuthService not initialized")
            return
        }
        
        val currentUser = authService.currentUser.value
        if (currentUser?.isCarrier != true) {
            Log.e(TAG, "❌ User is not a carrier, cannot update carrier status")
            return
        }
        
        Log.d(TAG, "🚛 Updating carrier active status via API...")
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val carrierStatusResponse = authService.toggleCarrierStatus()
                
                Log.d(TAG, "✅ Carrier status updated via API")
                Log.d(TAG, "   🚛 Is Active Carrier: ${carrierStatusResponse.data.isActiveCarrier}")
                Log.d(TAG, "   📊 Status: ${carrierStatusResponse.data.carrierStatus}")
                
                // Only switch to carrier role if API confirms user is active carrier
                if (carrierStatusResponse.data.isActiveCarrier) {
                    Log.d(TAG, "   ✅ Switching to carrier role - user is active carrier")
                    _currentRole.value = UserRole.CARRIER
                    userExplicitlySetRole = true
                } else {
                    Log.d(TAG, "   ❌ User is not active carrier, switching to shipper role")
                    _currentRole.value = UserRole.SHIPPER
                    userExplicitlySetRole = true
                    _errorMessage.value = "Carrier status could not be activated. Switched to shipper role."
                }
                
                // Refresh user data to get updated status
                refreshUserData()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to update carrier status", e)
                _errorMessage.value = e.message ?: "Failed to update carrier status"
                
                // ALWAYS stay on shipper role when API call fails - just like iOS
                Log.d(TAG, "   ❌ Staying on shipper role due to API failure")
                _currentRole.value = UserRole.SHIPPER
                userExplicitlySetRole = true
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh user data from backend
     * Mirrors iOS refreshUserData method
     */
    private fun refreshUserData() {
        if (!::authService.isInitialized) return
        
        Log.d(TAG, "🔄 Refreshing user data...")
        viewModelScope.launch {
            authService.refreshCurrentUser()
                .onSuccess { Log.d(TAG, "✅ User data refreshed successfully") }
                .onFailure { Log.e(TAG, "❌ Failed to refresh user data", it) }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Toggle between roles
     */
    fun toggleRole() {
        val newRole = when (_currentRole.value) {
            UserRole.SHIPPER -> UserRole.CARRIER
            UserRole.CARRIER -> UserRole.SHIPPER
        }
        switchRole(newRole)
    }
    
    // MARK: - Computed Properties
    val canSwitchToCarrier: Boolean
        get() = _availableRoles.value.contains(UserRole.CARRIER)
    
    val canSwitchToShipper: Boolean
        get() = _availableRoles.value.contains(UserRole.SHIPPER)
    
    val hasMultipleRoles: Boolean
        get() = _availableRoles.value.size > 1
    
    val currentRoleDisplayName: String
        get() = when (_currentRole.value) {
            UserRole.SHIPPER -> "Shipper"
            UserRole.CARRIER -> "Carrier"
        }
    
    /**
     * Check if user is already a carrier by checking profile
     * Caches result to avoid repeated API calls
     */
    private suspend fun isUserAlreadyCarrier(): Boolean? {
        // Check cache first
        val currentTime = System.currentTimeMillis()
        if (carrierProfileCache != null && 
            currentTime - carrierProfileCacheTime < CACHE_VALIDITY_MS) {
            Log.d(TAG, "📋 Using cached carrier profile status: $carrierProfileCache")
            return carrierProfileCache
        }
        
        Log.d(TAG, "🔍 Cache expired or not available, checking carrier profile...")
        
        return try {
            val result = authService.checkCarrierProfile()
            result.fold(
                onSuccess = { isCarrier ->
                    Log.d(TAG, "✅ Carrier profile check completed: isCarrier = $isCarrier")
                    // Cache the result
                    carrierProfileCache = isCarrier
                    carrierProfileCacheTime = currentTime
                    isCarrier
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Carrier profile check failed: ${error.message}")
                    // Don't cache failures, return null to fall back to toggle API
                    null
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during carrier profile check: ${e.message}")
            null
        }
    }
    
    /**
     * Clear carrier profile cache (useful after successful carrier activation)
     */
    private fun clearCarrierProfileCache() {
        Log.d(TAG, "🗑️ Clearing carrier profile cache")
        carrierProfileCache = null
        carrierProfileCacheTime = 0
    }
} 