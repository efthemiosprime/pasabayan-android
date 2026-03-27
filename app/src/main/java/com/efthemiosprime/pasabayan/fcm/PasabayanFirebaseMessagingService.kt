package com.efthemiosprime.pasabayan.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point. Register device token with backend (Phase 5 / [android-spec/08-notifications-device-tokens.md]).
 */
class PasabayanFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        // Route by notification type (e.g. counter_offer) when notifications feature lands
    }

    override fun onNewToken(token: String) {
        // POST device token — Phase 5
    }
}
