package com.efthemiosprime.pasabayan.features.notifications.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Idempotently registers the foreground notification channel. Spec 08 — "Foreground notification
 * display": iOS shows `[.banner, .badge, .sound]`; Android requires a channel with importance
 * HIGH so heads-up is allowed.
 */
@Singleton
class NotificationChannelSetup @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return
        if (manager.getNotificationChannel(CHANNEL_ID_GENERAL) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            "Pasabayan",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Delivery, chat, and payment updates"
            enableLights(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID_GENERAL = "pasabayan_general"
    }
}
