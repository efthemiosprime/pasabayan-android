package com.efthemiosprime.pasabayan.services.fcm

import com.efthemiosprime.pasabayan.features.notifications.services.NotificationChannelSetup
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationDisplay
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationLifecycleManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM entry point. Spec [08-notifications-device-tokens.md] § "Device token lifecycle" + §
 * "Foreground notification display".
 */
@AndroidEntryPoint
class PasabayanFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var lifecycleManager: NotificationLifecycleManager
    @Inject lateinit var notificationDisplay: NotificationDisplay
    @Inject lateinit var channelSetup: NotificationChannelSetup

    override fun onNewToken(token: String) {
        lifecycleManager.onTokenRefreshed(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        channelSetup.ensureChannels()
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: return
        val body = message.notification?.body ?: data["body"].orEmpty()
        notificationDisplay.post(title = title, body = body, data = data)
    }
}
