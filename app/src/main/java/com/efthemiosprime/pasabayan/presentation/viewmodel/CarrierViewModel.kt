package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.VehicleType
import com.efthemiosprime.pasabayan.data.model.Booking
import com.efthemiosprime.pasabayan.data.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * CarrierViewModel manages carrier operations, trips, and bookings
 * Mirrors iOS CarrierViewModel structure
 */
class CarrierViewModel : ViewModel() {
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _activeBookings = MutableStateFlow<List<Booking>>(emptyList())
    val activeBookings: StateFlow<List<Booking>> = _activeBookings.asStateFlow()
    
    private val _isCarrierProfileSetup = MutableStateFlow(true)
    val isCarrierProfileSetup: StateFlow<Boolean> = _isCarrierProfileSetup.asStateFlow()
    
    private val _isCarrierActive = MutableStateFlow(false)
    val isCarrierActive: StateFlow<Boolean> = _isCarrierActive.asStateFlow()
    
    private val _totalEarnings = MutableStateFlow("₱12,450")
    val totalEarnings: StateFlow<String> = _totalEarnings.asStateFlow()
    
    private val _averageRating = MutableStateFlow(4.8)
    val averageRating: StateFlow<Double> = _averageRating.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadMockData()
    }
    
    val carrierStatusText: String
        get() = if (_isCarrierActive.value) "Online" else "Offline"
    
    val averageRatingText: String
        get() = String.format("%.1f", _averageRating.value)
    
    private fun loadMockData() {
        // Mock trips data
        val mockTrips = listOf(
            Trip(
                id = 1,
                carrierId = 1,
                title = "Manila to Quezon City Route",
                description = "Regular delivery route",
                originLocation = "Manila",
                destinationLocation = "Quezon City",
                departureDate = "2024-01-15",
                departureTime = "08:00:00",
                arrivalDate = "2024-01-15",
                arrivalTime = "10:00:00",
                vehicleType = VehicleType.MOTORCYCLE,
                maxWeight = 20.0,
                pricePerKg = 50.0,
                status = TripStatus.PLANNED,
                createdAt = "2024-01-14T10:00:00Z",
                updatedAt = "2024-01-14T10:00:00Z"
            ),
            Trip(
                id = 2,
                carrierId = 1,
                title = "BGC to Makati Express",
                description = "Fast delivery for urgent packages",
                originLocation = "BGC Taguig",
                destinationLocation = "Makati CBD",
                departureDate = "2024-01-16",
                departureTime = "14:00:00",
                arrivalDate = "2024-01-16",
                arrivalTime = "15:30:00",
                vehicleType = VehicleType.CAR,
                maxWeight = 50.0,
                pricePerKg = 75.0,
                status = TripStatus.COMPLETED,
                createdAt = "2024-01-15T09:00:00Z",
                updatedAt = "2024-01-16T15:30:00Z"
            )
        )
        
        // Mock bookings data
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
        
        _trips.value = mockTrips
        _activeBookings.value = mockBookings
    }
    
    /**
     * Toggle carrier online/offline status
     */
    fun toggleCarrierStatus() {
        if (_isCarrierProfileSetup.value) {
            _isCarrierActive.value = !_isCarrierActive.value
        }
    }
    
    /**
     * Load carrier profile data
     */
    suspend fun loadCarrierProfile() {
        _isLoading.value = true
        delay(500)
        // Simulate loading carrier profile
        _isLoading.value = false
    }
    
    /**
     * Load carrier statistics
     */
    suspend fun loadCarrierStats() {
        _isLoading.value = true
        delay(500)
        // Simulate loading stats
        _totalEarnings.value = "₱${(10000..50000).random()}"
        _averageRating.value = (40..50).random() / 10.0
        _isLoading.value = false
    }
    
    /**
     * Load trips data
     */
    suspend fun loadTrips() {
        _isLoading.value = true
        delay(500)
        loadMockData()
        _isLoading.value = false
    }
    
    /**
     * Load active bookings
     */
    suspend fun loadActiveBookings() {
        _isLoading.value = true
        delay(500)
        // Filter active bookings
        _activeBookings.value = _activeBookings.value.filter { 
            it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        }
        _isLoading.value = false
    }
} 