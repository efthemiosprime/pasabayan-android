package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the shipper-side "compatible trips for package" flow.
 *
 * iOS parity: `CompatibleTripsView` (Features/Bookings/Views/Shipper).
 * Backed by `GET /packages/{packageRequestId}/compatible-trips`.
 */
data class CompatibleTripsUiState(
    val packageRequestId: Int? = null,
    val carrierIdFilter: Int? = null,
    val trips: List<CompatibleTrip> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
)

@HiltViewModel
class CompatibleTripsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibleTripsUiState())
    val uiState: StateFlow<CompatibleTripsUiState> = _uiState.asStateFlow()

    /**
     * Load trips compatible with [packageRequestId]. Optionally restricts to a single
     * [carrierId] (the favorite-carrier-from-popover entry point on iOS). No-op when
     * the same package/carrier combo is already loaded unless [force] is true.
     */
    fun loadCompatibleTrips(
        packageRequestId: Int,
        carrierId: Int? = null,
        force: Boolean = false,
    ) {
        val current = _uiState.value
        val sameQuery = current.packageRequestId == packageRequestId &&
            current.carrierIdFilter == carrierId
        if (!force && current.hasLoaded && sameQuery) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    packageRequestId = packageRequestId,
                    carrierIdFilter = carrierId,
                    isLoading = true,
                    errorMessage = null,
                )
            }
            bookingsRepository.getCompatibleTrips(packageRequestId, carrierId).fold(
                onSuccess = { trips ->
                    _uiState.update {
                        it.copy(
                            trips = trips,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = e.message
                                ?: context.getString(R.string.bookings_compatible_trips_error_load),
                        )
                    }
                },
            )
        }
    }

    fun refresh() {
        val state = _uiState.value
        val packageRequestId = state.packageRequestId ?: return
        loadCompatibleTrips(
            packageRequestId = packageRequestId,
            carrierId = state.carrierIdFilter,
            force = true,
        )
    }
}
