package com.efthemiosprime.pasabayan.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.profile.services.DisclaimerSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Top-level hook for `MainTabScreen` to (1) reconcile local disclaimer flags with the server on
 * app launch and (2) retry any pending acknowledgments that previously failed to sync. Per-feature
 * VMs (`PackageCreationAssistViewModel`, `TripsLocalStateViewModel`) own the **immediate**
 * acknowledgment paths.
 */
@HiltViewModel
class DisclaimerSyncViewModel @Inject constructor(
    private val service: DisclaimerSyncService,
) : ViewModel() {

    fun bootstrapAndRetry(userId: Long) {
        viewModelScope.launch {
            service.bootstrapAcknowledgments(userId)
            service.retryPendingSyncs(userId)
        }
    }
}
