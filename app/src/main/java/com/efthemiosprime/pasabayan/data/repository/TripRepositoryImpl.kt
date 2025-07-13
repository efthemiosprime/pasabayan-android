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
import com.efthemiosprime.pasabayan.data.model.CreateTripRequestApi
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

/**
 * Trip repository implementation following functional programming patterns
 * Uses pure functions and immutable data operations
 */
class TripRepositoryImpl(
    private val apiService: APIService
) : TripRepository {
    
    override suspend fun getTripsForCarrier(carrierId: Int): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            println("🌐 Calling API: GET ${APIService.BASE_URL}/api/trips?carrier_id=$carrierId")
            try {
                val response = apiService.getCarrierTrips(carrierId)
                println("✅ API Success: Retrieved ${response.data.data.size} trips from page ${response.data.currentPage}")
                println("📊 Pagination: ${response.data.data.size}/${response.data.total} total trips")
                response.data.data
            } catch (e: Exception) {
                println("❌ API Error: ${e.message}")
                // Don't hide API errors - let them bubble up to show real status
                throw Exception("API Error: ${e.message ?: "Server is currently unavailable"}")
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch trips")))
    }
    
    override suspend fun getTripById(tripId: Int): Flow<Result<Trip>> = flow {
        emit(resultOf {
            try {
                val response = apiService.getTrip(tripId)
                response.data
            } catch (e: Exception) {
                throw Exception("Trip not found with ID: $tripId")
            }
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
            try {
                val response = apiService.getTrips()
                // Apply filters to the response data
                response.data.data.filter { trip ->
                    filterByOriginCity(trip, originCity) &&
                    filterByDestinationCity(trip, destinationCity) &&
                    filterByDepartureDate(trip, departureDate) &&
                    filterByMinWeight(trip, minWeight) &&
                    filterByMaxPrice(trip, maxPrice)
                }
            } catch (e: Exception) {
                // Return empty list if API fails
                emptyList()
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
                println("❌ Validation failed")
                throw Exception("Validation failed")
            }
            
            // Convert to API format
            val apiRequest = trip.toApiRequest()
            println("🌐 Calling API: POST ${APIService.BASE_URL}/api/trips")
            println("📤 Request: ${apiRequest.originCity} → ${apiRequest.destinationCity}")
            println("🚛 Method: ${apiRequest.transportationMethod}, Weight: ${apiRequest.availableWeightKg}kg")
            println("📋 Full Request Body:")
            println("   - originCity: ${apiRequest.originCity}")
            println("   - destinationCity: ${apiRequest.destinationCity}")  
            println("   - departureDate: ${apiRequest.departureDate}")
            println("   - transportationMethod: ${apiRequest.transportationMethod}")
            println("   - availableWeightKg: ${apiRequest.availableWeightKg}")
            println("   - availableSpaceLiters: ${apiRequest.availableSpaceLiters}")
            println("   - pricePerKg: ${apiRequest.pricePerKg}")
            
            try {
                val response = apiService.createTrip(apiRequest)
                println("✅ API Success: Trip created with ID ${response.data.id}")
                response.data
            } catch (e: Exception) {
                println("❌ API Error: ${e.message}")
                // Don't hide API errors - let them bubble up to show real status
                throw Exception("API Error: ${e.message ?: "Server is currently unavailable"}")
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to create trip")))
    }
    
    override suspend fun updateTrip(tripId: Int, trip: Trip): Flow<Result<Trip>> = flow {
        emit(resultOf {
            // Validate trip data first
            val validationResult = validateTrip(trip)
            if (validationResult.isFailure) {
                throw Exception("Validation failed")
            }
            
            try {
                val apiRequest = trip.toApiRequest()
                val response = apiService.updateTrip(tripId, apiRequest)
                response.data
            } catch (e: Exception) {
                // Return updated trip with ID if API fails
                trip.copy(id = tripId)
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.ValidationError(exception.message ?: "Failed to update trip")))
    }
    
    override suspend fun updateTripStatus(tripId: Int, status: TripStatus): Flow<Result<Trip>> = flow {
        emit(resultOf {
            try {
                // Get trip first, then update with new status
                val tripResponse = apiService.getTrip(tripId)
                val trip = tripResponse.data
                val updatedTrip = trip.updateStatus(status)
                
                // Update via API
                val apiRequest = updatedTrip.toApiRequest()
                val response = apiService.updateTrip(tripId, apiRequest)
                response.data
            } catch (e: Exception) {
                throw Exception("Trip not found with ID: $tripId")
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NotFoundError(exception.message ?: "Failed to update trip status")))
    }
    
    override suspend fun deleteTrip(tripId: Int): Flow<Result<Unit>> = flow {
        emit(resultOf {
            try {
                apiService.deleteTrip(tripId)
                Unit
            } catch (e: Exception) {
                throw Exception("Trip not found with ID: $tripId")
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NotFoundError(exception.message ?: "Failed to delete trip")))
    }
    
    override suspend fun getTripsByStatus(status: TripStatus): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            try {
                val response = apiService.getTrips()
                response.data.data.filter { it.tripStatus == status }
            } catch (e: Exception) {
                emptyList()
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch trips by status")))
    }
    
    /**
     * Get all trips from the API
     * Used for browse functionality to show all available trips
     */
    suspend fun getAllTrips(): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            println("🌐 Calling API: GET ${APIService.BASE_URL}/api/trips")
            try {
                val response = apiService.getTrips()
                println("✅ API Success: Retrieved ${response.data.data.size} trips from page ${response.data.currentPage}")
                println("📊 Pagination: ${response.data.data.size}/${response.data.total} total trips")
                response.data.data
            } catch (e: Exception) {
                println("❌ API Error: ${e.message}")
                throw Exception("API Error: ${e.message ?: "Server is currently unavailable"}")
            }
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.NetworkError(exception.message ?: "Failed to fetch trips")))
    }
    
    override suspend fun getActiveTrips(): Flow<Result<List<Trip>>> = flow {
        emit(resultOf {
            try {
                val response = apiService.getTrips()
                response.data.data.filter { it.isActive || it.isScheduled }
            } catch (e: Exception) {
                emptyList()
            }
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
    
    private fun generateNewTripId(): Int = (1..999999).random()
    
    companion object {
        private const val TAG = "TripRepositoryImpl"
        
        /**
         * Factory method to create TripRepositoryImpl with APIService
         */
        fun create(context: Context): TripRepositoryImpl {
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
                    
                    // Add authentication header if available
                    try {
                        val authService = AuthService.getInstance(context)
                        val token = kotlinx.coroutines.runBlocking { 
                            authService.getToken()
                        }
                        val isAuthenticated = authService.isAuthenticated.value
                        val currentUser = authService.currentUser.value
                        
                        println("🔍 Auth Status Check:")
                        println("   - Is Authenticated: $isAuthenticated")
                        println("   - Current User: ${currentUser?.name ?: "null"}")
                        println("   - User Types: ${currentUser?.userTypes ?: "null"}")
                        println("   - Is Carrier: ${currentUser?.isCarrier ?: false}")
                        println("   - Token Available: ${token != null}")
                        
                        if (token != null) {
                            println("🔑 Adding auth token: Bearer ${token.take(20)}...")
                            requestBuilder.addHeader("Authorization", "Bearer $token")
                        } else {
                            println("⚠️ No auth token available - user needs to login first")
                        }
                    } catch (e: Exception) {
                        println("❌ Auth token error: ${e.message}")
                    }
                    
                    requestBuilder.addHeader("Accept", "application/json")
                    requestBuilder.addHeader("Content-Type", "application/json")
                    
                    chain.proceed(requestBuilder.build())
                }
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl("${APIService.BASE_URL}/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            
            val apiService = retrofit.create(APIService::class.java)
            return TripRepositoryImpl(apiService)
        }
    }
}

/**
 * Extension method to convert Trip to API request format
 * Uses ISO datetime format matching backend requirements
 */
private fun Trip.toApiRequest(): CreateTripRequestApi {
    try {
        // Handle empty dates - use defaults if not provided
        val finalDepartureDate = if (departureDate.isEmpty()) {
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            isoFormat.format(tomorrow.time)
        } else {
            departureDate // Already in ISO format from ViewModel
        }
        
        val finalArrivalDate = if (arrivalDate.isEmpty()) {
            val dayAfter = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            isoFormat.format(dayAfter.time)
        } else {
            arrivalDate // Already in ISO format from ViewModel
        }
        
        println("🔄 Converting Trip to API Request:")
        println("   - Origin: $originCity, $originCountry")
        println("   - Destination: $destinationCity, $destinationCountry")
        println("   - Departure: $finalDepartureDate")
        println("   - Arrival: $finalArrivalDate")
        println("   - Transportation Method: ${transportationMethod.name} → ${transportationMethod.name.lowercase()}")
        println("   - Weight: ${availableWeightKg}kg, Space: ${availableSpaceLiters}L, Price: $${pricePerKg}/kg")
        println("   - Special Notes: ${specialNotes ?: "None"}")
        
        return CreateTripRequestApi(
            originCity = originCity,
            originCountry = originCountry,
            destinationCity = destinationCity,
            destinationCountry = destinationCountry,
            departureDate = finalDepartureDate,
            arrivalDate = finalArrivalDate,
            availableWeightKg = availableWeightKg,
            availableSpaceLiters = availableSpaceLiters,
            pricePerKg = pricePerKg,
            transportationMethod = transportationMethod.name.lowercase(),
            specialNotes = specialNotes
        )
    } catch (e: Exception) {
        println("❌ API request conversion error: ${e.message}")
        throw Exception("Failed to create trip request: ${e.message}")
    }
}

 