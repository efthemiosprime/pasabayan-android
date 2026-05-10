package com.efthemiosprime.pasabayan.features.profile.model

/**
 * `preferred_contact_method` enum per `09-profile-carrier-consent` § Smart defaults.
 * String value matches the backend wire format.
 */
enum class ContactMethod(val raw: String) {
    PHONE("phone"),
    EMAIL("email"),
    APP_NOTIFICATION("app_notification"),
    ;

    companion object {
        fun fromRaw(raw: String?): ContactMethod =
            entries.firstOrNull { it.raw == raw } ?: APP_NOTIFICATION
    }
}
