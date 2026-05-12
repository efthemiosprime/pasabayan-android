package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.ui.graphics.Color
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.component.StatusBadgeConfig
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel

/**
 * [StatusBadgeConfig] mapping for [UrgencyLevel]. Parity with iOS `UrgencyBadge`
 * (`Pasabayan/Views/Components/UrgencyBadge.swift`). Intensity-ranked colour scale —
 * URGENT uses the error token, EXPRESS uses BadgePurple, HIGH uses BadgeAmber,
 * NORMAL uses BadgeBlue, FLEXIBLE/LOW use BadgeTeal/BadgeGray.
 *
 * Render with `PStatusBadge(config = UrgencyBadgeConfig(...), variant = Compact)` for
 * the explore-card header chip; no Material icon — the urgency emoji is the iOS-parity
 * affordance and we keep the chip compact.
 */
data class UrgencyBadgeConfig(
    private val level: UrgencyLevel,
    private val label: String,
) : StatusBadgeConfig {

    override val displayText: String get() = "${level.icon} $label"

    override val backgroundColor: Color
        get() = when (level) {
            UrgencyLevel.LOW -> PasabayanColors.BadgeGray
            UrgencyLevel.NORMAL -> PasabayanColors.BadgeBlue
            UrgencyLevel.HIGH -> PasabayanColors.BadgeAmber
            UrgencyLevel.URGENT -> PasabayanColors.Error
            UrgencyLevel.EXPRESS -> PasabayanColors.BadgePurple
            UrgencyLevel.FLEXIBLE -> PasabayanColors.BadgeTeal
        }

    override val textColor: Color get() = backgroundColor
}
