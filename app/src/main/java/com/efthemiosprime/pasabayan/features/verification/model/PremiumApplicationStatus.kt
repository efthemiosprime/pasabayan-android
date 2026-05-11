package com.efthemiosprime.pasabayan.features.verification.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * Server-side application status. Backend variants normalise to the app enum via [fromRaw].
 */
enum class PremiumApplicationStatus(
    val raw: String,
    @StringRes val labelRes: Int,
) {
    PENDING("pending", R.string.verification_premium_status_pending),
    UNDER_REVIEW("under_review", R.string.verification_premium_status_under_review),
    APPROVED("approved", R.string.verification_premium_status_approved),
    REJECTED("rejected", R.string.verification_premium_status_rejected),
    ;

    companion object {
        fun fromRaw(value: String?): PremiumApplicationStatus = when (value) {
            null, "pending_admin_review", "pending" -> PENDING
            "under_review" -> UNDER_REVIEW
            "approved" -> APPROVED
            "rejected" -> REJECTED
            else -> PENDING
        }
    }
}
