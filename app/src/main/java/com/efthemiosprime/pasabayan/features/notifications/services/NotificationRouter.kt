package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotificationRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton bridge that fans navigation events from two input sources to a single Compose
 * observer:
 *  1. **Push tap** — `MainActivity` parses the FCM data map into a [PushNotificationRoute] and
 *     calls [emitFromPush].
 *  2. **In-app card tap** — the notification list view-model calls [emitFromInApp] with the
 *     tapped [PushNotification].
 *
 * Both paths funnel through [NotificationRouting.resolve], so the per-type/per-role mapping
 * lives in exactly one place.
 */
@Singleton
class NotificationRouter @Inject constructor() {

    private val _events = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 8,
    )
    val events: SharedFlow<NavigationEvent> = _events.asSharedFlow()

    fun emitFromPush(route: PushNotificationRoute): Boolean {
        val event = NotificationRouting.resolve(route.type, route.data) ?: return false
        return _events.tryEmit(event)
    }

    fun emitFromInApp(notification: PushNotification): Boolean {
        val event = NotificationRouting.resolve(notification.type, notification.data) ?: return false
        return _events.tryEmit(event)
    }
}
