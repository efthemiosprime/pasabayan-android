package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationHistoryResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: NotificationHistoryDataJson? = null,
)

@Serializable
data class NotificationHistoryDataJson(
    val notifications: List<PushNotificationJson> = emptyList(),
    val pagination: PaginationInfoJson? = null,
)

/**
 * Notification-specific pagination shape. Not the generic `PaginatedResponse<T>` —
 * keys differ from the receipts/transactions pagination envelope.
 */
@Serializable
data class PaginationInfoJson(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 15,
    val total: Int = 0,
) {
    val hasMore: Boolean get() = currentPage < lastPage
}
