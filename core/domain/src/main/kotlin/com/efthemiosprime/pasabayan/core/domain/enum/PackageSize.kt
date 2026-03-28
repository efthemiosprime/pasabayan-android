package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PackageSize {
    @SerialName("small") SMALL,
    @SerialName("medium") MEDIUM,
    @SerialName("large") LARGE,
    @SerialName("extra_large") EXTRA_LARGE;

    companion object {
        fun fromWeight(kg: Double): PackageSize = when {
            kg <= 5.0 -> SMALL
            kg <= 15.0 -> MEDIUM
            kg <= 30.0 -> LARGE
            else -> EXTRA_LARGE
        }
    }
}
