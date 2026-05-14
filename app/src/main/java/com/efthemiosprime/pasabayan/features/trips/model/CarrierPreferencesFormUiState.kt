package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod

/**
 * Drives `CarrierPreferencesFormSheet`. iOS parity:
 * `pasabayan-ios/Pasabayan/Features/Profile/ViewModels/CarrierPreferencesFormViewModel.swift`.
 */
data class CarrierPreferencesFormUiState(
    val preferredPickupCityId: Int? = null,
    val preferredPickupCityText: String = "",
    val maxWeightKg: String = "",
    val maxSpaceLiters: String = "",
    val usualTransport: TransportationMethod = TransportationMethod.CAR,
    val hasExistingProfile: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
) {
    val isBusy: Boolean get() = isLoading || isSaving
}
