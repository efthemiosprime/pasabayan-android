package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.trips.services.TripsLocalStateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class TripsLocalStateViewModel @Inject constructor(
    private val tripsLocalStateUpdater: TripsLocalStateUpdater,
) : ViewModel() {
    fun retryCarrierDisclaimerPendingSync(userId: Long) {
        viewModelScope.launch {
            // TODO: replace placeholder success with API disclaimer sync call.
            tripsLocalStateUpdater.retryPendingCarrierDisclaimerSync(userId) {
                Result.success(Unit)
            }
        }
    }
}
