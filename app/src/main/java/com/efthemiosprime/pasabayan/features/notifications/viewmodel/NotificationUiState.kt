package com.efthemiosprime.pasabayan.features.notifications.viewmodel

import com.efthemiosprime.pasabayan.core.network.notifications.PaginationInfoJson
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotification

data class NotificationUiState(
    val notifications: List<PushNotification> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val pagination: PaginationInfoJson? = null,
    val unreadCount: Int = 0,
    val carrierUnreadCount: Int = 0,
    val shipperUnreadCount: Int = 0,
    val testNotificationStatus: TestNotificationStatus? = null,
) {
    val hasMore: Boolean get() = pagination?.hasMore == true
}

enum class TestNotificationStatus { SENT, FAILED }
