package com.efthemiosprime.pasabayan.features.support.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors

/** iOS parity: `SupportPriority`. */
enum class SupportPriority(
    val rawValue: String,
    val color: Color,
    @StringRes val displayNameRes: Int,
) {
    LOW("low", PasabayanColors.Success, R.string.support_priority_low),
    MEDIUM("medium", PasabayanColors.Warning, R.string.support_priority_medium),
    HIGH("high", PasabayanColors.Error, R.string.support_priority_high);

    companion object {
        private val byRaw = entries.associateBy { it.rawValue }
        fun fromRaw(raw: String?): SupportPriority? = raw?.let { byRaw[it] }
    }
}
