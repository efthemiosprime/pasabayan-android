package com.efthemiosprime.pasabayan.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

// Spacing extensions
fun Modifier.dsSpacingXS() = this.padding(PasabayanDesignSystem.Spacing.xs)
fun Modifier.dsSpacingSM() = this.padding(PasabayanDesignSystem.Spacing.sm)
fun Modifier.dsSpacingMD() = this.padding(PasabayanDesignSystem.Spacing.md)
fun Modifier.dsSpacingLG() = this.padding(PasabayanDesignSystem.Spacing.lg)
fun Modifier.dsSpacingXL() = this.padding(PasabayanDesignSystem.Spacing.xl)
fun Modifier.dsSpacingXXL() = this.padding(PasabayanDesignSystem.Spacing.xxl)
fun Modifier.dsSpacingScreen() = this.padding(PasabayanDesignSystem.Spacing.screenPadding)
fun Modifier.dsSpacingCard() = this.padding(PasabayanDesignSystem.Spacing.cardPadding)

// Horizontal spacing extensions
fun Modifier.dsSpacingHorizontalXS() = this.padding(horizontal = PasabayanDesignSystem.Spacing.xs)
fun Modifier.dsSpacingHorizontalSM() = this.padding(horizontal = PasabayanDesignSystem.Spacing.sm)
fun Modifier.dsSpacingHorizontalMD() = this.padding(horizontal = PasabayanDesignSystem.Spacing.md)
fun Modifier.dsSpacingHorizontalLG() = this.padding(horizontal = PasabayanDesignSystem.Spacing.lg)
fun Modifier.dsSpacingHorizontalXL() = this.padding(horizontal = PasabayanDesignSystem.Spacing.xl)

// Vertical spacing extensions
fun Modifier.dsSpacingVerticalXS() = this.padding(vertical = PasabayanDesignSystem.Spacing.xs)
fun Modifier.dsSpacingVerticalSM() = this.padding(vertical = PasabayanDesignSystem.Spacing.sm)
fun Modifier.dsSpacingVerticalMD() = this.padding(vertical = PasabayanDesignSystem.Spacing.md)
fun Modifier.dsSpacingVerticalLG() = this.padding(vertical = PasabayanDesignSystem.Spacing.lg)
fun Modifier.dsSpacingVerticalXL() = this.padding(vertical = PasabayanDesignSystem.Spacing.xl)

// Corner radius extensions
fun Modifier.dsCornerRadiusXS() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.xs))
fun Modifier.dsCornerRadiusSM() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.sm))
fun Modifier.dsCornerRadiusMD() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.md))
fun Modifier.dsCornerRadiusLG() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.lg))
fun Modifier.dsCornerRadiusXL() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.xl))
fun Modifier.dsCornerRadiusXXL() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.xxl))

// Semantic corner radius extensions
fun Modifier.dsCornerRadiusCard() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card))
fun Modifier.dsCornerRadiusButton() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button))
fun Modifier.dsCornerRadiusModal() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.modal))
fun Modifier.dsCornerRadiusBadge() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.badge))
fun Modifier.dsCornerRadiusAvatar() = this.clip(RoundedCornerShape(PasabayanDesignSystem.CornerRadius.avatar))

// Elevation extensions
fun Modifier.dsShadowXS() = this.shadow(
    PasabayanDesignSystem.Elevation.xs, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.sm)
)
fun Modifier.dsShadowSM() = this.shadow(
    PasabayanDesignSystem.Elevation.sm, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.sm)
)
fun Modifier.dsShadowMD() = this.shadow(
    PasabayanDesignSystem.Elevation.md, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.md)
)
fun Modifier.dsShadowLG() = this.shadow(
    PasabayanDesignSystem.Elevation.lg, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.lg)
)

// Semantic elevation extensions
fun Modifier.dsShadowCard() = this.shadow(
    PasabayanDesignSystem.Elevation.card, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)
)
fun Modifier.dsShadowButton() = this.shadow(
    PasabayanDesignSystem.Elevation.button, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.button)
)
fun Modifier.dsShadowModal() = this.shadow(
    PasabayanDesignSystem.Elevation.modal, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.modal)
)
fun Modifier.dsShadowFloating() = this.shadow(
    PasabayanDesignSystem.Elevation.floating, 
    RoundedCornerShape(PasabayanDesignSystem.CornerRadius.lg)
) 