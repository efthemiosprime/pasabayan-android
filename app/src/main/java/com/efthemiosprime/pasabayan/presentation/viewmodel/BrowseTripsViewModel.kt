package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.common.LoadingState
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json

/**
 * Browse Trips ViewModel
 * Handles trip browsing, filtering, search, and direct booking functionality
 * Follows existing ViewModel patterns in the project
 * Uses StateFlow for reactive state management
 */
class BrowseTripsViewModel(
    private val context: Context
) : ViewModel() {

    // Dependencies following existing patterns
    private val authService = AuthService.getInstance(context)
    private val apiService = createApiService()
    private val tripRepository = TripRepositoryImpl(apiService)

    // MARK: - State Flows
    
    /**
     * Available trips for browsing
     */
    private val _availableTrips = MutableStateFlow<List<Trip>>(emptyList())
    val availableTrips: StateFlow<List<Trip>> = _availableTrips.asStateFlow()
    
    /**
     * Filtered trips based on current search and filters
     */
    private val _filteredTrips = MutableStateFlow<List<Trip>>(emptyList())
    val filteredTrips: StateFlow<List<Trip>> = _filteredTrips.asStateFlow()
    
    /**
     * Loading state for trip operations
     */
    private val _loadingState = MutableStateFlow<LoadingState<List<Trip>>>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState<List<Trip>>> = _loadingState.asStateFlow()
    
    /**
     * Search query
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Filter sheet state
     */
    private val _filterSheetState = MutableStateFlow(FilterSheetState())
    val filterSheetState: StateFlow<FilterSheetState> = _filterSheetState.asStateFlow()
    
    /**
     * Direct booking state
     */
    private val _bookingState = MutableStateFlow<BookingUIState>(BookingUIState.Idle)
    val bookingState: StateFlow<BookingUIState> = _bookingState.asStateFlow()
    
    /**
     * Error messages
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Success messages
     */
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadAvailableTrips()
        observeSearchAndFilters()
    }

    // MARK: - Public Actions
    
    /**
     * Load available trips from the server
     */
    fun loadAvailableTrips() {
        viewModelScope.launch {
            println("🔍 BrowseTripsViewModel: Starting loadAvailableTrips()")
            _loadingState.value = LoadingState.Loading
            println("🔍 BrowseTripsViewModel: Set loading state to Loading")
            
            delay(300) // Debounce for better UX
            
            try {
                println("🔍 BrowseTripsViewModel: Calling tripRepository.getAvailableTrips()")
                tripRepository.getAvailableTrips().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            println("🔍 BrowseTripsViewModel: Success! Got ${result.data.size} trips")
                            result.data.forEach { trip ->
                                println("   - Trip: ${trip.originCity} -> ${trip.destinationCity} (${trip.transportationMethod.displayName})")
                            }
                            _availableTrips.value = result.data
                            _loadingState.value = LoadingState.Success(result.data)
                            applyFilters() // Apply current filters to update filteredTrips
                            clearError()
                            println("🔍 BrowseTripsViewModel: Set success state with ${result.data.size} trips")
                        }
                        is Result.Failure -> {
                            val errorMsg = result.error.message ?: "Failed to load trips"
                            println("🔍 BrowseTripsViewModel: Failure! Error: $errorMsg")
                            println("🔍 BrowseTripsViewModel: Error type: ${result.error::class.simpleName}")
                            _loadingState.value = LoadingState.Error(result.error)
                            _errorMessage.value = errorMsg
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                println("🔍 BrowseTripsViewModel: Exception! Error: $errorMsg")
                println("🔍 BrowseTripsViewModel: Exception type: ${e::class.simpleName}")
                e.printStackTrace()
                _loadingState.value = LoadingState.Error(com.efthemiosprime.pasabayan.data.common.AppError.NetworkError(errorMsg))
                _errorMessage.value = errorMsg
            }
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    /**
     * Show filter sheet
     */
    fun showFilterSheet() {
        _filterSheetState.value = _filterSheetState.value.copy(
            isVisible = true,
            tempFilters = _filterSheetState.value.filters
        )
    }
    
    /**
     * Hide filter sheet
     */
    fun hideFilterSheet() {
        _filterSheetState.value = _filterSheetState.value.copy(
            isVisible = false,
            tempFilters = _filterSheetState.value.filters
        )
    }
    
    /**
     * Update temporary filters (while editing in filter sheet)
     */
    fun updateTempFilters(filters: TripFilters) {
        _filterSheetState.value = _filterSheetState.value.copy(tempFilters = filters)
    }
    
    /**
     * Apply filters from the filter sheet
     */
    fun applyFilters() {
        _filterSheetState.value = _filterSheetState.value.applyTempFilters()
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        _filterSheetState.value = _filterSheetState.value.clearFilters()
    }
    
    /**
     * Reset temporary filters to current filters
     */
    fun resetTempFilters() {
        _filterSheetState.value = _filterSheetState.value.resetTempFilters()
    }
    
    /**
     * Book a trip directly
     */
    fun bookTripDirectly(tripId: String, bookingRequest: DirectBookingRequest) {
        viewModelScope.launch {
            _bookingState.value = BookingUIState.Loading
            
            try {
                // Make API call to book the trip (iOS-style - no client-side validation)
                val response = apiService.bookTripDirectly(tripId, bookingRequest)
                
                if (response.success) {
                    _bookingState.value = BookingUIState.Success(response)
                    _successMessage.value = response.message
                    
                    // Refresh trips to update availability
                    loadAvailableTrips()
                } else {
                    _bookingState.value = BookingUIState.Error("Booking failed: ${response.message}")
                    _errorMessage.value = "Booking failed: ${response.message}"
                }
            } catch (e: Exception) {
                _bookingState.value = BookingUIState.Error(e.message ?: "Network error")
                _errorMessage.value = e.message ?: "Network error occurred"
            }
        }
    }
    
    /**
     * Reset booking state
     */
    fun resetBookingState() {
        _bookingState.value = BookingUIState.Idle
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    // MARK: - Private Functions
    
    /**
     * Observe search and filter changes to update filtered trips
     */
    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _availableTrips,
                _searchQuery,
                _filterSheetState.map { it.filters }
            ) { trips, query, filters ->
                filterTrips(trips, query, filters)
            }.collect { filtered ->
                _filteredTrips.value = filtered
            }
        }
    }
    
    /**
     * Filter trips based on search query and filters - Exactly matches iOS filtering
     * Uses actual Trip model fields from the current codebase
     */
    private fun filterTrips(trips: List<Trip>, query: String, filters: TripFilters): List<Trip> {
        var filtered = trips
        
        // Apply text search (includes both searchText and direct query)
        val searchTerms = listOfNotNull(
            query.takeIf { it.isNotBlank() },
            filters.searchText.takeIf { it.isNotBlank() }
        ).distinct()
        
        if (searchTerms.isNotEmpty()) {
            filtered = filtered.filter { trip ->
                searchTerms.any { term ->
                    trip.originCity.contains(term, ignoreCase = true) ||
                    trip.destinationCity.contains(term, ignoreCase = true) ||
                    trip.transportationMethod.displayName.contains(term, ignoreCase = true) ||
                    trip.specialNotes?.contains(term, ignoreCase = true) == true
                }
            }
        }
        
        // Apply specific filters using new TripFilters structure
        if (filters.origin.isNotBlank()) {
            filtered = filtered.filter { trip ->
                trip.originCity.contains(filters.origin, ignoreCase = true) ||
                trip.originCountry.contains(filters.origin, ignoreCase = true)
            }
        }
        
        if (filters.destination.isNotBlank()) {
            filtered = filtered.filter { trip ->
                trip.destinationCity.contains(filters.destination, ignoreCase = true) ||
                trip.destinationCountry.contains(filters.destination, ignoreCase = true)
            }
        }
        
        filters.transportationMethod?.let { method ->
            filtered = filtered.filter { trip ->
                trip.transportationMethod == method
            }
        }
        
        filters.minPricePerKg?.let { minPrice ->
            filtered = filtered.filter { trip ->
                trip.pricePerKg >= minPrice
            }
        }
        
        filters.maxPricePerKg?.let { maxPrice ->
            filtered = filtered.filter { trip ->
                trip.pricePerKg <= maxPrice
            }
        }
        
        filters.minWeightCapacity?.let { minWeight ->
            filtered = filtered.filter { trip ->
                trip.availableWeightKg >= minWeight
            }
        }
        
        filters.maxWeightCapacity?.let { maxWeight ->
            filtered = filtered.filter { trip ->
                trip.availableWeightKg <= maxWeight
            }
        }
        
        filters.minSpaceCapacity?.let { minSpace ->
            filtered = filtered.filter { trip ->
                trip.availableSpaceLiters >= minSpace
            }
        }
        
        filters.maxSpaceCapacity?.let { maxSpace ->
            filtered = filtered.filter { trip ->
                trip.availableSpaceLiters <= maxSpace
            }
        }
        
        // Date filtering
        filters.departureDate?.let { dateStr ->
            // For simplicity, check if the departure date contains the date string
            // In a real implementation, this would use proper date parsing
            filtered = filtered.filter { trip ->
                trip.departureDate.contains(dateStr)
            }
        }
        
        // Apply sorting using new TripSortOption enum
        filtered = when (filters.sortBy) {
            TripSortOption.PRICE_PER_KG -> if (filters.sortOrder == SortOrder.DESCENDING) {
                filtered.sortedByDescending { it.pricePerKg }
            } else {
                filtered.sortedBy { it.pricePerKg }
            }
            TripSortOption.AVAILABLE_CAPACITY -> if (filters.sortOrder == SortOrder.DESCENDING) {
                filtered.sortedByDescending { it.availableWeightKg }
            } else {
                filtered.sortedBy { it.availableWeightKg }
            }
            TripSortOption.DEPARTURE_DATE -> if (filters.sortOrder == SortOrder.DESCENDING) {
                filtered.sortedByDescending { it.departureDate }
            } else {
                filtered.sortedBy { it.departureDate }
            }
            TripSortOption.DISTANCE -> {
                // For now, sort by departure date as distance calculation is complex
                if (filters.sortOrder == SortOrder.DESCENDING) {
                    filtered.sortedByDescending { it.departureDate }
                } else {
                    filtered.sortedBy { it.departureDate }
                }
            }
        }
        
        return filtered
    }
    
    /**
     * Create APIService with authentication headers
     * Following the same pattern as DashboardViewModel
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
                
                // Get auth token from AuthService
                try {
                    val token = kotlinx.coroutines.runBlocking { authService.getToken() }
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                } catch (e: Exception) {
                    // Log error but continue without auth header
                }
                
                chain.proceed(requestBuilder.build())
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl("${APIService.BASE_URL}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(APIService::class.java)
    }

}

/**
 * UI State for Browse Trips Screen
 * Simplified to match existing patterns in the codebase
 */
data class BrowseTripsUiState(
    val trips: List<Trip> = emptyList(),
    val loadingState: LoadingState<List<Trip>> = LoadingState.Idle,
    val searchQuery: String = "",
    val filterSheetState: FilterSheetState = FilterSheetState(),
    val bookingState: BookingUIState = BookingUIState.Idle,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasActiveFilters: Boolean = false,
    val activeFiltersDescription: String = "No filters applied"
) {
    val isLoading: Boolean get() = loadingState is LoadingState.Loading
    val isEmpty: Boolean get() = trips.isEmpty() && !isLoading
    val isBookingInProgress: Boolean get() = bookingState is BookingUIState.Loading
}

/**
 * Booking UI State
 */
sealed class BookingUIState {
    object Idle : BookingUIState()
    object Loading : BookingUIState()
    data class Success(val response: DirectBookingResponse) : BookingUIState()
    data class Error(val message: String) : BookingUIState()
} 