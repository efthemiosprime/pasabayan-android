package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.domain.repository.PackageRepository
import com.efthemiosprime.pasabayan.data.repository.PackageRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log
import com.efthemiosprime.pasabayan.data.service.AuthService

/**
 * PackageViewModel manages package requests and operations
 * Enhanced version with filtering, repository integration, and real API calls
 * Mirrors iOS PackageViewModel structure with Android-specific enhancements
 */
class PackageViewModel(application: Application) : AndroidViewModel(application) {
    
    // ✅ Keep existing StateFlow properties:
    private val _packageRequests = MutableStateFlow<List<PackageRequest>>(emptyList())
    val packageRequests: StateFlow<List<PackageRequest>> = _packageRequests.asStateFlow()
    
    private val _myPackageRequests = MutableStateFlow<List<PackageRequest>>(emptyList())
    val myPackageRequests: StateFlow<List<PackageRequest>> = _myPackageRequests.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 🆕 ADD these new properties for filtering:
    private val _selectedFilter = MutableStateFlow<PackageRequestStatus?>(null)
    val selectedFilter: StateFlow<PackageRequestStatus?> = _selectedFilter.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Available packages state (for shipper browse functionality)
    private val _availablePackages = MutableStateFlow<List<PackageRequest>>(emptyList())
    val availablePackages: StateFlow<List<PackageRequest>> = _availablePackages.asStateFlow()
    
    private val _isLoadingAvailable = MutableStateFlow(false)
    val isLoadingAvailable: StateFlow<Boolean> = _isLoadingAvailable.asStateFlow()
    
    private val _availableErrorMessage = MutableStateFlow<String?>(null)
    val availableErrorMessage: StateFlow<String?> = _availableErrorMessage.asStateFlow()
    
    // 🆕 ADD PackageRepository dependency (manual DI following existing pattern):
    private val packageRepository: PackageRepository by lazy {
        PackageRepositoryImpl.create(getApplication())
    }
    
    // 🆕 ADD filtering logic matching iOS behavior:
    val filteredPackages: StateFlow<List<PackageRequest>> = combine(
        myPackageRequests,
        selectedFilter
    ) { packages, filter ->
        when (filter) {
            null -> packages
            PackageRequestStatus.PENDING -> packages.filter { 
                // 🔑 KEY: "Pending" filter includes both PENDING and OPEN statuses
                it.status in listOf(PackageRequestStatus.PENDING, PackageRequestStatus.OPEN) 
            }
            else -> packages.filter { it.status == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // Load real data from API
        loadPackageRequests()
    }
    
    // 🆕 ADD these new methods:
    fun setFilter(filter: PackageRequestStatus?) {
        _selectedFilter.value = filter
    }
    
    fun loadPackageRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            packageRepository.getPackageRequests()
                .onSuccess { packages ->
                    _packageRequests.value = packages
                    // Filter for current user packages (you'll need to get current user ID)
                    val currentUserId = getCurrentUserId() // Implement this
                    _myPackageRequests.value = packages.filter { it.shipperId == currentUserId }
                }
                .onFailure { exception ->
                    println("🔧 API Error: ${exception.message}")
                    _errorMessage.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadAvailablePackages(
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
    ) {
        viewModelScope.launch {
            _isLoadingAvailable.value = true
            _availableErrorMessage.value = null
            
            packageRepository.getAvailablePackages(
                page = page,
                origin = origin,
                destination = destination,
                maxWeight = maxWeight,
                urgency = urgency,
                minBudget = minBudget,
                maxBudget = maxBudget,
                fragile = fragile,
                pickupDateFrom = pickupDateFrom,
                pickupDateTo = pickupDateTo
            )
                .onSuccess { packages ->
                    _availablePackages.value = packages
                    println("✅ Available packages loaded successfully - Count: ${packages.size}")
                    if (packages.isNotEmpty()) {
                        println("   📦 First available package: ${packages.first().title}")
                        println("   📍 Route: ${packages.first().pickupLocation} → ${packages.first().deliveryLocation}")
                    }
                }
                .onFailure { exception ->
                    println("🔧 Available packages API Error: ${exception.message}")
                    _availableErrorMessage.value = exception.message
                }
            
            _isLoadingAvailable.value = false
        }
    }
    
    fun createPackageRequest(request: CreatePackageRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            packageRepository.createPackageRequest(request)
                .onSuccess { newPackage ->
                    // Add to beginning of list
                    _myPackageRequests.value = listOf(newPackage) + _myPackageRequests.value
                    _packageRequests.value = listOf(newPackage) + _packageRequests.value
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // 🆕 Helper method to get current user ID (integrate with your AuthService)
    private fun getCurrentUserId(): Int {
        // TODO: Get from your AuthService - you'll need to adapt this
        // val authService = AuthService.getInstance(context)
        // return authService.currentUser.value?.id ?: 0
        return 1 // Temporary - replace with actual user ID
    }
    

    
    // ✅ Keep existing filter methods but update them:
    fun getPackagesByStatus(status: PackageRequestStatus): List<PackageRequest> {
        return _myPackageRequests.value.filter { it.status == status }
    }
    
    fun getMatchedRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.MATCHED)
    fun getBookedRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.BOOKED)
    fun getInTransitRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.IN_TRANSIT)
    fun getDeliveredRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.DELIVERED)
    
    // Update existing method to use new status enum:
    fun getPendingRequests(): List<PackageRequest> = 
        _myPackageRequests.value.filter { 
            it.status == PackageRequestStatus.PENDING || it.status == PackageRequestStatus.OPEN 
        }
    
    /**
     * Refresh data (real API call)
     */
    suspend fun refreshData() {
        loadPackageRequests()
    }
    
    /**
     * Debug method to test server connection
     */
    fun debugServerConnection() {
        viewModelScope.launch {
            println("🔧 DEBUG: Testing server connection...")
            println("   📡 Server URL: http://10.0.2.2:8000/api")
            println("   🎯 Endpoint: GET /packages")
            println("   🔐 Auth: ${if (getCurrentAuthToken() != null) "Token available" else "No token"}")
            
            // Try to load packages and log detailed results
            loadPackageRequests()
        }
    }
    
    /**
     * Test method to verify authentication and load packages
     * Useful for debugging authentication issues
     */
    fun testAuthenticationAndLoadPackages() {
        viewModelScope.launch {
            try {
                                 Log.d("PackageViewModel", "🧪 Testing authentication and package loading")
                 
                 // First test authentication
                 val authResult = PackageRepositoryImpl.testAuthentication(getApplication())
                 authResult.onSuccess {
                     Log.d("PackageViewModel", "✅ Authentication test passed, loading packages")
                     loadPackageRequests()
                 }.onFailure { exception ->
                     Log.e("PackageViewModel", "❌ Authentication test failed: ${exception.message}")
                     Log.d("PackageViewModel", "🔧 Attempting mock login...")
                     
                     // Try mock login
                     val authService = AuthService.getInstance(getApplication())
                     val mockResult = authService.mockLogin()
                     
                     mockResult.onSuccess {
                         Log.d("PackageViewModel", "✅ Mock login successful, loading packages")
                         loadPackageRequests()
                     }.onFailure { mockException ->
                         Log.e("PackageViewModel", "❌ Mock login failed: ${mockException.message}")
                         _errorMessage.value = "Authentication failed: ${mockException.message}"
                     }
                 }
             } catch (e: Exception) {
                 Log.e("PackageViewModel", "❌ Test failed: ${e.message}", e)
                 _errorMessage.value = "Test failed: ${e.message}"
            }
        }
    }
    
    /**
     * Comprehensive debug method to compare iOS vs Android authentication
     * Since iOS works with the same server, this helps identify Android-specific issues
     */
    fun debugAndroidVsIOS() {
        viewModelScope.launch {
            try {
                Log.d("PackageViewModel", "🔍 === ANDROID vs iOS DEBUG COMPARISON ===")
                Log.d("PackageViewModel", "📱 Since iOS works with the same server, investigating Android-specific issues")
                Log.d("PackageViewModel", "")
                
                // Get comprehensive auth debug info
                val authService = AuthService.getInstance(getApplication())
                val debugInfo = authService.debugAuthenticationState()
                
                Log.d("PackageViewModel", "📊 Current authentication state:")
                Log.d("PackageViewModel", debugInfo)
                
                // Test the authentication flow step by step
                Log.d("PackageViewModel", "🧪 Testing authentication flow:")
                
                val token = authService.getToken()
                if (token != null) {
                    Log.d("PackageViewModel", "✅ Token found, testing API call...")
                    
                    // Try to load packages with existing token
                    _isLoading.value = true
                    _errorMessage.value = null
                    
                    val result = packageRepository.getPackageRequests()
                    result.onSuccess { packages ->
                                                 Log.d("PackageViewModel", "✅ SUCCESS! Packages loaded with existing token")
                         Log.d("PackageViewModel", "   📊 Package count: ${packages.size}")
                         _packageRequests.value = packages
                         _myPackageRequests.value = packages // This triggers filteredPackages update automatically
                         _isLoading.value = false
                    }.onFailure { exception ->
                        Log.e("PackageViewModel", "❌ FAILED with existing token: ${exception.message}")
                        Log.d("PackageViewModel", "🔄 Token might be invalid, trying mock login...")
                        
                        // Try mock login as fallback
                        val mockResult = authService.mockLogin()
                        mockResult.onSuccess {
                            Log.d("PackageViewModel", "✅ Mock login successful, retrying packages...")
                            loadPackageRequests()
                        }.onFailure { mockException ->
                            Log.e("PackageViewModel", "❌ Mock login also failed: ${mockException.message}")
                            _errorMessage.value = "Authentication failed: ${mockException.message}"
                            _isLoading.value = false
                        }
                    }
                } else {
                    Log.w("PackageViewModel", "❌ No token found, trying mock login...")
                    
                    // No token, try mock login
                    val mockResult = authService.mockLogin()
                    mockResult.onSuccess {
                        Log.d("PackageViewModel", "✅ Mock login successful, loading packages...")
                        loadPackageRequests()
                    }.onFailure { exception ->
                        Log.e("PackageViewModel", "❌ Mock login failed: ${exception.message}")
                        _errorMessage.value = "Authentication failed: ${exception.message}"
                        _isLoading.value = false
                    }
                }
                
                Log.d("PackageViewModel", "=== END ANDROID vs iOS DEBUG ===")
                
            } catch (e: Exception) {
                Log.e("PackageViewModel", "❌ Debug process failed: ${e.message}", e)
                _errorMessage.value = "Debug failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun getCurrentAuthToken(): String? {
        return try {
            com.efthemiosprime.pasabayan.data.service.AuthService.getInstance(getApplication()).getToken()
        } catch (e: Exception) {
            null
        }
    }
} 