package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.ui.graphics.Color

/** Mirrors [14-design-system.md] — brand is black-forward primary. */
object PasabayanColors {
    val PrimaryBlack = Color(0xFF000000)

    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9500)
    val Error = Color(0xFFFF3B30)
    val Info = Color(0xFF007AFF)

    val BadgeBlue = Color(0xFF007AFF)
    val BadgePurple = Color(0xFFAF52DE)
    val BadgeOrange = Color(0xFFFF9500)
    val BadgeGreen = Color(0xFF34C759)
    val BadgeTeal = Color(0xFF00A699)
    val BadgeAmber = Color(0xFFFFB400)
    val BadgeRust = Color(0xFFC13515)
    val BadgeGray = Color(0xFF8E8E93)
    val BadgeGold = Color(0xFFFFD700)

    /** iOS `OnboardingTheme.primary` / carrier accent — not the app-wide M3 primary. */
    val OnboardingPrimary = Color(0xFF4A6CF7)

    val Border = Color(0xFFC6C6C8)
    val OverlayScrim = Color(0x4D000000)

    /** ~`systemGray4` (light) — disabled button fill per iOS `PButton`. */
    val ButtonDisabled = Color(0xFFD1D1D6)
}
