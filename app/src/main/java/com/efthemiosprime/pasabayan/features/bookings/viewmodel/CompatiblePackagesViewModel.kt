package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the "compatible packages for trip" carrier flow.
 *
 * iOS parity: `CompatiblePackagesForTripView` (Features/Bookings/Views/Carrier).
 * Backed by `GET /trips/{tripId}/compatible-packages`.
 */
data class CompatiblePackagesUiState(
    val tripId: Int? = null,
    val packages: List<PackageRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
)

@HiltViewModel
class CompatiblePackagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatiblePackagesUiState())
    val uiState: StateFlow<CompatiblePackagesUiState> = _uiState.asStateFlow()

    /**
     * Load packages compatible with [tripId]. No-op when already loaded for
     * the same trip unless [force] is true. Changing [tripId] always reloads.
     */
    fun loadCompatiblePackages(tripId: Int, force: Boolean = false) {
        val current = _uiState.value
        if (!force && current.hasLoaded && current.tripId == tripId) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(tripId = tripId, isLoading = true, errorMessage = null)
            }
            bookingsRepository.getCompatiblePackages(tripId).fold(
                onSuccess = { packages ->
                    _uiState.update {
                        it.copy(
                            tripId = tripId,
                            packages = packages,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            tripId = tripId,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = e.message
                                ?: context.getString(R.string.bookings_compatible_packages_error_load),
                        )
                    }
                },
            )
        }
    }

    fun refresh() {
        val tripId = _uiState.value.tripId ?: return
        loadCompatiblePackages(tripId, force = true)
    }
}
