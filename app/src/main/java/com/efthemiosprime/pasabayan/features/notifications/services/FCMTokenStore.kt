package com.efthemiosprime.pasabayan.features.notifications.services

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted FCM token (spec 08 — "FCM token received" lifecycle). iOS parity:
 * `FCMTokenStore`.
 */
interface FCMTokenStore {
    fun getToken(): String?
    fun setToken(token: String?)
    fun clear()
}

@Singleton
class SharedPreferencesFCMTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : FCMTokenStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getToken(): String? = prefs.getString(KEY_FCM_TOKEN, null)

    override fun setToken(token: String?) {
        prefs.edit().apply {
            if (token.isNullOrBlank()) remove(KEY_FCM_TOKEN) else putString(KEY_FCM_TOKEN, token)
            apply()
        }
    }

    override fun clear() {
        prefs.edit().remove(KEY_FCM_TOKEN).apply()
    }

    private companion object {
        const val PREFS_NAME = "pasabayan_notifications"
        const val KEY_FCM_TOKEN = "fcm_device_token"
    }
}
