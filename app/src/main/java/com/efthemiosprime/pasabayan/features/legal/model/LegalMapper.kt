package com.efthemiosprime.pasabayan.features.legal.model

import com.efthemiosprime.pasabayan.core.network.legal.LegalStatusDataJson
import com.efthemiosprime.pasabayan.core.network.legal.PendingDocumentJson

/** DTO → domain mappers for the legal feature. */
object LegalMapper {

    fun toDomain(dto: PendingDocumentJson): LegalDocument = LegalDocument(
        id = dto.id,
        type = dto.type,
        title = dto.title,
        version = dto.version,
    )

    fun toDomain(dto: LegalStatusDataJson?): LegalStatus {
        val docs = dto?.pendingDocuments.orEmpty().map(::toDomain)
        return LegalStatus(
            allAgreed = dto?.allAgreed ?: false,
            pendingDocuments = docs,
            pendingCount = dto?.pendingCount ?: docs.size,
        )
    }
}
