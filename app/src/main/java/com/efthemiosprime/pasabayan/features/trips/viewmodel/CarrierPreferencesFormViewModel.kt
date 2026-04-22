package com.efthemiosprime.pasabayan.features.trips.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.features.trips.services.CarrierPreferencesFormStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CarrierPreferencesFormViewModel @Inject constructor(
    private val store: CarrierPreferencesFormStore,
) : ViewModel() {
    fun isAcknowledged(userId: Long): Boolean = store.isAcknowledged(userId)

    fun markAcknowledged(userId: Long) {
        store.markAcknowledged(userId)
    }
}
