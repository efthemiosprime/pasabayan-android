package com.efthemiosprime.pasabayan.features.profile.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * Four consent purposes per `09-profile-carrier-consent` § Consent preferences. [raw] matches the
 * backend JSON key (e.g. `push_notifications`). [requiresDisableConfirm] surfaces a confirmation
 * alert before turning the toggle OFF — push and location both need it.
 */
enum class ConsentPreference(
    val raw: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val disableConfirmRes: Int?,
    val requiresDisableConfirm: Boolean,
) {
    PUSH_NOTIFICATIONS(
        raw = "push_notifications",
        labelRes = R.string.profile_privacy_push_label,
        descriptionRes = R.string.profile_privacy_push_description,
        disableConfirmRes = R.string.profile_privacy_push_disable_message,
        requiresDisableConfirm = true,
    ),
    LOCATION_TRACKING(
        raw = "location_tracking",
        labelRes = R.string.profile_privacy_location_label,
        descriptionRes = R.string.profile_privacy_location_description,
        disableConfirmRes = R.string.profile_privacy_location_disable_message,
        requiresDisableConfirm = true,
    ),
    ANALYTICS(
        raw = "analytics",
        labelRes = R.string.profile_privacy_analytics_label,
        descriptionRes = R.string.profile_privacy_analytics_description,
        disableConfirmRes = null,
        requiresDisableConfirm = false,
    ),
    MARKETING(
        raw = "marketing_communications",
        labelRes = R.string.profile_privacy_marketing_label,
        descriptionRes = R.string.profile_privacy_marketing_description,
        disableConfirmRes = null,
        requiresDisableConfirm = false,
    ),
    ;

    companion object {
        fun fromRaw(raw: String): ConsentPreference? = entries.firstOrNull { it.raw == raw }
    }
}
