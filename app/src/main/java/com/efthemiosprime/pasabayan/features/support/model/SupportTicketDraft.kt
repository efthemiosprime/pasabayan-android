package com.efthemiosprime.pasabayan.features.support.model

import android.net.Uri

/**
 * In-memory draft of a support ticket as the user fills the form. Mirrors iOS
 * `SupportTicketRequest`.
 */
data class SupportTicketDraft(
    val category: SupportCategory? = null,
    val subject: String = "",
    val email: String = "",
    val priority: SupportPriority = SupportPriority.MEDIUM,
    val description: String = "",
    val attachments: List<Uri> = emptyList(),
) {
    val isSubmittable: Boolean
        get() = category != null &&
            subject.trim().length in MIN_SUBJECT..MAX_SUBJECT &&
            email.isLikelyEmail() &&
            description.trim().length >= MIN_DESCRIPTION

    companion object {
        const val MIN_SUBJECT = 3
        const val MAX_SUBJECT = 120
        const val MIN_DESCRIPTION = 10
        const val MAX_DESCRIPTION = 2_000
        const val MAX_ATTACHMENTS = 5
    }
}

/** Lenient email check — server is authoritative. */
internal fun String.isLikelyEmail(): Boolean {
    val trimmed = trim()
    val at = trimmed.indexOf('@')
    if (at <= 0 || at == trimmed.length - 1) return false
    val dot = trimmed.indexOf('.', startIndex = at)
    return dot > at && dot < trimmed.length - 1
}
