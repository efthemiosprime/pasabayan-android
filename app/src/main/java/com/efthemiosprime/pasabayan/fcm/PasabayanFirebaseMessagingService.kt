package com.efthemiosprime.pasabayan.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point. Register device token with backend (Phase 5).
 */
class PasabayanFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        // Route by notification type (e.g. counter_offer) when notifications feature lands
    }

    override fun onNewToken(token: String) {
        // POST device token — Phase 5
    }
}
