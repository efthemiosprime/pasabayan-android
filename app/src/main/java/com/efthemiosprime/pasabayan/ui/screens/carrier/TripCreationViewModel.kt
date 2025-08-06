package com.efthemiosprime.pasabayan.ui.screens.carrier

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.efthemiosprime.pasabayan.data.model.TransportationMethod
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl
import com.efthemiosprime.pasabayan.ui.screens.carrier.models.TripCreationUiState
import com.efthemiosprime.pasabayan.ui.screens.carrier.models.TripCreationEvent
import java.text.SimpleDateFormat
import java.util.*

/**
 * TripCreationViewModel - Functional state management for trip creation
 * Following functional programming patterns with immutable state and pure functions
 */
class TripCreationViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repository for API calls
    private val tripRepository = TripRepositoryImpl.create(getApplication())
    
    private val _uiState = MutableStateFlow(TripCreationUiState())
    val uiState: StateFlow<TripCreationUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = Channel<TripCreationEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    // Date formatter for ISO conversion
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // MARK: - Computed Properties (Pure Functions)
    val isFormValid: Boolean
        get() = with(uiState.value) {
            originCity.isNotBlank() && 
            destinationCity.isNotBlank() && 
            selectedTransportationMethod != null &&
            departureDate.isNotBlank() &&
            departureTime.isNotBlank() &&
            arrivalDate.isNotBlank() &&
            arrivalTime.isNotBlank() &&
            availableWeight.isNotBlank() && 
            pricePerKg.isNotBlank()
            // Note: availableSpace is optional
        }
    
    // MARK: - Form Field Updates (Pure Functions)
    
    fun updateOriginCity(value: String) {
        _uiState.update { it.copy(originCity = value) }
    }
    
    fun updateDestinationCity(value: String) {
        _uiState.update { it.copy(destinationCity = value) }
    }
    
    fun updateSelectedTransportationMethod(method: TransportationMethod?) {
        _uiState.update { it.copy(selectedTransportationMethod = method) }
    }
    
    fun updateDepartureDate(value: String) {
        _uiState.update { it.copy(departureDate = value) }
    }
    
    fun updateDepartureTime(value: String) {
        _uiState.update { it.copy(departureTime = value) }
    }
    
    fun updateArrivalDate(value: String) {
        _uiState.update { it.copy(arrivalDate = value) }
    }
    
    fun updateArrivalTime(value: String) {
        _uiState.update { it.copy(arrivalTime = value) }
    }
    
    fun updateAvailableWeight(value: String) {
        _uiState.update { it.copy(availableWeight = value) }
    }
    
    fun updateAvailableSpace(value: String) {
        _uiState.update { it.copy(availableSpace = value) }
    }
    
    fun updatePricePerKg(value: String) {
        _uiState.update { it.copy(pricePerKg = value) }
    }
    
    fun updateSpecialNotes(value: String) {
        _uiState.update { it.copy(specialNotes = value) }
    }
    
    fun updateShowTransportationDropdown(value: Boolean) {
        _uiState.update { it.copy(showTransportationDropdown = value) }
    }
    
    // MARK: - Trip Creation Logic
    
    fun createTrip() {
        val currentState = uiState.value
        
        // Validate form
        if (!isFormValid) {
            sendEvent(TripCreationEvent.ShowError("Please fill in all required fields"))
            return
        }
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                // Validate numeric inputs
                val weightKg = currentState.availableWeight.toDoubleOrNull()
                if (weightKg == null || weightKg <= 0) {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(TripCreationEvent.ShowError("Please enter a valid weight greater than 0"))
                    return@launch
                }
                
                val spaceLiters = if (currentState.availableSpace.isNotBlank()) {
                    currentState.availableSpace.toDoubleOrNull()?.takeIf { it > 0 } ?: 0.0
                } else 0.0
                
                val price = currentState.pricePerKg.toDoubleOrNull()
                if (price == null || price <= 0) {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(TripCreationEvent.ShowError("Please enter a valid price per kg greater than 0"))
                    return@launch
                }
                
                // Country detection logic
                val originCountry = detectCountry(currentState.originCity)
                val destinationCountry = detectCountry(currentState.destinationCity)
                
                // Coordinate lookup
                val (originLat, originLng) = getCoordinates(currentState.originCity)
                val (destinationLat, destinationLng) = getCoordinates(currentState.destinationCity)
                
                println("📍 Trip Creation Debug (Activity-style with schedule):")
                println("   Origin: ${currentState.originCity}, $originCountry ($originLat, $originLng)")
                println("   Destination: ${currentState.destinationCity}, $destinationCountry ($destinationLat, $destinationLng)")
                println("   Transportation: ${currentState.selectedTransportationMethod?.name?.lowercase()}")
                println("   Schedule: ${currentState.departureDate} ${currentState.departureTime} → ${currentState.arrivalDate} ${currentState.arrivalTime}")
                println("   Weight: ${weightKg}kg, Space: ${spaceLiters}L, Price: $${price}/kg")
                
                // Convert user input dates to ISO format for API
                val departureDateTimeISO = convertToISODateTime(currentState.departureDate, currentState.departureTime)
                val arrivalDateTimeISO = convertToISODateTime(currentState.arrivalDate, currentState.arrivalTime)
                
                // Validate that departure is in the future
                if (!isDepartureInFuture(departureDateTimeISO)) {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(TripCreationEvent.ShowError("Departure must be in the future. If selecting today's date, choose a time after the current time."))
                    return@launch
                }
                
                // Validate that arrival is after departure
                if (!validateDateTimeOrder(departureDateTimeISO, arrivalDateTimeISO)) {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(TripCreationEvent.ShowError("Arrival date/time must be after departure date/time"))
                    return@launch
                }
                
                // Create trip from form data
                val newTrip = Trip(
                    id = 0,
                    carrierId = 1,
                    originCity = currentState.originCity.trim(),
                    originCountry = originCountry,
                    originLat = originLat,
                    originLng = originLng,
                    destinationCity = currentState.destinationCity.trim(),
                    destinationCountry = destinationCountry,
                    destinationLat = destinationLat,
                    destinationLng = destinationLng,
                    departureDate = departureDateTimeISO,
                    arrivalDate = arrivalDateTimeISO,
                    availableWeightKg = weightKg,
                    availableSpaceLiters = spaceLiters,
                    pricePerKg = price,
                    tripStatus = TripStatus.SCHEDULED,
                    transportationMethod = currentState.selectedTransportationMethod!!,
                    specialNotes = if (currentState.specialNotes.isBlank()) null else currentState.specialNotes.trim()
                )
                
                tripRepository.createTrip(newTrip).collect { result ->
                    result.fold(
                        onSuccess = { createdTrip ->
                            println("✅ Trip created successfully - ID: ${createdTrip.id}")
                            _uiState.update { it.copy(isLoading = false) }
                            sendEvent(TripCreationEvent.ShowSuccess("Trip created successfully!"))
                            // Auto-navigate to Trips tab after 2 seconds
                            delay(2000)
                            sendEvent(TripCreationEvent.NavigateToTripsTab)
                        },
                        onFailure = { error ->
                            println("❌ Trip creation failed: ${error.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = error.message ?: "Failed to create trip"
                                )
                            }
                            sendEvent(TripCreationEvent.ShowError(error.message ?: "Failed to create trip"))
                        }
                    )
                }
            } catch (e: Exception) {
                println("❌ Exception during trip creation: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Invalid input: ${e.message}"
                    )
                }
                sendEvent(TripCreationEvent.ShowError("Invalid input: ${e.message}"))
            }
        }
    }
    
    fun clearForm() {
        _uiState.value = TripCreationUiState()
        sendEvent(TripCreationEvent.ClearForm)
    }
    
    // MARK: - Private Helper Functions
    
    private fun sendEvent(event: TripCreationEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
    
    private fun detectCountry(city: String): String {
        return when (city.trim().lowercase()) {
            "montreal", "toronto", "vancouver", "calgary", "ottawa" -> "Canada"
            "manila", "cebu", "davao", "makati", "quezon city", "bgc", "taguig", "pasig", "bacolod", "iloilo", "cagayan de oro", "baguio" -> "Philippines"
            "new york", "los angeles", "chicago", "houston", "phoenix", "philadelphia", "san antonio", "san diego", "dallas", "san jose" -> "United States"
            else -> "Philippines" // Default
        }
    }
    
    private fun getCoordinates(city: String): Pair<Double, Double> {
        return when (city.trim().lowercase()) {
            "montreal" -> Pair(45.5017, -73.5673)
            "toronto" -> Pair(43.6532, -79.3832)
            "vancouver" -> Pair(49.2827, -123.1207)
            "manila" -> Pair(14.5995, 120.9842)
            "cebu" -> Pair(10.3157, 123.8854)
            "makati" -> Pair(14.5547, 121.0244)
            "quezon city" -> Pair(14.6760, 121.0437)
            "bgc", "taguig" -> Pair(14.5176, 121.0509)
            "pasig" -> Pair(14.5764, 121.0851)
            "davao" -> Pair(7.1907, 125.4553)
            "bacolod" -> Pair(10.6319, 122.9951)
            "iloilo" -> Pair(10.7202, 122.5621)
            "cagayan de oro" -> Pair(8.4542, 124.6319)
            "baguio" -> Pair(16.4023, 120.5960)
            else -> Pair(14.5995, 120.9842) // Default to Manila
        }
    }
    
    private fun isDepartureInFuture(departureDateTime: String): Boolean {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val departureTime = isoFormat.parse(departureDateTime)
            val currentTime = Date()
            
            if (departureTime != null) {
                // More intelligent validation: allow any time on future dates
                val currentCalendar = Calendar.getInstance().apply { time = currentTime }
                val departureCalendar = Calendar.getInstance().apply { time = departureTime }
                
                // Check if departure is on a future date
                val currentDate = currentCalendar.get(Calendar.YEAR) * 10000 + 
                                currentCalendar.get(Calendar.MONTH) * 100 + 
                                currentCalendar.get(Calendar.DAY_OF_MONTH)
                val departureDate = departureCalendar.get(Calendar.YEAR) * 10000 + 
                                  departureCalendar.get(Calendar.MONTH) * 100 + 
                                  departureCalendar.get(Calendar.DAY_OF_MONTH)
                
                val isInFuture = if (departureDate > currentDate) {
                    // Future date - any time is valid
                    true
                } else if (departureDate == currentDate) {
                    // Same date - time must be in future
                    departureTime.after(currentTime)
                } else {
                    // Past date - invalid
                    false
                }
                
                val timeDiff = departureTime.time - currentTime.time
                val hoursDiff = timeDiff / (1000 * 60 * 60)
                
                println("🕐 Smart Future validation:")
                println("   Current: $currentTime (date: $currentDate)")
                println("   Departure: $departureTime (date: $departureDate)")
                println("   Is future date: ${departureDate > currentDate}")
                println("   Is same date: ${departureDate == currentDate}")
                println("   Is in future: $isInFuture")
                println("   Hours difference: $hoursDiff")
                
                isInFuture
            } else {
                println("❌ Date parsing failed during future validation")
                false
            }
        } catch (e: Exception) {
            println("❌ Future validation exception: ${e.message}")
            false
        }
    }
    
    private fun validateDateTimeOrder(departureDateTime: String, arrivalDateTime: String): Boolean {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val departureTime = isoFormat.parse(departureDateTime)
            val arrivalTime = isoFormat.parse(arrivalDateTime)
            
            if (departureTime != null && arrivalTime != null) {
                val isValid = arrivalTime.after(departureTime)
                println("📅 Date order validation:")
                println("   Departure: $departureDateTime ($departureTime)")
                println("   Arrival: $arrivalDateTime ($arrivalTime)")
                println("   Is valid: $isValid")
                isValid
            } else {
                println("❌ Date parsing failed during validation")
                false
            }
        } catch (e: Exception) {
            println("❌ Date validation exception: ${e.message}")
            false
        }
    }
    
    private fun convertToISODateTime(date: String, time: String): String {
        return try {
            // Parse user input (e.g., "Jul 12, 2025" and "9:30 PM")
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            
            val parsedDate = dateFormat.parse(date)
            val parsedTime = timeFormat.parse(time)
            
            if (parsedDate != null && parsedTime != null) {
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                
                val timeCalendar = Calendar.getInstance()
                timeCalendar.time = parsedTime
                
                // Combine date and time
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                // Format to ISO string in local timezone (no Z suffix)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formattedDateTime = isoFormat.format(calendar.time)
                
                println("🕐 Date conversion debug:")
                println("   Input: $date $time")
                println("   Parsed: ${calendar.time}")
                println("   ISO Format: $formattedDateTime")
                
                formattedDateTime
            } else {
                // Fallback to current time if parsing fails
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fallbackDateTime = isoFormat.format(Date())
                println("⚠️ Date parsing failed, using fallback: $fallbackDateTime")
                fallbackDateTime
            }
        } catch (e: Exception) {
            // Fallback to current time if parsing fails
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fallbackDateTime = isoFormat.format(Date())
            println("❌ Date conversion exception: ${e.message}")
            println("   Using fallback: $fallbackDateTime")
            fallbackDateTime
        }
    }
} 