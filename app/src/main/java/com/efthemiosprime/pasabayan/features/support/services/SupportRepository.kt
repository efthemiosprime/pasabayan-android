package com.efthemiosprime.pasabayan.features.support.services

import android.net.Uri
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import com.efthemiosprime.pasabayan.features.support.model.SupportTicket

interface SupportRepository {
    /**
     * Submits a ticket via `POST /support/tickets` (multipart). Attachments are read through the
     * provided URIs at submit time.
     */
    suspend fun submit(
        category: SupportCategory,
        subject: String,
        email: String,
        priority: SupportPriority,
        description: String,
        attachments: List<Uri>,
    ): Result<SupportTicket>
}
