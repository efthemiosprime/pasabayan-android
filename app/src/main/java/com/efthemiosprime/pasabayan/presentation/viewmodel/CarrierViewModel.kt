package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.Booking
import com.efthemiosprime.pasabayan.data.model.BookingStatus
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.CarrierStatsData
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.presentation.common.FunctionalViewModel
import com.efthemiosprime.pasabayan.presentation.common.UiState
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.ui.screens.dashboard.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Functional state for carrier operations
 */
data class CarrierState(
    val trips: UiState<List<Trip>> = UiState.idle(),
    val bookings: UiState<List<Booking>> = UiState.idle(),
    val profile: CarrierProfile = CarrierProfile(),
    val selectedFilter: TripStatus? = null,
    val isRefreshing: Boolean = false
) {
    val isLoading: Boolean get() = trips.isLoading || bookings.isLoading
    val error: AppError? get() = trips.error ?: bookings.error
    
    val carrierStatusText: String
        get() = if (profile.isActive) "Online" else "Offline"
    
    val averageRatingText: String
        get() = String.format("%.1f", profile.averageRating)
    
    val filteredTrips: List<Trip>
        get() = trips.data?.let { tripList ->
            selectedFilter?.let { filter ->
                tripList.filter { it.tripStatus == filter }
            } ?: tripList
        } ?: emptyList()
    
    // Helper to create profile with current trips data
    fun profileWithTripsData(): CarrierProfile = profile.copy(tripsData = trips.data)
}

/**
 * Carrier profile data
 */
data class CarrierProfile(
    val isSetup: Boolean = true,
    val isActive: Boolean = false,
    val statsData: CarrierStatsData? = null,
    val tripsData: List<Trip>? = null
) {
    val totalEarnings: String
        get() = statsData?.earnings?.totalEarnings?.let { "CAD ${String.format("%.2f", it)}" } ?: "CAD 0.00"
    
    val averageRating: Double
        get() = statsData?.ratings?.averageRating ?: 0.0
    
    val totalTrips: Int
        get() = statsData?.deliveries?.totalTrips ?: 0
    
    val activeTrips: Int
        get() {
            // If we have actual trips data, count PLANNING + ACTIVE trips
            return if (tripsData != null) {
                tripsData.count { it.tripStatus == TripStatus.ACTIVE || it.tripStatus == TripStatus.PLANNING }
            } else {
                // Fall back to API stats (which might not include PLANNING)
                statsData?.deliveries?.activeTrips ?: 0
            }
        }
    
    val totalMatches: Int
        get() = statsData?.deliveries?.totalMatches ?: 0
    
    val successRate: Double
        get() = statsData?.deliveries?.successRate ?: 0.0
}

/**
 * Carrier actions following functional patterns
 */
sealed class CarrierAction {
    object LoadTrips : CarrierAction()
    object LoadBookings : CarrierAction()
    object LoadProfile : CarrierAction()
    object LoadStats : CarrierAction()
    object RefreshData : CarrierAction()
    object ToggleCarrierStatus : CarrierAction()
    data class FilterTrips(val status: TripStatus?) : CarrierAction()
    data class TripsLoaded(val trips: List<Trip>) : CarrierAction()
    data class BookingsLoaded(val bookings: List<Booking>) : CarrierAction()
    data class StatsLoaded(val statsData: CarrierStatsData) : CarrierAction()
    data class LoadingError(val error: AppError) : CarrierAction()
    object ClearError : CarrierAction()
}

/**
 * Carrier side effects
 */
sealed class CarrierEffect {
    data class ShowToast(val message: String) : CarrierEffect()
    object RefreshComplete : CarrierEffect()
    data class NavigateToTrip(val tripId: Int) : CarrierEffect()
}

/**
 * CarrierViewModel manages carrier operations, trips, and bookings using functional patterns
 * Mirrors iOS CarrierViewModel structure with exact Trip model
 */
class CarrierViewModel(
    private val apiService: APIService
) : FunctionalViewModel<CarrierState, CarrierAction, CarrierEffect>(
    initialState = CarrierState()
) {
    
    // Internal state for user data
    private val _user = MutableStateFlow<User?>(null)
    
    // Computed UI state following functional patterns
    private val _uiState = combine(
        _user,
        state
    ) { user, carrierState ->
        DashboardUiState(
            user = user,
            isLoading = carrierState.isLoading,
            errorMessage = carrierState.error?.message,
            activeTripsCount = carrierState.trips.data?.count { 
                it.tripStatus == TripStatus.ACTIVE || it.tripStatus == TripStatus.PLANNING 
            } ?: 0,
            activeBookingsCount = carrierState.bookings.data?.size ?: 0,
            totalEarnings = carrierState.profile.totalEarnings,
            averageRatingText = carrierState.averageRatingText,
            recentTrips = carrierState.trips.data?.take(3) ?: emptyList(),
            recentPackages = emptyList() // Carriers don't have packages
        )
    }
    
    val uiState: StateFlow<DashboardUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
    
    // MARK: - Pure Reducer Function
    
    override fun reduce(currentState: CarrierState, action: CarrierAction): CarrierState = when (action) {
        is CarrierAction.LoadTrips -> currentState.copy(
            trips = UiState.loading(currentState.trips.data)
        )
        
        is CarrierAction.LoadBookings -> currentState.copy(
            bookings = UiState.loading(currentState.bookings.data)
        )
        
        is CarrierAction.LoadProfile -> currentState.copy(
            trips = UiState.loading(currentState.trips.data),
            bookings = UiState.loading(currentState.bookings.data)
        )
        
        is CarrierAction.LoadStats -> currentState.copy(
            trips = UiState.loading(currentState.trips.data)
        )
        
        is CarrierAction.RefreshData -> currentState.copy(
            isRefreshing = true,
            trips = UiState.loading(currentState.trips.data),
            bookings = UiState.loading(currentState.bookings.data)
        )
        
        is CarrierAction.ToggleCarrierStatus -> currentState.copy(
            profile = currentState.profile.copy(
                isActive = !currentState.profile.isActive,
                tripsData = currentState.trips.data // Preserve existing trips data
            )
        )
        
        is CarrierAction.FilterTrips -> currentState.copy(
            selectedFilter = action.status
        )
        
        is CarrierAction.TripsLoaded -> currentState.copy(
            trips = UiState.success(action.trips),
            isRefreshing = false
        ).let { newState ->
            // Update profile with new trips data
            newState.copy(profile = newState.profile.copy(tripsData = action.trips))
        }
        
        is CarrierAction.BookingsLoaded -> currentState.copy(
            bookings = UiState.success(action.bookings),
            isRefreshing = false
        )
        
        is CarrierAction.StatsLoaded -> currentState.copy(
            profile = currentState.profile.copy(
                statsData = action.statsData,
                isSetup = action.statsData.profileComplete,
                tripsData = currentState.trips.data // Preserve existing trips data
            ),
            isRefreshing = false
        )
        
        is CarrierAction.LoadingError -> currentState.copy(
            trips = if (currentState.trips.isLoading) 
                UiState.error(action.error, currentState.trips.data) 
            else currentState.trips,
            bookings = if (currentState.bookings.isLoading) 
                UiState.error(action.error, currentState.bookings.data) 
            else currentState.bookings,
            isRefreshing = false
        )
        
        is CarrierAction.ClearError -> currentState.copy(
            trips = currentState.trips.copy(error = null),
            bookings = currentState.bookings.copy(error = null)
        )
    }
    
    // MARK: - Side Effects Handler
    
    override suspend fun handleSideEffect(action: CarrierAction, currentState: CarrierState): CarrierEffect? = when (action) {
        is CarrierAction.LoadTrips -> {
            loadTripsInternal()
            null
        }
        is CarrierAction.LoadBookings -> {
            loadBookingsInternal()
            null
        }
        is CarrierAction.LoadProfile -> {
            loadProfileInternal()
            null
        }
        is CarrierAction.LoadStats -> {
            loadStatsInternal()
            null
        }
        is CarrierAction.RefreshData -> {
            refreshDataInternal()
            CarrierEffect.RefreshComplete
        }
        is CarrierAction.ToggleCarrierStatus -> {
            val statusText = if (currentState.profile.isActive) "Going Offline" else "Going Online"
            CarrierEffect.ShowToast(statusText)
        }
        else -> null
    }
    
    init {
        // Initialize with mock user data
        _user.value = User(
            id = 1,
            name = "Jane Carrier",
            email = "jane@example.com",
            avatar = null
        )
        loadCarrierData()
    }
    
    private fun loadCarrierData() {
        dispatch(CarrierAction.LoadTrips)
        dispatch(CarrierAction.LoadBookings)
        dispatch(CarrierAction.LoadStats)
    }
    
    // MARK: - Public API Methods (Functional Dispatch)
    
    /**
     * Toggle carrier online/offline status
     */
    fun toggleCarrierStatus() {
        if (state.value.profile.isSetup) {
            dispatch(CarrierAction.ToggleCarrierStatus)
        }
    }
    
    /**
     * Load carrier profile data
     */
    fun loadCarrierProfile() {
        dispatch(CarrierAction.LoadProfile)
    }
    
    /**
     * Load carrier statistics
     */
    fun loadCarrierStats() {
        dispatch(CarrierAction.LoadStats)
    }
    
    /**
     * Load trips data (matching iOS implementation)
     */
    fun loadTrips() {
        dispatch(CarrierAction.LoadTrips)
    }
    
    /**
     * Refresh trips data (for pull-to-refresh)
     */
    fun refreshTrips() {
        dispatch(CarrierAction.RefreshData)
    }
    
    /**
     * Load active bookings
     */
    fun loadActiveBookings() {
        dispatch(CarrierAction.LoadBookings)
    }
    
    /**
     * Filter trips by status
     */
    fun filterTrips(status: TripStatus?) {
        dispatch(CarrierAction.FilterTrips(status))
    }
    
    /**
     * Clear errors
     */
    fun clearError() {
        dispatch(CarrierAction.ClearError)
    }
    
    // MARK: - Legacy API (for backward compatibility)
    
    /**
     * Get trips filtered by status
     */
    fun getTripsFilteredBy(status: TripStatus?): List<Trip> {
        return state.value.filteredTrips
    }
    
    /**
     * Get trip count by status
     */
    fun getTripCountBy(status: TripStatus): Int {
        return state.value.trips.data?.count { it.tripStatus == status } ?: 0
    }
    
    // Legacy StateFlow properties for backward compatibility
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _activeBookings = MutableStateFlow<List<Booking>>(emptyList())
    val activeBookings: StateFlow<List<Booking>> = _activeBookings.asStateFlow()
    
    private val _isCarrierProfileSetup = MutableStateFlow(true)
    val isCarrierProfileSetup: StateFlow<Boolean> = _isCarrierProfileSetup.asStateFlow()
    
    private val _isCarrierActive = MutableStateFlow(false)
    val isCarrierActive: StateFlow<Boolean> = _isCarrierActive.asStateFlow()
    
    private val _totalEarnings = MutableStateFlow("$298.80")
    val totalEarnings: StateFlow<String> = _totalEarnings.asStateFlow()
    
    private val _averageRating = MutableStateFlow(4.8)
    val averageRating: StateFlow<Double> = _averageRating.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val carrierStatusText: String
        get() = state.value.carrierStatusText
    
    val averageRatingText: String
        get() = state.value.averageRatingText
    
    // MARK: - Private Implementation Methods
    
    private suspend fun loadTripsInternal() {
        try {
            // Fetch real trips from API
            val response = apiService.getTrips()
            
            if (response.message == "Trips retrieved successfully") {
                val trips = response.data.data // Extract trips from pagination wrapper
                dispatch(CarrierAction.TripsLoaded(trips))
                
                // Update legacy StateFlow
                _trips.value = trips
            } else {
                dispatch(CarrierAction.LoadingError(AppError.NetworkError("Failed to load trips: ${response.message}")))
            }
        } catch (e: Exception) {
            dispatch(CarrierAction.LoadingError(AppError.NetworkError(e.message ?: "Failed to load trips")))
        }
    }
    
    private suspend fun loadBookingsInternal() {
        try {
            delay(500)
            // Mock bookings data (keeping existing structure)
            val mockBookings = listOf(
                Booking(
                    id = 1,
                    packageRequestId = 1,
                    tripId = 1,
                    shipperId = 1,
                    carrierId = 1,
                    status = BookingStatus.CONFIRMED,
                    agreedPrice = 125.0,
                    createdAt = "2024-01-14T11:00:00Z",
                    updatedAt = "2024-01-14T11:00:00Z"
                ),
                Booking(
                    id = 2,
                    packageRequestId = 2,
                    tripId = 2,
                    shipperId = 2,
                    carrierId = 1,
                    status = BookingStatus.PENDING,
                    agreedPrice = 37.5,
                    createdAt = "2024-01-15T10:00:00Z",
                    updatedAt = "2024-01-15T10:00:00Z"
                )
            )
            
            dispatch(CarrierAction.BookingsLoaded(mockBookings))
            
            // Update legacy StateFlow
            _activeBookings.value = mockBookings
        } catch (e: Exception) {
            dispatch(CarrierAction.LoadingError(AppError.NetworkError(e.message ?: "Failed to load bookings")))
        }
    }
    
    private suspend fun loadProfileInternal() {
        loadTripsInternal()
        loadBookingsInternal()
    }
    
    private suspend fun loadStatsInternal() {
        try {
            // Fetch real carrier stats from API
            val response = apiService.getCarrierStats()
            
            if (response.success) {
                dispatch(CarrierAction.StatsLoaded(response.data))
                
                // Update legacy StateFlows for backward compatibility
                _totalEarnings.value = response.data.earnings.totalEarnings.let { "CAD ${String.format("%.2f", it)}" }
                _averageRating.value = response.data.ratings.averageRating
                _isCarrierProfileSetup.value = response.data.profileComplete
            } else {
                dispatch(CarrierAction.LoadingError(AppError.NetworkError("Failed to load stats: ${response.message}")))
            }
        } catch (e: Exception) {
            dispatch(CarrierAction.LoadingError(AppError.NetworkError(e.message ?: "Failed to load stats")))
        }
    }
    
    private suspend fun refreshDataInternal() {
        loadTripsInternal()
        loadBookingsInternal()
    }
    
    // Sync legacy StateFlows with functional state
    init {
        viewModelScope.launch {
            state.collect { currentState ->
                _isLoading.value = currentState.isLoading
                _isCarrierActive.value = currentState.profile.isActive
                _isCarrierProfileSetup.value = currentState.profile.isSetup
                _totalEarnings.value = currentState.profile.totalEarnings
                _averageRating.value = currentState.profile.averageRating
            }
        }
    }
} 