package com.efthemiosprime.pasabayan.features.profile.model

data class EditUserProfileUiState(
    val fullName: String = "",
    val deliveryAddress: String = "",
    val contactMethod: ContactMethod = ContactMethod.APP_NOTIFICATION,
    val timezone: String = SupportedTimezones.first(),
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isFullNameValid: Boolean get() = fullName.trim().isNotEmpty()
    val isFormValid: Boolean get() = isFullNameValid && !isLoading
}

/**
 * Subset of iOS timezone options ([EditUserProfileSheet]). The system timezone is appended if
 * missing so callers always have a valid pre-fill.
 */
val SupportedTimezones: List<String> = listOf(
    "Asia/Manila",
    "Asia/Tokyo",
    "Asia/Singapore",
    "America/Los_Angeles",
    "America/New_York",
    "America/St_Johns",
    "America/Halifax",
    "America/Toronto",
    "America/Winnipeg",
    "America/Regina",
    "America/Edmonton",
    "America/Vancouver",
    "America/Whitehorse",
)
