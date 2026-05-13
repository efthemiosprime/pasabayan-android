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
    /** iOS `Color.pink` — tip badge accent. */
    val BadgePink = Color(0xFFFF2D55)

    /** iOS `OnboardingTheme.primary` / carrier accent — not the app-wide M3 primary. */
    val OnboardingPrimary = Color(0xFF4A6CF7)

    /** iOS system indigo. */
    val Indigo = Color(0xFF5856D6)

    // -- Generic status colors --
    val StatusPending = Color(0xFFFF9500)
    val StatusActive = Color(0xFF007AFF)
    val StatusCompleted = Color(0xFF34C759)
    val StatusCancelled = Color(0xFFFF3B30)
    val StatusMatched = Color(0xFFAF52DE)
    val StatusInTransit = Color(0xFF5856D6)

    // -- Trip status colors --
    val TripScheduled = Color(0xFF007AFF)
    val TripActive = Color(0xFF5856D6)
    val TripCompleted = Color(0xFF34C759)
    val TripCancelled = Color(0xFFFF3B30)

    // -- Package status colors --
    val PackageOpen = Color(0xFF007AFF)
    val PackageMatched = Color(0xFFAF52DE)
    val PackageBooked = Color(0xFFFF9500)
    val PackageInTransit = Color(0xFF5856D6)
    val PackageDelivered = Color(0xFF34C759)

    // -- Badge light variants (0.1 alpha) --
    val BadgeBlueLight = Color(0x1A007AFF)
    val BadgePurpleLight = Color(0x1AAF52DE)
    val BadgeOrangeLight = Color(0x1AFF9500)
    val BadgeGreenLight = Color(0x1A34C759)
    val BadgeTealLight = Color(0x1A00A699)
    val BadgeAmberLight = Color(0x1AFFB400)
    val BadgeRedLight = Color(0x1AC13515)
    val BadgeGoldLight = Color(0x1AFFD700)
    val BadgeGrayLight = Color(0x1A8E8E93)
    val BadgePinkLight = Color(0x1AFF2D55)

    val Border = Color(0xFFC6C6C8)
    val OverlayScrim = Color(0x4D000000)

    /** ~`systemGray4` (light) — disabled button fill per iOS `PButton`. */
    val ButtonDisabled = Color(0xFFD1D1D6)
}
