package com.efthemiosprime.pasabayan.features.notifications.services

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records whether the user has *intentionally* opted in to push notifications, so the
 * auto-consent retry flow can be replayed across app launches if the server returns
 * 403 `consent_required` again. Mirrors iOS `PushNotificationsConsentIntent`.
 */
interface PushNotificationsConsentIntent {
    fun isOptedIn(): Boolean
    fun setOptedIn(value: Boolean)
}

@Singleton
class SharedPreferencesPushNotificationsConsentIntent @Inject constructor(
    @ApplicationContext context: Context,
) : PushNotificationsConsentIntent {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isOptedIn(): Boolean = prefs.getBoolean(KEY_OPTED_IN, false)

    override fun setOptedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_OPTED_IN, value).apply()
    }

    private companion object {
        const val PREFS_NAME = "pasabayan_notifications"
        const val KEY_OPTED_IN = "push_notifications_consent_intent"
    }
}
