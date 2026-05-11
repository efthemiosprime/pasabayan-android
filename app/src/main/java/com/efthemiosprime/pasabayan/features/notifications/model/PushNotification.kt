package com.efthemiosprime.pasabayan.features.notifications.model

/**
 * Domain notification model. Maps from
 * [com.efthemiosprime.pasabayan.core.network.notifications.PushNotificationJson].
 */
data class PushNotification(
    val id: Int,
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: NotificationData? = null,
    val recipientRole: String? = null,
    val sentAt: String,
    val readAt: String? = null,
    val clickedAt: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
)
