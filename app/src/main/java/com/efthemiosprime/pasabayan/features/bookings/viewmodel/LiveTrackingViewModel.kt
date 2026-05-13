package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.bookings.model.CarrierLocationSnapshot
import com.efthemiosprime.pasabayan.features.bookings.model.TrackingMath
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/**
 * UI state for the live-tracking sheet.
 *
 * The pickup/delivery code fields predate live tracking; they remain here
 * because the same VM owns code-confirmation actions. The map / distance /
 * ETA fields mirror iOS `LiveTrackingViewModel`:
 *
 *  - [carrierLat] / [carrierLng] are null until the carrier broadcasts.
 *  - [deliveryLat] / [deliveryLng] come from the snapshot's
 *    `delivery_address` (String wire format, parsed via `toDoubleOrNull`).
 *  - [remainingDistanceKm] is the great-circle distance carrier → delivery.
 *  - [totalDistanceKm] captures the *initial* distance so progress can be
 *    computed; once set it does not shrink. iOS uses `max(remaining, 5.0)`
 *    as a display floor — we mirror that.
 *  - [estimatedMinutes] is the ETA at 40 km/h with a 1-minute floor.
 *  - [isStale] reflects the server flag when present, else the local
 *    10-minute window.
 */
data class LiveTrackingUiState(
    val pickupCode: String? = null,
    val deliveryCode: String? = null,
    val isGeneratingCode: Boolean = false,
    val codeVerified: Boolean = false,
    val errorMessage: String? = null,
    val isLoadingLocation: Boolean = false,
    val carrierLat: Double? = null,
    val carrierLng: Double? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
    val remainingDistanceKm: Double = 0.0,
    val totalDistanceKm: Double = 0.0,
    val deliveryProgress: Double = 0.0,
    val estimatedMinutes: Int = 0,
    val lastUpdatedAt: String? = null,
    val isStale: Boolean = false,
) {
    val hasCarrierLocation: Boolean
        get() {
            val lat = carrierLat ?: return false
            val lng = carrierLng ?: return false
            return lat != 0.0 || lng != 0.0
        }
}

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTrackingUiState())
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    private var clock: Clock = Clock.systemUTC()

    /** Test seam — override the clock used for local staleness detection. */
    @VisibleForTesting
    internal fun overrideClock(clock: Clock) {
        this.clock = clock
    }

    fun generatePickupCode(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, errorMessage = null) }
            bookingsRepository.generatePickupCode(matchId).fold(
                onSuccess = { code ->
                    _uiState.update { it.copy(pickupCode = code, isGeneratingCode = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isGeneratingCode = false, errorMessage = e.message ?: "Failed to generate code")
                    }
                },
            )
        }
    }

    fun generateDeliveryCode(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, errorMessage = null) }
            bookingsRepository.generateDeliveryCode(matchId).fold(
                onSuccess = { code ->
                    _uiState.update { it.copy(deliveryCode = code, isGeneratingCode = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isGeneratingCode = false, errorMessage = e.message ?: "Failed to generate code")
                    }
                },
            )
        }
    }

    fun confirmPickupWithCode(matchId: Int, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, codeVerified = false) }
            bookingsRepository.confirmPickupWithCode(matchId, code).fold(
                onSuccess = { _uiState.update { it.copy(codeVerified = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Invalid code") }
                },
            )
        }
    }

    fun confirmDeliveryWithCode(matchId: Int, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, codeVerified = false) }
            bookingsRepository.confirmDeliveryWithCode(matchId, code).fold(
                onSuccess = { _uiState.update { it.copy(codeVerified = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Invalid code") }
                },
            )
        }
    }

    /**
     * Fetch the latest carrier-location snapshot for [matchId] and recompute
     * distance / ETA / stale-flag. Parity with iOS
     * `LiveTrackingViewModel.fetchCarrierLocation`.
     */
    fun refreshLocation(matchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, errorMessage = null) }
            bookingsRepository.getCarrierLocation(matchId).fold(
                onSuccess = { snapshot -> applySnapshot(snapshot) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            errorMessage = e.message ?: "Failed to load location",
                        )
                    }
                },
            )
        }
    }

    fun clearState() {
        _uiState.update { LiveTrackingUiState() }
    }

    private fun applySnapshot(snapshot: CarrierLocationSnapshot) {
        val deliveryLat = snapshot.deliveryLat ?: _uiState.value.deliveryLat
        val deliveryLng = snapshot.deliveryLng ?: _uiState.value.deliveryLng
        val carrierLat = snapshot.carrierLat
        val carrierLng = snapshot.carrierLng

        val remainingKm = TrackingMath.distanceKm(carrierLat, carrierLng, deliveryLat, deliveryLng)
            ?: 0.0
        val previousTotal = _uiState.value.totalDistanceKm
        // iOS captures the initial distance and uses max(remaining, 5.0) as a display floor.
        val totalKm = if (previousTotal == 0.0) maxOf(remainingKm, 5.0) else previousTotal
        val etaMinutes = if (carrierLat == null || carrierLng == null) 0
        else TrackingMath.etaMinutes(remainingKm) ?: 0
        val progress = TrackingMath.progress(remainingKm, totalKm)
        val stale = TrackingMath.isStale(
            lastUpdatedAt = snapshot.lastUpdatedAt,
            now = clock.instant(),
            serverIsStale = snapshot.isStale,
        )

        _uiState.update {
            it.copy(
                isLoadingLocation = false,
                errorMessage = null,
                carrierLat = carrierLat,
                carrierLng = carrierLng,
                deliveryLat = deliveryLat,
                deliveryLng = deliveryLng,
                remainingDistanceKm = remainingKm,
                totalDistanceKm = totalKm,
                deliveryProgress = progress,
                estimatedMinutes = etaMinutes,
                lastUpdatedAt = snapshot.lastUpdatedAt,
                isStale = stale,
            )
        }
    }
}
