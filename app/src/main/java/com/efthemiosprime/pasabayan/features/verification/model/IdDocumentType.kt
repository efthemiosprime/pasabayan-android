package com.efthemiosprime.pasabayan.features.verification.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * `id_type` enum sent to `POST /verification/request-premium` (per iOS `IDDocumentType`). Passport
 * is single-sided ([requiresBackImage] = false); the rest require front + back.
 */
enum class IdDocumentType(
    val raw: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val requiresBackImage: Boolean,
) {
    DRIVERS_LICENSE(
        raw = "drivers_license",
        labelRes = R.string.verification_premium_id_drivers_license,
        descriptionRes = R.string.verification_premium_id_drivers_license_desc,
        requiresBackImage = true,
    ),
    PASSPORT(
        raw = "passport",
        labelRes = R.string.verification_premium_id_passport,
        descriptionRes = R.string.verification_premium_id_passport_desc,
        requiresBackImage = false,
    ),
    NATIONAL_ID(
        raw = "national_id",
        labelRes = R.string.verification_premium_id_national,
        descriptionRes = R.string.verification_premium_id_national_desc,
        requiresBackImage = true,
    ),
    POSTAL_ID(
        raw = "postal_id",
        labelRes = R.string.verification_premium_id_postal,
        descriptionRes = R.string.verification_premium_id_postal_desc,
        requiresBackImage = true,
    ),
    VOTERS_ID(
        raw = "voters_id",
        labelRes = R.string.verification_premium_id_voters,
        descriptionRes = R.string.verification_premium_id_voters_desc,
        requiresBackImage = true,
    ),
    ;

    companion object {
        fun fromRaw(value: String?): IdDocumentType? =
            entries.firstOrNull { it.raw == value }
    }
}
