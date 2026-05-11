package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.features.notifications.model.NotificationPage

/**
 * Repository surface for spec 08 endpoints. Implementations should keep auth-check / defer
 * logic out of this layer — callers (e.g. the FCM lifecycle manager) decide whether to call.
 */
interface NotificationRepository {

    /**
     * Register an FCM token. On 403 `consent_required` (`"push_notifications"`), the impl
     * auto-opts the user in and retries once. Returns the typed [DeviceTokenRegisterResult]
     * so callers can react to each terminal case.
     */
    suspend fun registerDeviceToken(fcmToken: String): DeviceTokenRegisterResult

    suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit>

    suspend fun fetchNotifications(
        page: Int = 1,
        unreadOnly: Boolean? = null,
        role: String? = null,
    ): Result<NotificationPage>

    suspend fun getUnreadCount(role: String? = null): Result<Int>

    suspend fun markAsRead(id: Int): Result<Unit>

    /** Returns the number of notifications marked as read. */
    suspend fun markAllAsRead(): Result<Int>

    suspend fun sendTestNotification(): Result<Unit>
}
