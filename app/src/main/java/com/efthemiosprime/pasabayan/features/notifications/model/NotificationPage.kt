package com.efthemiosprime.pasabayan.features.notifications.model

import com.efthemiosprime.pasabayan.core.network.notifications.PaginationInfoJson

/** Paginated notification history page. iOS parity: `NotificationHistoryData`. */
data class NotificationPage(
    val notifications: List<PushNotification>,
    val pagination: PaginationInfoJson?,
) {
    val hasMore: Boolean get() = pagination?.hasMore == true
    val total: Int get() = pagination?.total ?: notifications.size
}
