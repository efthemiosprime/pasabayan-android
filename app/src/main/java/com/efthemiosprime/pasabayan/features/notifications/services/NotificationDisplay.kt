package com.efthemiosprime.pasabayan.features.notifications.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.efthemiosprime.pasabayan.MainActivity
import com.efthemiosprime.pasabayan.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and posts a foreground notification when an FCM push arrives. The PendingIntent
 * carries the original FCM payload so [com.efthemiosprime.pasabayan.MainActivity] can extract
 * a [com.efthemiosprime.pasabayan.features.notifications.model.PushNotificationRoute] (slice 5
 * wires the SharedFlow event).
 */
@Singleton
class NotificationDisplay @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    // Lint can't see that hasPostPermission() below gates the POST_NOTIFICATIONS
    // permission check for the `notify` call — the runtime guard is correct.
    @SuppressLint("MissingPermission")
    fun post(
        title: String,
        body: String,
        data: Map<String, String>,
        notificationId: Int = (System.currentTimeMillis() and Int.MAX_VALUE.toLong()).toInt(),
    ) {
        if (!hasPostPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_NOTIFICATION_DATA, HashMap(data))
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannelSetup.CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun hasPostPermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /** Activity intent extra carrying the FCM `data` map (HashMap<String, String>). */
        const val EXTRA_NOTIFICATION_DATA = "extra_notification_data"
    }
}
