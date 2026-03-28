package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UrgencyLevel {
    @SerialName("low") LOW,
    @SerialName("normal") NORMAL,
    @SerialName("high") HIGH,
    @SerialName("urgent") URGENT,
    @SerialName("express") EXPRESS,
    @SerialName("flexible") FLEXIBLE;

    val icon: String
        get() = when (this) {
            LOW -> "🐌"
            NORMAL -> "📦"
            HIGH -> "⚡"
            URGENT -> "🚨"
            EXPRESS -> "🚀"
            FLEXIBLE -> "🔄"
        }
}
