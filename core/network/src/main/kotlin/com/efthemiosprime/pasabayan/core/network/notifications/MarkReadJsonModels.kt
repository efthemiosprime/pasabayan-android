package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkReadResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: MarkReadDataJson? = null,
)

@Serializable
data class MarkReadDataJson(
    @SerialName("notification_id") val notificationId: Int = 0,
    @SerialName("read_at") val readAt: String? = null,
)

@Serializable
data class MarkAllReadResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: MarkAllReadDataJson? = null,
)

/**
 * Backend may return either `marked_count` or `updated_count`. Callers should prefer
 * [markedCount] then fall back to [updatedCount] via [resolvedCount].
 */
@Serializable
data class MarkAllReadDataJson(
    @SerialName("marked_count") val markedCount: Int? = null,
    @SerialName("updated_count") val updatedCount: Int? = null,
) {
    val resolvedCount: Int get() = markedCount ?: updatedCount ?: 0
}
