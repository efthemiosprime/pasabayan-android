package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.features.trips.services.TripsLocalStateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class TripsLocalStateViewModel @Inject constructor(
    private val tripsLocalStateUpdater: TripsLocalStateUpdater,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    /**
     * Bridges the existing `TripsLocalStateUpdater` pending-sync gate to the actual
     * `POST /api/profile/disclaimer-acknowledgments` call. The store's `pendingSync` flag stays
     * the single source of truth for whether we still owe the server an ack.
     */
    fun retryCarrierDisclaimerPendingSync(userId: Long) {
        viewModelScope.launch {
            tripsLocalStateUpdater.retryPendingCarrierDisclaimerSync(userId) {
                profileRepository.acknowledgeDisclaimer(CARRIER_TRIP).map { }
            }
        }
    }

    private companion object {
        const val CARRIER_TRIP = "carrier_trip"
    }
}
