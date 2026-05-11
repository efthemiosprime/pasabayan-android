package com.efthemiosprime.pasabayan.features.profile.model

import com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson

/**
 * UI state for `EditCarrierProfileSheet`. Numeric fields are plain strings so we can show what the
 * user has typed (and surface validation errors per field). Server values use [CarrierProfileJson]
 * loaded on initialize and re-read on save to support the create-vs-update branching.
 */
data class EditCarrierProfileUiState(
    val maxWeightKg: String = "",
    val maxSpaceLiters: String = "",
    val pricePerKgCad: String = "",
    val insuranceCoverageCad: String = "",
    val bio: String = "",
    val selectedPackageTypes: Set<String> = emptySet(),
    val selectedRestrictedItems: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isInitialized: Boolean = false,
    val hasExistingProfile: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val fieldErrors: Set<EditCarrierProfileField> = emptySet(),
) {
    fun fieldHasError(field: EditCarrierProfileField): Boolean = field in fieldErrors
    val isFormBusy: Boolean get() = isLoading || isSaving
}

enum class EditCarrierProfileField {
    MAX_WEIGHT,
    MAX_SPACE,
    PRICE,
    INSURANCE,
}
