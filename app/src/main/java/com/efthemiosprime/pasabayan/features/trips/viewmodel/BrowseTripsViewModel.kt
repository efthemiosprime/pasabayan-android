package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.model.TripCompatibilityResult
import com.efthemiosprime.pasabayan.features.trips.model.TripFilter
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.trips.services.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

private val BOOKABLE_STATUSES = setOf(TripStatus.PLANNING, TripStatus.ACTIVE)

data class BrowseTripsUiState(
    val availableTrips: List<Trip> = emptyList(),
    val popularRoutes: List<PopularRoute> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingPopularRoutes: Boolean = false,
    val errorMessage: String? = null,
    val popularRoutesErrorMessage: String? = null,
    val hasLoadedTrips: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val filter: TripFilter = TripFilter(),
    val selectedTrip: Trip? = null,
    val selectedPackage: PackageRequest? = null,
    val compatibilityResult: TripCompatibilityResult? = null,
    val isCheckingCompatibility: Boolean = false,
    val isBookingSheetPresented: Boolean = false,
    val isBookingTrip: Boolean = false,
    val bookingSuccessMessage: String? = null,
    val bookingErrorMessage: String? = null,
)

@HiltViewModel
class BrowseTripsViewModel @Inject constructor(
    private val tripsRepository: TripsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseTripsUiState())
    val uiState: StateFlow<BrowseTripsUiState> = _uiState.asStateFlow()

    fun loadAvailableTrips(reset: Boolean = true) {
        viewModelScope.launch {
            val current = _uiState.value
            val requestFilter = if (reset) current.filter.copy(page = 1) else current.filter
            _uiState.update {
                it.copy(
                    filter = requestFilter,
                    isLoading = reset,
                    isLoadingMore = !reset,
                    errorMessage = null,
                )
            }
            tripsRepository.loadAvailableTrips(requestFilter).fold(
                onSuccess = { trips ->
                    // Client-side filter: remove non-bookable statuses
                    val filtered = trips.filter { it.tripStatus in BOOKABLE_STATUSES }
                    _uiState.update {
                        val merged = if (reset) {
                            filtered
                        } else {
                            (it.availableTrips + filtered).distinctBy { trip -> trip.id }
                        }
                        it.copy(
                            availableTrips = merged,
                            isLoading = false,
                            isLoadingMore = false,
                            hasLoadedTrips = true,
                            currentPage = requestFilter.page,
                            hasMore = filtered.isNotEmpty(),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            hasLoadedTrips = true,
                            errorMessage = e.message ?: "Failed to load trips",
                        )
                    }
                },
            )
        }
    }

    fun refreshTrips() = loadAvailableTrips(reset = true)

    fun loadAvailableTripsWithFilter() = loadAvailableTrips(reset = true)

    fun submitSearch() = loadAvailableTripsWithFilter()

    fun clearSearch() {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(searchText = ""),
            )
        }
        loadAvailableTripsWithFilter()
    }

    fun loadMoreTrips() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        _uiState.update {
            it.copy(
                filter = it.filter.copy(page = it.currentPage + 1),
            )
        }
        loadAvailableTrips(reset = false)
    }

    fun loadPopularRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPopularRoutes = true, popularRoutesErrorMessage = null) }
            tripsRepository.loadPopularPackageRoutes().fold(
                onSuccess = { routes ->
                    _uiState.update {
                        it.copy(
                            popularRoutes = routes,
                            isLoadingPopularRoutes = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingPopularRoutes = false,
                            popularRoutesErrorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchText = text)) }
    }

    fun updateOrigin(text: String) {
        _uiState.update { it.copy(filter = it.filter.copy(origin = text)) }
    }

    fun updateDestination(text: String) {
        _uiState.update { it.copy(filter = it.filter.copy(destination = text)) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filter = TripFilter(),
                availableTrips = emptyList(),
                currentPage = 1,
                hasMore = true,
            )
        }
    }

    fun applyFilterAndFetch() {
        _uiState.update { it.copy(filter = it.filter.copy(page = 1)) }
        loadAvailableTrips(reset = true)
    }

    fun selectTrip(trip: Trip) {
        _uiState.update {
            it.copy(
                selectedTrip = trip,
                compatibilityResult = null,
                bookingErrorMessage = null,
            )
        }
        if (_uiState.value.selectedPackage != null) checkTripCompatibility()
    }

    fun selectPackage(packageRequest: PackageRequest) {
        _uiState.update {
            it.copy(
                selectedPackage = packageRequest,
                compatibilityResult = null,
                bookingErrorMessage = null,
            )
        }
        if (_uiState.value.selectedTrip != null) checkTripCompatibility()
    }

    fun checkTripCompatibility() {
        val current = _uiState.value
        val trip = current.selectedTrip ?: return
        val packageRequest = current.selectedPackage ?: return

        _uiState.update { it.copy(isCheckingCompatibility = true) }
        val compatibility = computeCompatibility(trip = trip, packageRequest = packageRequest)
        _uiState.update {
            it.copy(
                compatibilityResult = compatibility,
                isCheckingCompatibility = false,
            )
        }
    }

    fun showBookingSheet() {
        if (!canBook()) return
        _uiState.update {
            it.copy(
                isBookingSheetPresented = true,
                bookingErrorMessage = null,
            )
        }
    }

    fun hideBookingSheet() {
        _uiState.update {
            it.copy(
                isBookingSheetPresented = false,
            )
        }
    }

    fun bookTrip() {
        startBooking()
    }

    fun bookTripDirectly() {
        startBooking()
    }

    fun markBookingSuccess(message: String?) {
        _uiState.update {
            it.copy(
                isBookingTrip = false,
                isBookingSheetPresented = false,
                bookingSuccessMessage = message,
                bookingErrorMessage = null,
            )
        }
    }

    fun markBookingFailed(message: String?) {
        _uiState.update {
            it.copy(
                isBookingTrip = false,
                bookingSuccessMessage = null,
                bookingErrorMessage = message,
            )
        }
    }

    fun clearBookingState() {
        _uiState.update {
            it.copy(
                selectedTrip = null,
                selectedPackage = null,
                compatibilityResult = null,
                isCheckingCompatibility = false,
                isBookingSheetPresented = false,
                isBookingTrip = false,
                bookingSuccessMessage = null,
                bookingErrorMessage = null,
            )
        }
    }

    private fun startBooking() {
        if (!canBook()) return
        _uiState.update {
            it.copy(
                isBookingTrip = true,
                bookingErrorMessage = null,
                bookingSuccessMessage = null,
            )
        }
    }

    private fun canBook(): Boolean {
        val state = _uiState.value
        return state.selectedTrip != null &&
            state.selectedPackage != null &&
            state.compatibilityResult?.isCompatible == true
    }

    private fun computeCompatibility(
        trip: Trip,
        packageRequest: PackageRequest,
    ): TripCompatibilityResult {
        val routeCompatible = isRouteCompatible(trip = trip, packageRequest = packageRequest)
        val capacitySufficient = isCapacitySufficient(trip = trip, packageRequest = packageRequest)
        val dateCompatible = isDateCompatible(trip = trip, packageRequest = packageRequest)
        val priceCompatible = isPriceCompatible(trip = trip, packageRequest = packageRequest)
        return TripCompatibilityResult(
            routeCompatible = routeCompatible,
            capacitySufficient = capacitySufficient,
            dateCompatible = dateCompatible,
            priceCompatible = priceCompatible,
        )
    }

    private fun isRouteCompatible(
        trip: Trip,
        packageRequest: PackageRequest,
    ): Boolean {
        val pickupCity = packageRequest.pickupCity
        val deliveryCity = packageRequest.deliveryCity
        val originMatch = pickupCity.isNullOrBlank() || pickupCity.equals(trip.originCity, ignoreCase = true)
        val destinationMatch =
            deliveryCity.isNullOrBlank() || deliveryCity.equals(trip.destinationCity, ignoreCase = true)
        return originMatch && destinationMatch
    }

    private fun isCapacitySufficient(
        trip: Trip,
        packageRequest: PackageRequest,
    ): Boolean {
        val packageWeight = packageRequest.packageWeightKg ?: return true
        val tripWeight = trip.availableWeightKg ?: return true
        return tripWeight >= packageWeight
    }

    private fun isDateCompatible(
        trip: Trip,
        packageRequest: PackageRequest,
    ): Boolean {
        val pickupDate = packageRequest.pickupDatePreferred?.toInstantOrNull() ?: return true
        val departureDate = trip.departureDate?.toInstantOrNull() ?: return true
        val arrivalDate = trip.arrivalDate?.toInstantOrNull() ?: return true
        return pickupDate >= departureDate && pickupDate <= arrivalDate
    }

    private fun isPriceCompatible(
        trip: Trip,
        packageRequest: PackageRequest,
    ): Boolean {
        val budget = packageRequest.maxPriceBudget ?: return true
        val requestedWeight = packageRequest.packageWeightKg ?: 0.0
        return trip.estimatedPrice(forWeightKg = requestedWeight) <= budget
    }

    private fun String.toInstantOrNull(): Instant? = try {
        Instant.parse(this)
    } catch (_: Exception) {
        null
    }
}
