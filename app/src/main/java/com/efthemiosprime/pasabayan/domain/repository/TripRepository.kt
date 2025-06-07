package com.efthemiosprime.pasabayan.domain.repository

import kotlinx.coroutines.flow.Flow
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.LoadingState

/**
 * Trip repository interface following functional programming patterns
 * Provides pure functions for trip data operations
 */
interface TripRepository {
    
    /**
     * Get all trips for a carrier (pure function)
     */
    suspend fun getTripsForCarrier(carrierId: Int): Flow<Result<List<Trip>>>
    
    /**
     * Get trip by ID (pure function)
     */
    suspend fun getTripById(tripId: Int): Flow<Result<Trip>>
    
    /**
     * Search trips by criteria (pure function)
     */
    suspend fun searchTrips(
        originCity: String? = null,
        destinationCity: String? = null,
        departureDate: String? = null,
        minWeight: Double? = null,
        maxPrice: Double? = null
    ): Flow<Result<List<Trip>>>
    
    /**
     * Create new trip (functional with validation)
     */
    suspend fun createTrip(trip: Trip): Flow<Result<Trip>>
    
    /**
     * Update trip (functional with validation)
     */
    suspend fun updateTrip(tripId: Int, trip: Trip): Flow<Result<Trip>>
    
    /**
     * Update trip status (functional)
     */
    suspend fun updateTripStatus(tripId: Int, status: TripStatus): Flow<Result<Trip>>
    
    /**
     * Delete trip (functional)
     */
    suspend fun deleteTrip(tripId: Int): Flow<Result<Unit>>
    
    /**
     * Get trips by status (pure function)
     */
    suspend fun getTripsByStatus(status: TripStatus): Flow<Result<List<Trip>>>
    
    /**
     * Get active trips (pure function)
     */
    suspend fun getActiveTrips(): Flow<Result<List<Trip>>>
    
    /**
     * Validate trip data (pure function)
     */
    fun validateTrip(trip: Trip): Result<Trip>
    
    /**
     * Check if trip can accept booking (pure function)
     */
    fun canAcceptBooking(trip: Trip, requiredWeight: Double, requiredSpace: Double): Result<Boolean>
} 