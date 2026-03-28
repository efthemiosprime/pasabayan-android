package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SortOrder {
    @SerialName("asc") ASCENDING,
    @SerialName("desc") DESCENDING,
}
