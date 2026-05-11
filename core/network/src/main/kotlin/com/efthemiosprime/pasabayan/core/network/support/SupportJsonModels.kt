package com.efthemiosprime.pasabayan.core.network.support

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketResponseJson(
    val success: Boolean = false,
    val message: String? = null,
    val data: SupportTicketJson? = null,
)

@Serializable
data class SupportTicketJson(
    val id: Int = 0,
    val email: String = "",
    val subject: String = "",
    val category: String = "",
    val priority: String = "",
    val status: String? = null,
    val description: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("user_id") val userId: Int? = null,
    val attachments: List<SupportAttachmentJson>? = null,
)

@Serializable
data class SupportAttachmentJson(
    val path: String = "",
    @SerialName("original_name") val originalName: String = "",
    val mime: String = "",
    @SerialName("size_kb") val sizeKb: Double = 0.0,
)
