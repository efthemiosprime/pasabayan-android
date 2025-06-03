package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.Booking
import com.efthemiosprime.pasabayan.data.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * CarrierViewModel manages carrier operations, trips, and bookings
 * Mirrors iOS CarrierViewModel structure with exact Trip model
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
        // Use the exact mock trips from iOS Trip.swift
        _trips.value = Trip.mockTrips
        
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
     * Load trips data (matching iOS implementation)
     */
    suspend fun loadTrips() {
        _isLoading.value = true
        delay(500)
        // Reload mock data to simulate API call
        _trips.value = Trip.mockTrips
        _isLoading.value = false
    }
    
    /**
     * Refresh trips data (for pull-to-refresh)
     */
    fun refreshTrips() {
        _trips.value = Trip.mockTrips
    }
    
    /**
     * Load active bookings
     */
    suspend fun loadActiveBookings() {
        _isLoading.value = true
        delay(500)
        // Simulate loading active bookings
        _isLoading.value = false
    }
    
    /**
     * Get trips filtered by status
     */
    fun getTripsFilteredBy(status: TripStatus?): List<Trip> {
        return if (status == null) {
            _trips.value
        } else {
            _trips.value.filter { it.tripStatus == status }
        }
    }
    
    /**
     * Get trip count by status
     */
    fun getTripCountBy(status: TripStatus): Int {
        return _trips.value.count { it.tripStatus == status }
    }
} 