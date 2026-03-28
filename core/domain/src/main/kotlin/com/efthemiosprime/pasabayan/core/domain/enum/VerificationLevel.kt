package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VerificationLevel {
    @SerialName("basic") BASIC,
    @SerialName("verified") VERIFIED,
    @SerialName("premium") PREMIUM;

    val isVerified: Boolean
        get() = this == VERIFIED || this == PREMIUM

    val isPremium: Boolean
        get() = this == PREMIUM

    companion object {
        /**
         * Normalizes a raw verification level string from the API.
         * Returns VERIFIED or PREMIUM if recognized, BASIC otherwise.
         * Null input defaults to BASIC (mirrors iOS behavior).
         */
        fun normalized(raw: String?): VerificationLevel {
            if (raw.isNullOrBlank()) return BASIC
            return when (raw.trim().lowercase()) {
                "verified" -> VERIFIED
                "premium" -> PREMIUM
                else -> BASIC
            }
        }
    }
}
