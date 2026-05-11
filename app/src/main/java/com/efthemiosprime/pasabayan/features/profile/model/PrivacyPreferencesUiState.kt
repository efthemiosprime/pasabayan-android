package com.efthemiosprime.pasabayan.features.profile.model

data class PrivacyPreferencesUiState(
    val pushNotifications: Boolean = false,
    val locationTracking: Boolean = false,
    val analytics: Boolean = false,
    val marketingCommunications: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingDisable: ConsentPreference? = null,
) {
    fun valueFor(preference: ConsentPreference): Boolean = when (preference) {
        ConsentPreference.PUSH_NOTIFICATIONS -> pushNotifications
        ConsentPreference.LOCATION_TRACKING -> locationTracking
        ConsentPreference.ANALYTICS -> analytics
        ConsentPreference.MARKETING -> marketingCommunications
    }
}
