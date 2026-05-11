package com.efthemiosprime.pasabayan.features.support.model

import com.efthemiosprime.pasabayan.core.network.support.SupportAttachmentJson
import com.efthemiosprime.pasabayan.core.network.support.SupportTicketJson

object SupportMapper {

    fun toDomain(dto: SupportTicketJson): SupportTicket = SupportTicket(
        id = dto.id,
        email = dto.email,
        subject = dto.subject,
        category = SupportCategory.fromRaw(dto.category),
        priority = SupportPriority.fromRaw(dto.priority),
        status = dto.status,
        description = dto.description,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        userId = dto.userId,
        attachments = dto.attachments.orEmpty().map(::toDomain),
    )

    fun toDomain(dto: SupportAttachmentJson): SupportAttachment = SupportAttachment(
        path = dto.path,
        originalName = dto.originalName,
        mime = dto.mime,
        sizeKb = dto.sizeKb,
    )
}
