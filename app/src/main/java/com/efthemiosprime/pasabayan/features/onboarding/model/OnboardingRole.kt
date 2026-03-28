package com.efthemiosprime.pasabayan.features.onboarding.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors

/** Parity with iOS `UserRole` for onboarding only (wire value matches API `carrier` / `shipper`). */
@Immutable
enum class OnboardingRole(val wireValue: String) {
    Carrier("carrier"),
    Shipper("shipper"),
    ;

    val opposite: OnboardingRole
        get() = when (this) {
            Carrier -> Shipper
            Shipper -> Carrier
        }

    val accentColor: Color
        get() = when (this) {
            Carrier -> PasabayanColors.OnboardingPrimary
            Shipper -> PasabayanColors.BadgePurple
        }

    fun accentLightColor(): Color = accentColor.copy(alpha = 0.1f)

    @get:StringRes
    val displayNameRes: Int
        get() = when (this) {
            Carrier -> R.string.common_role_carrier
            Shipper -> R.string.common_role_sender
        }
}
