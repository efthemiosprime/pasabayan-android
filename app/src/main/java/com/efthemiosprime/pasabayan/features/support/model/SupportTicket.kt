package com.efthemiosprime.pasabayan.features.support.model

/** Domain twin of [com.efthemiosprime.pasabayan.core.network.support.SupportTicketJson]. */
data class SupportTicket(
    val id: Int,
    val email: String,
    val subject: String,
    val category: SupportCategory?,
    val priority: SupportPriority?,
    val status: String?,
    val description: String,
    val createdAt: String?,
    val updatedAt: String?,
    val userId: Int?,
    val attachments: List<SupportAttachment>,
)

data class SupportAttachment(
    val path: String,
    val originalName: String,
    val mime: String,
    val sizeKb: Double,
)
