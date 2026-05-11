package com.efthemiosprime.pasabayan.features.profile.model

data class SettingsUiState(
    val currency: CurrencyPreference = CurrencyPreference.Default,
    val isClearingCache: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
)
