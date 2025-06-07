package com.efthemiosprime.pasabayan.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.delay
import com.efthemiosprime.pasabayan.domain.repository.TripRepository
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.data.common.resultOf

/**
 * Trip repository implementation following functional programming patterns
 * Uses pure functions and immutable data operations
 */
class TripRepositoryImpl : TripRepository {
    
    // Mock data source - in real app this would be API service
    private val mockTrips = Trip.getMockTrips().toMutableList()
    
    override suspend fun getTripsForCarrier(carrierId: Int): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            delay(500) // Simulate network delay
            mockTrips.filter { it.carrierId == carrierId }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch trips")))
    }
    
    override suspend fun getTripById(tripId: Int): Flow<Result<Trip>> = flow {
        emit(resultOf {
            delay(300) // Simulate network delay
            mockTrips.find { it.id == tripId } 
                ?: throw Exception("Trip not found with ID: $tripId")
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NotFoundError(exception.message ?: "Trip not found")))
    }
    
    override suspend fun searchTrips(
        originCity: String?,
        destinationCity: String?,
        departureDate: String?,
        minWeight: Double?,
        maxPrice: Double?
    ): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            delay(400) // Simulate network delay
            
            // Functional filtering using pure functions
            mockTrips.filter { trip ->
                filterByOriginCity(trip, originCity) &&
                filterByDestinationCity(trip, destinationCity) &&
                filterByDepartureDate(trip, departureDate) &&
                filterByMinWeight(trip, minWeight) &&
                filterByMaxPrice(trip, maxPrice)
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Search failed")))
    }
    
    override suspend fun createTrip(trip: Trip): Flow<Result<Trip>> = flow {
        emit(resultOf {
            // Validate trip data first
            val validationResult = validateTrip(trip)
            if (validationResult.isFailure) {
                throw Exception("Validation failed")
            }
            
            delay(600) // Simulate network delay
            
            // Create new trip with generated ID (functional approach)
            val newTrip = trip.copy(id = generateNewTripId())
            mockTrips.add(newTrip)
            newTrip
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.ValidationError(exception.message ?: "Failed to create trip")))
    }
    
    override suspend fun updateTrip(tripId: Int, trip: Trip): Flow<Result<Trip>> = flow {
        emit(resultOf {
            // Validate trip data first
            val validationResult = validateTrip(trip)
            if (validationResult.isFailure) {
                throw Exception("Validation failed")
            }
            
            delay(500) // Simulate network delay
            
            val index = mockTrips.indexOfFirst { it.id == tripId }
            if (index == -1) {
                throw Exception("Trip not found with ID: $tripId")
            }
            
            // Functional update - create new trip with updated data
            val updatedTrip = trip.copy(id = tripId)
            mockTrips[index] = updatedTrip
            updatedTrip
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.ValidationError(exception.message ?: "Failed to update trip")))
    }
    
    override suspend fun updateTripStatus(tripId: Int, status: TripStatus): Flow<Result<Trip>> = flow {
        emit(resultOf {
            delay(300) // Simulate network delay
            
            val index = mockTrips.indexOfFirst { it.id == tripId }
            if (index == -1) {
                throw Exception("Trip not found with ID: $tripId")
            }
            
            // Functional update using immutable copy
            val updatedTrip = mockTrips[index].updateStatus(status)
            mockTrips[index] = updatedTrip
            updatedTrip
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NotFoundError(exception.message ?: "Failed to update trip status")))
    }
    
    override suspend fun deleteTrip(tripId: Int): Flow<Result<Unit>> = flow {
        emit(resultOf {
            delay(400) // Simulate network delay
            
            val removed = mockTrips.removeIf { it.id == tripId }
            if (!removed) {
                throw Exception("Trip not found with ID: $tripId")
            }
            Unit
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NotFoundError(exception.message ?: "Failed to delete trip")))
    }
    
    override suspend fun getTripsByStatus(status: TripStatus): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            delay(300) // Simulate network delay
            mockTrips.filter { it.tripStatus == status }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch trips by status")))
    }
    
    override suspend fun getActiveTrips(): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            delay(300) // Simulate network delay
            mockTrips.filter { it.isActive || it.isScheduled }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch active trips")))
    }
    
    override fun validateTrip(trip: Trip): Result<Trip> = resultOf {
        val validationResult = trip.validate()
        if (validationResult.isInvalid) {
            throw Exception("Trip validation failed: ${validationResult.errorList().joinToString(", ")}")
        }
        trip
    }
    
    override fun canAcceptBooking(trip: Trip, requiredWeight: Double, requiredSpace: Double): Result<Boolean> = resultOf {
        val validationResult = trip.canAcceptBooking(requiredWeight, requiredSpace)
        if (validationResult.isInvalid) {
            throw Exception("Booking validation failed: ${validationResult.errorList().joinToString(", ")}")
        }
        true
    }
    
    // MARK: - Pure Helper Functions
    
    private fun filterByOriginCity(trip: Trip, originCity: String?): Boolean =
        originCity?.let { trip.originCity.contains(it, ignoreCase = true) } ?: true
    
    private fun filterByDestinationCity(trip: Trip, destinationCity: String?): Boolean =
        destinationCity?.let { trip.destinationCity.contains(it, ignoreCase = true) } ?: true
    
    private fun filterByDepartureDate(trip: Trip, departureDate: String?): Boolean =
        departureDate?.let { trip.departureDate.startsWith(it) } ?: true
    
    private fun filterByMinWeight(trip: Trip, minWeight: Double?): Boolean =
        minWeight?.let { trip.availableWeightKg >= it } ?: true
    
    private fun filterByMaxPrice(trip: Trip, maxPrice: Double?): Boolean =
        maxPrice?.let { trip.pricePerKg <= it } ?: true
    
    private fun generateNewTripId(): Int = (mockTrips.maxOfOrNull { it.id } ?: 0) + 1
} 